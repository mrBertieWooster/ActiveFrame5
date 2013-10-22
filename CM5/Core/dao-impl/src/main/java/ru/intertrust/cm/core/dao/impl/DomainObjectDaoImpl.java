package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeSaveExtensionHandler;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.*;

import javax.sql.DataSource;
import java.util.*;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.generateParameter;

/**
 * @author atsvetkov
 */

public class DomainObjectDaoImpl implements DomainObjectDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private IdGenerator idGenerator;

    private DomainObjectCacheServiceImpl domainObjectCacheService;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private DynamicGroupService dynamicGroupService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    public void setDomainObjectCacheService(
            DomainObjectCacheServiceImpl domainObjectCacheService) {
        this.domainObjectCacheService = domainObjectCacheService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Устанавливает генератор для создания уникальных идентифиткаторово
     *
     * @param idGenerator
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Устанавливает {@link #configurationExplorer}
     *
     * @param configurationExplorer
     *            {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(
            ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(
            DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    public void setAccessControlService(
            AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void setDynamicGroupService(DynamicGroupService dynamicGroupService) {
        this.dynamicGroupService = dynamicGroupService;
    }

    @Override
    public DomainObject create(DomainObject domainObject) {
        DomainObject createdObject = create(domainObject,
                domainObjectTypeIdCache.getId(domainObject.getTypeName()));
        domainObjectCacheService.putObjectToCache(createdObject);

        //refreshDynamiGroupsAndAclForCreate(createdObject);
        return createdObject;
    }

    private void refreshDynamiGroupsAndAclForCreate(DomainObject createdObject) {
        dynamicGroupService.notifyDomainObjectCreated(createdObject.getId());
        permissionService.refreshAclFor(createdObject.getId());
    }

    @Override
    public DomainObject save(DomainObject domainObject)
            throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {

        DomainObject result = null;
        // Вызов точки расширения до сохранения
        BeforeSaveExtensionHandler beforeSaveExtension = extensionService
                .getExtentionPoint(BeforeSaveExtensionHandler.class,
                        domainObject.getTypeName());
        beforeSaveExtension.onBeforeSave(domainObject);

        DomainObjectVersion.AuditLogOperation operation = null;

        // Сохранение в базе
        if (domainObject.isNew()) {
            result = create(domainObject);
            operation = DomainObjectVersion.AuditLogOperation.CREATE;
        } else {
            result = update(domainObject);
            operation = DomainObjectVersion.AuditLogOperation.UPDATE;
        }

        // Запись в auditLog
        createAuditLog(result, result.getTypeName(),
                domainObjectTypeIdCache.getId(domainObject.getTypeName()), operation);

        // Вызов точки расширения после сохранения
        AfterSaveExtensionHandler afterSaveExtension = extensionService
                .getExtentionPoint(AfterSaveExtensionHandler.class,
                        domainObject.getTypeName());
        afterSaveExtension.onAfterSave(result);

        return result;
    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {
        // todo this can be optimized with batches

        List<DomainObject> result = new ArrayList<>();

        for (DomainObject domainObject : domainObjects) {
            DomainObject newDomainObject;
            newDomainObject = save(domainObject);
            result.add(newDomainObject);
        }

        return result;

    }

    @Override
    public DomainObject update(DomainObject domainObject)
            throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {
        GenericDomainObject updatedObject = new GenericDomainObject(
                domainObject);

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        updatedObject.getTypeName());

        validateIdType(updatedObject.getId());
        DomainObject parentDO = updateParentDO(domainObjectTypeConfig, domainObject);

        String query = generateUpdateQuery(domainObjectTypeConfig);

        Date currentDate = new Date();
        // В случае если сохранялся родительский объект то берем дату
        // модификации из нее, иначе в базе и возвращаемом доменном объекте
        // будут различные даты изменения и изменение объект отвалится по ошибке
        // OptimisticLockException
        if (parentDO != null) {
            currentDate = parentDO.getModifiedDate();
        }

        Map<String, Object> parameters = initializeUpdateParameters(
                updatedObject, domainObjectTypeConfig, currentDate);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0 && (!exists(updatedObject.getId()))) {
            throw new ObjectNotFoundException(updatedObject.getId());
        }

        if (!isDerived(domainObjectTypeConfig)) {
            if (count == 0) {
                throw new OptimisticLockException(updatedObject);
            }

            updatedObject.setModifiedDate(currentDate);
        }

        updatedObject.setModifiedDate(currentDate);
        updatedObject.resetDirty();

        domainObjectCacheService.putObjectToCache(updatedObject);

        //refreshDynamiGroupsAndAclForUpdate(domainObject);

        return updatedObject;

    }

    private void refreshDynamiGroupsAndAclForUpdate(DomainObject domainObject) {
        List<String> modifiedFields = getModifiedFieldNames(domainObject);
        dynamicGroupService.notifyDomainObjectChanged(domainObject.getId(), modifiedFields);
        permissionService.refreshAclFor(domainObject.getId());
    }

    private List<String> getModifiedFieldNames(DomainObject domainObject) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("DomainObjectDaoImpl");
        DomainObject originalDomainObject = find(domainObject.getId(),
                accessToken);

        List<String> modifiedFieldNames = new ArrayList<String>();
        for (String fieldName : domainObject.getFields()) {
            Value originalValue = originalDomainObject.getValue(fieldName);
            Value newValue = domainObject.getValue(fieldName);
            if (!originalValue.equals(newValue)) {
                modifiedFieldNames.add(fieldName);
            }

        }
        return modifiedFieldNames;
    }

    @Override
    public void delete(Id id) throws InvalidIdException,
            ObjectNotFoundException {
        validateIdType(id);

        RdbmsId rdbmsId = (RdbmsId) id;

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        getDOTypeName(rdbmsId.getTypeId()));

        // Получаем удаляемый доменный объект для нужд точек расширения
        AccessToken accessToken = accessControlService
                .createSystemAccessToken("DomainObjectDaoImpl");

        DomainObject deletedObject = find(id, accessToken);

        // Точка расширения до удаления
        BeforeDeleteExtensionHandler beforeDeleteEH =
                extensionService.getExtentionPoint(BeforeDeleteExtensionHandler.class, domainObjectTypeConfig.getName());
        beforeDeleteEH.onBeforeDelete(deletedObject);

        String query = generateDeleteQuery(domainObjectTypeConfig);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        int count = jdbcTemplate.update(query, parameters);

        domainObjectCacheService.removeObjectFromCache(id);

        if (count == 0) {
            throw new ObjectNotFoundException(rdbmsId);
        }

        //Удаление родительского объекта, перенесено ниже удаления дочернего объекта для того чтобы не ругались foreign key
        Id parentId = getParentId(rdbmsId, domainObjectTypeConfig);
        if (parentId != null) {
            delete(parentId);
        }

        // Пишем в аудит лог
        createAuditLog(deletedObject, deletedObject.getTypeName(), domainObjectTypeIdCache.getId(deletedObject.getTypeName()),
                DomainObjectVersion.AuditLogOperation.DELETE);

        // Точка расширения после
        AfterDeleteExtensionHandler afterDeleteEH =
                extensionService.getExtentionPoint(AfterDeleteExtensionHandler.class, domainObjectTypeConfig.getName());
        afterDeleteEH.onAfterDelete(deletedObject);

        //refreshDynamiGroupsAndAclForDelete(deletedObject);

    }

    private void refreshDynamiGroupsAndAclForDelete(DomainObject deletedObject) {
        dynamicGroupService.notifyDomainObjectDeleted(deletedObject.getId());
        permissionService.cleanAclFor(deletedObject.getId());
    }

    @Override
    public int delete(Collection<Id> ids) {
        // todo: in a batch
        // TODO как обрабатывать ошибки при удалении каждого доменного
        // объекта...
        int count = 0;
        for (Id id : ids) {
            try {
                delete(id);

                count++;
            } catch (ObjectNotFoundException e) {
                // ничего не делаем пока
            } catch (InvalidIdException e) {
                // //ничего не делаем пока
            }

        }
        return count;
    }

    @Override
    public boolean exists(Id id) throws InvalidIdException {
        if (domainObjectCacheService.getObjectToCache(id) != null) {
            return true;
        }

        RdbmsId rdbmsId = (RdbmsId) id;
        validateIdType(id);

        StringBuilder query = new StringBuilder();
        query.append(generateExistsQuery(getDOTypeName(rdbmsId.getTypeId())));

        Map<String, Object> parameters = initializeExistsParameters(id);
        long total = jdbcTemplate.queryForObject(query.toString(), parameters,
                Long.class);

        return total > 0;
    }

    @Override
    public DomainObject find(Id id, AccessToken accessToken) {
        if (id == null) {
            throw new IllegalArgumentException("Object id can not be null");
        }

        DomainObject domainObject = domainObjectCacheService
                .getObjectToCache(id);
        if (domainObject != null) {
            return domainObject;
        }

        RdbmsId rdbmsId = (RdbmsId) id;
        String typeName = getDOTypeName(rdbmsId.getTypeId());

        String query = generateFindQuery(typeName, accessToken);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }

        return jdbcTemplate.query(query, parameters, new SingleObjectRowMapper(
                typeName, configurationExplorer, domainObjectTypeIdCache));
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType, AccessToken accessToken) {
        return findAll(domainObjectType, 0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType, int offset, int limit, AccessToken accessToken) {
        if (domainObjectType == null || domainObjectType.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain Object type can not be null or empty");
        }

        if (ConfigurationExplorer.REFERENCE_TYPE_ANY.equals(domainObjectType)) {
            throw new IllegalArgumentException("'*' is not a valid Domain Object type");
        }

        String[] cacheKey = new String[] { domainObjectType,String.valueOf(offset),String.valueOf(limit) };
        List<DomainObject> result = domainObjectCacheService.getObjectToCache(cacheKey);
        if (result != null) {
            return result;
        }

        String query = generateFindAllQuery(domainObjectType, offset, limit, accessToken);

        Map<String, Object> parameters = new HashMap<String, Object>();
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }

        result = jdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(domainObjectType, configurationExplorer, domainObjectTypeIdCache));
        domainObjectCacheService.putObjectToCache(result, cacheKey);

        return result;
    }

    @Override
    public List<DomainObject> find(List<Id> ids, AccessToken accessToken) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        List<DomainObject> allDomainObjects = new ArrayList<DomainObject>();

        IdSorterByType idSorterByType = new IdSorterByType(
                ids.toArray(new RdbmsId[ids.size()]));

        for (final Integer domainObjectType : idSorterByType
                .getDomainObjectTypeIds()) {
            List<RdbmsId> idsOfSingleType = idSorterByType
                    .getIdsOfType(domainObjectType);
            String doTypeName = domainObjectTypeIdCache
                    .getName(domainObjectType);
            allDomainObjects.addAll(findSingleTypeDomainObjects(
                    idsOfSingleType, accessToken, doTypeName));
        }

        return allDomainObjects;
    }

    /**
     * Поиск доменных объектов одного типа.
     *
     * @param ids
     *            идентификаторы доменных объектов
     * @param accessToken
     *            маркер доступа
     * @param domainObjectType
     *            тип доменного объекта
     * @return список доменных объектов
     */
    private List<DomainObject> findSingleTypeDomainObjects(List<RdbmsId> ids, AccessToken accessToken,
            String domainObjectType) {
        List<DomainObject> cachedDomainObjects = domainObjectCacheService.getObjectToCache(ids);
        if (cachedDomainObjects != null && cachedDomainObjects.size() == ids.size()) {
            return cachedDomainObjects;
        }

        List<RdbmsId> idsToRead = null;
        if (cachedDomainObjects == null) {
            idsToRead = ids;
        } else {
            idsToRead = new ArrayList<>(ids.size());
            for (DomainObject domainObject : cachedDomainObjects) {
                idsToRead.remove(domainObject.getId());
            }
        }

        StringBuilder query = new StringBuilder();

        Map<String, Object> aclParameters = new HashMap<String, Object>();

        if (accessToken.isDeferred()) {
            /*
             * String aclReadTable = AccessControlUtility
             * .getAclReadTableNameFor(domainObjectType);
             * query.append("select distinct t.* from " + domainObjectType +
             * " t inner join " + aclReadTable + " r " +
             * "on t.id = r.object_id inner join group_member gm on r.group_id = gm.usergroup "
             * + "where gm.person_id = :user_id and t.id in (:object_ids) ");
             *
             * aclParameters = getAclParameters(accessToken);
             */

        } else {
            query.append("select * from ").append(domainObjectType)
                    .append(" where ID in (:object_ids) ");
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        List<Long> listIds = AccessControlUtility.convertRdbmsIdsToLongIds(idsToRead);
        parameters.put("object_ids", listIds);

        if (accessToken.isDeferred()) {
            parameters.putAll(aclParameters);
        }

        List<DomainObject> readDomainObjects = jdbcTemplate.query(query.toString(), parameters,
                new MultipleObjectRowMapper(domainObjectType, configurationExplorer, domainObjectTypeIdCache));

        if (cachedDomainObjects == null) {
            return readDomainObjects;
        } else {
            List result = cachedDomainObjects;
            result.addAll(readDomainObjects);
            return result;
        }
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, AccessToken accessToken) {
        return findLinkedDomainObjects(domainObjectId, linkedType, linkedField, 0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
            int offset, int limit, AccessToken accessToken) {
        String[] cacheKey = new String[] { linkedType,linkedField,String.valueOf(offset),String.valueOf(limit) };
        List<DomainObject> domainObjects = domainObjectCacheService.getObjectToCache(domainObjectId, cacheKey);
        if (domainObjects != null) {
            return domainObjects;
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("domain_object_id", ((RdbmsId) domainObjectId).getId());
        String query = buildFindChildrenQuery(linkedType, linkedField, offset, limit, accessToken);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }

        domainObjects = jdbcTemplate.query(query, parameters, new MultipleObjectRowMapper(linkedType,
                configurationExplorer, domainObjectTypeIdCache));
        domainObjectCacheService.putObjectToCache(domainObjectId, domainObjects, cacheKey);

        return domainObjects;
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, AccessToken accessToken) {
        return findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, 0, 0, accessToken);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
            int offset, int limit, AccessToken accessToken) {
        String[] cacheKey = new String[] { linkedType,linkedField,String.valueOf(offset),String.valueOf(limit) };
        List<DomainObject> domainObjects = domainObjectCacheService.getObjectToCache(domainObjectId, cacheKey);
        if (domainObjects != null) {
            return extractIds(domainObjects);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("domain_object_id", ((RdbmsId) domainObjectId).getId());
        String query = buildFindChildrenIdsQuery(linkedType, linkedField, offset, limit, accessToken);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }
        Integer linkedTypeId = domainObjectTypeIdCache.getId(linkedType);
        return jdbcTemplate.query(query, parameters, new MultipleIdRowMapper(
                linkedTypeId));
    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     *
     * @param domainObject
     *            доменный объект
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeCreateParameters(
            DomainObject domainObject,
            DomainObjectTypeConfig domainObjectTypeConfig,
            Integer type) {

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        if (!isDerived(domainObjectTypeConfig)) {
            parameters.put("created_date", getGMTDate(domainObject.getCreatedDate()));
            parameters.put("updated_date", getGMTDate(domainObject.getModifiedDate()));
        }
        parameters.put("type_id", type);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;
    }

    /**
     * Создает SQL запрос для нахождения доменного объекта
     *
     * @param typeName
     *            тип доменного объекта
     * @return SQL запрос для нахождения доменного объекта
     */
    protected String generateFindQuery(String typeName, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendTableNameQueryPart(query, typeName);
        query.append(" where ").append(tableAlias).append(".ID=:id ");

        Map<String, Object> aclParameters = new HashMap<String, Object>();
        if (accessToken.isDeferred()) {
            /*
             * String aclReadTable = AccessControlUtility
             * .getAclReadTableName(typeName);
             * query.append(" and exists (select a.object_id from " +
             * aclReadTable + " a inner join group_member gm " +
             * "on a.group_id = gm.usergroup where gm.person_id = :user_id and a.object_id = :id)"
             * );
             */
        }

        return query.toString();
    }

    /**
     * Создает SQL запрос для нахождения всех доменных объектов определенного
     * типа
     *
     * @param typeName
     *            тип доменного объекта
     * @return SQL запрос для нахождения доменного объекта
     */
    protected String generateFindAllQuery(String typeName, int offset, int limit, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendTableNameQueryPart(query, typeName);

        if (accessToken.isDeferred()) {
            /*
             * String aclReadTable = AccessControlUtility
             * .getAclReadTableName(typeName); query.append(
             * " where exists (select a.object_id from " + aclReadTable +
             * " a inner join group_member gm " +
             * "on a.group_id = gm.usergroup where gm.person_id = :user_id and a.object_id = "
             * ) .append(tableAlias).append(".ID)");
             */
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    /**
     * Создает SQL запрос для модификации доменного объекта
     *
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для модиификации доменного объекта с параметрами
     */
    protected String generateUpdateQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {

        StringBuilder query = new StringBuilder();

        String tableName = getSqlName(domainObjectTypeConfig);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

        List<String> columnNames = DataStructureNamingHelper.getColumnNames(feldConfigs);

        String fieldsWithparams = DaoUtils
                .generateCommaSeparatedListWithParams(columnNames);

        query.append("update ").append(tableName).append(" set ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(UPDATED_DATE_COLUMN).append("=:current_date, ");
        }

        query.append(fieldsWithparams);
        query.append(" where ").append(ID_COLUMN).append("=:id");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(" and ").append(UPDATED_DATE_COLUMN)
                    .append("=:updated_date");
        }

        return query.toString();

    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     *
     * @param domainObject
     *            доменный объект
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeUpdateParameters(
            DomainObject domainObject,
            DomainObjectTypeConfig domainObjectTypeConfig, Date currentDate) {

        Map<String, Object> parameters = new HashMap<String, Object>();

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        parameters.put("id", rdbmsId.getId());
        parameters.put("current_date", getGMTDate(currentDate));
        parameters.put("updated_date", getGMTDate(domainObject.getModifiedDate()));

        List<FieldConfig> fieldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, fieldConfigs, parameters);

        return parameters;

    }

    /**
     * Создает SQL запрос для создания доменного объекта
     *
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для создания доменного объекта с параметрами
     */
    protected String generateCreateQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> fieldConfigs = domainObjectTypeConfig
                .getFieldConfigs();

        String tableName = getSqlName(domainObjectTypeConfig);
        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(fieldConfigs);

        String commaSeparatedColumns = StringUtils
                .collectionToCommaDelimitedString(columnNames);
        String commaSeparatedParameters = DaoUtils
                .generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(tableName).append(" (ID, ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(CREATED_DATE_COLUMN).append(", ").append(UPDATED_DATE_COLUMN).append(", ");
        }
        query.append(TYPE_COLUMN).append(", ");

        query.append(commaSeparatedColumns);
        query.append(") values (:id , ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(":created_date, :updated_date, ");
        }
        query.append(":type_id, ");

        query.append(commaSeparatedParameters);
        query.append(")");

        return query.toString();

    }

    /**
     * Формирование запроса на добавление записи в таблицу аудита
     * @param domainObjectTypeConfig
     * @return
     */
    protected String generateCreateAuditLogQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> fieldConfigs = domainObjectTypeConfig
                .getFieldConfigs();

        String tableName = getSqlName(domainObjectTypeConfig) + "_LOG";
        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(fieldConfigs);

        String commaSeparatedColumns = StringUtils
                .collectionToCommaDelimitedString(columnNames);
        String commaSeparatedParameters = DaoUtils
                .generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(tableName).append("(");
        query.append(ID_COLUMN).append(", ");
        query.append(TYPE_COLUMN).append(", ");
        if (!isDerived(domainObjectTypeConfig)) {
            query.append(OPERATION_COLUMN).append(", ");
            query.append(UPDATED_DATE_COLUMN).append(", ");
            query.append(COMPONENT).append(", ");
            query.append(DOMAIN_OBJECT_ID).append(", ");
            query.append(INFO).append(", ");
            query.append(IP_ADDRESS).append(", ");
        }

        query.append(commaSeparatedColumns);
        query.append(") values (:ID, :TYPE_ID, ");
        if (!isDerived(domainObjectTypeConfig)) {
            query.append(":OPERATION, :UPDATED_DATE, :COMPONENT, :DOMAIN_OBJECT_ID, :INFO, :IP_ADDRESS, ");
        }

        query.append(commaSeparatedParameters);
        query.append(")");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления доменного объекта
     *
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateDeleteQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления всех доменных объектов
     *
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для удаления всех доменных объектов
     */
    protected String generateDeleteAllQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);

        return query.toString();

    }

    /**
     * Инициализирует параметр c id доменного объекта
     *
     * @param id
     *            идентификатор доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeIdParameter(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        return parameters;
    }

    /**
     * Создает SQL запрос для проверки существует ли доменный объект
     *
     * @param domainObjectName
     *            название доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateExistsQuery(String domainObjectName) {

        String tableName = getSqlName(domainObjectName);

        StringBuilder query = new StringBuilder();
        query.append("select id from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    /**
     * Инициализирует параметры для удаления доменного объекта
     *
     * @param id
     *            идентификатор доменных объектов для удаления
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeExistsParameters(Id id) {

        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());

        return parameters;
    }

    /**
     * Инициализация параметров для отложенной провеки доступа.
     *
     * @param accessToken
     * @return
     */
    protected Map<String, Object> getAclParameters(AccessToken accessToken) {
        long userId = ((UserSubject) accessToken.getSubject()).getUserId();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", userId);
        return parameters;
    }

    /**
     * Проверяет какого типа идентификатор
     */
    private void validateIdType(Id id) {
        if (id == null) {
            throw new InvalidIdException(id);
        }
        if (!(id instanceof RdbmsId)) {
            throw new InvalidIdException(id);
        }
    }

    private void initializeDomainParameters(DomainObject domainObject,
            List<FieldConfig> fieldConfigs, Map<String, Object> parameters) {
        for (FieldConfig fieldConfig : fieldConfigs) {
            Value value = null;
            // В случае удаление сюда придет null в параметре domainObject, при
            // этом значения параметров инициализируем null
            if (domainObject != null) {
                value = domainObject.getValue(fieldConfig.getName());
            }
            String columnName = getSqlName(fieldConfig.getName());
            String parameterName = generateParameter(columnName);

            if (value == null || value.get() == null) {
                if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                    parameters.put(parameterName, null);
                    parameterName = generateParameter(getReferenceTypeColumnName((ReferenceFieldConfig) fieldConfig));
                    parameters.put(parameterName, null);
                } else {
                    parameters.put(parameterName, null);
                }
                continue;
            }

            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                RdbmsId rdbmsId = (RdbmsId) value.get();
                parameters.put(parameterName, rdbmsId.getId());
                parameterName = generateParameter(getReferenceTypeColumnName((ReferenceFieldConfig) fieldConfig));
                parameters.put(parameterName, rdbmsId.getTypeId());
            } else if (DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
                parameters.put(parameterName, getGMTDate((Date) value.get()));
            } else {
                parameters.put(parameterName, value.get());
            }
        }
    }

    protected String buildFindChildrenQuery(String linkedType, String linkedField, int offset, int limit,
            AccessToken accessToken) {
        StringBuilder query = new StringBuilder();
        query.append("select t.* from ").append(linkedType)
                .append(" t where t.").append(linkedField)
                .append(" = :domain_object_id");
        if (accessToken.isDeferred()) {
            // appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, "t", offset, limit);

        return query.toString();
    }

    protected String buildFindChildrenIdsQuery(String linkedType, String linkedField, int offset, int limit,
            AccessToken accessToken) {
        StringBuilder query = new StringBuilder();
        query.append("select t.id from ").append(linkedType)
                .append(" t where t.").append(linkedField)
                .append(" = :domain_object_id");
        if (accessToken.isDeferred()) {
            // appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, "t", offset, limit);

        return query.toString();
    }

    private void appendAccessControlLogicToQuery(StringBuilder query,
            String linkedType) {
        String childAclReadTable = AccessControlUtility
                .getAclReadTableNameFor(linkedType);
        query.append(" and exists (select r.object_id from ")
                .append(childAclReadTable).append(" r ");
        query.append("inner join group_member gm on r.group_id = gm.usergroup where gm.person_id = :user_id and r"
                + ".object_id = t.id)");
    }

    private DomainObject create(DomainObject domainObject, Integer type) {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        domainObject.getTypeName());
        GenericDomainObject updatedObject = new GenericDomainObject(domainObject);

        DomainObject parentDo = createParentDO(domainObject,
                domainObjectTypeConfig, type);

        if (parentDo != null) {
            updatedObject.setCreatedDate(parentDo.getCreatedDate());
            updatedObject.setModifiedDate(parentDo.getModifiedDate());
        } else {
            Date currentDate = new Date();
            updatedObject.setCreatedDate(currentDate);
            updatedObject.setModifiedDate(currentDate);
        }

        String query = generateCreateQuery(domainObjectTypeConfig);

        Object id;
        if (parentDo != null) {
            id = ((RdbmsId) parentDo.getId()).getId();
        } else {
            id = idGenerator.generatetId(domainObjectTypeConfig);
        }

        RdbmsId doId = new RdbmsId(type, (Long) id);
        updatedObject.setId(doId);

        Map<String, Object> parameters = initializeCreateParameters(updatedObject, domainObjectTypeConfig, type);
        jdbcTemplate.update(query, parameters);

        return updatedObject;
    }

    /**
     * Получение конфигурации включения аудит лога для типа
     * @param domainObjectTypeConfig
     * @return
     */
    private boolean isAuditLogEnable(DomainObjectTypeConfig domainObjectTypeConfig) {
        boolean result = false;

        // Если в конфигурации доменного объекта указан флаг включения аудит
        // лога то принимаем его
        if (domainObjectTypeConfig.isAuditLog() != null) {
            result = domainObjectTypeConfig.isAuditLog();
        } else {
            // Если в конфигурации доменного объекта НЕ указан флаг включения
            // аудит лога то принимаем конфигурацию из блока глобальной
            // конфигурации
            GlobalSettingsConfig globalSettings = configurationExplorer.getConfiguration().getGlobalSettings();
            if (globalSettings != null && globalSettings.getAuditLog() != null) {
                result = globalSettings.getAuditLog().isEnable();
            }
        }
        return result;
    }

    /**
     * Запись информации аудит лог в базу
     * @param domainObject
     * @param type
     * @return
     */
    private Long createAuditLog(DomainObject domainObject, String typeName, Integer type, DomainObjectVersion.AuditLogOperation operation) {
        Long id = null;
        if (typeName != null) {
            DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                    .getConfig(DomainObjectTypeConfig.class, typeName);

            // Проверка на включенность аудит лога, или если пришли рекурсивно
            // из подчиненного уровня, где аудит был вулючен
            if (isAuditLogEnable(domainObjectTypeConfig) || !domainObject.getTypeName().equals(typeName)) {

                id = createAuditLog(domainObject,
                        domainObjectTypeConfig.getExtendsAttribute(), type, operation);

                if (id == null) {
                    id = (Long) idGenerator.generatetLogId(domainObjectTypeConfig);
                }

                String query = generateCreateAuditLogQuery(domainObjectTypeConfig);

                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(DomainObjectDao.ID_COLUMN, id);
                parameters.put(DomainObjectDao.TYPE_COLUMN, type);

                if (!isDerived(domainObjectTypeConfig)) {
                    parameters.put(DomainObjectDao.OPERATION_COLUMN, operation.getOperation());
                    parameters.put(DomainObjectDao.UPDATED_DATE_COLUMN, getGMTDate(domainObject.getModifiedDate()));
                    // TODO Получение имени компонента из AcceeToken
                    parameters.put(DomainObjectDao.COMPONENT, "");
                    parameters.put(DomainObjectDao.DOMAIN_OBJECT_ID, ((RdbmsId) domainObject.getId()).getId());
                    parameters.put(DomainObjectDao.INFO, "");
                    // TODO Получение ip адреса
                    parameters.put(DomainObjectDao.IP_ADDRESS, "");
                }

                List<FieldConfig> feldConfigs = domainObjectTypeConfig
                        .getDomainObjectFieldsConfig().getFieldConfigs();

                if (operation == DomainObjectVersion.AuditLogOperation.DELETE) {
                    initializeDomainParameters(null, feldConfigs, parameters);
                } else {
                    initializeDomainParameters(domainObject, feldConfigs, parameters);
                }

                jdbcTemplate.update(query, parameters);
            }

        }
        return id;
    }

    private DomainObject createParentDO(DomainObject domainObject,
            DomainObjectTypeConfig domainObjectTypeConfig, Integer type) {
        if (!isDerived(domainObjectTypeConfig)) {
            return null;
        }

        GenericDomainObject parentDO = new GenericDomainObject(domainObject);
        parentDO.setTypeName(domainObjectTypeConfig.getExtendsAttribute());
        return create(parentDO, type);
    }

    private DomainObject updateParentDO(DomainObjectTypeConfig domainObjectTypeConfig, DomainObject domainObject) {
        RdbmsId parentId = getParentId((RdbmsId) domainObject.getId(), domainObjectTypeConfig);
        if (parentId == null) {
            return null;
        }

        GenericDomainObject parentObject = new GenericDomainObject(domainObject);
        parentObject.setId(parentId);
        parentObject.setTypeName(domainObjectTypeConfig.getExtendsAttribute());

        return update(parentObject);
    }

    private void appendTableNameQueryPart(StringBuilder query, String typeName) {
        String tableName = getSqlName(typeName);
        query.append(tableName).append(" ").append(getSqlAlias(tableName));
        appendParentTable(query, typeName);
    }

    private void appendColumnsQueryPart(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        query.append(getSqlAlias(typeName)).append(".*");

        if (isDerived(config)) {
            appendParentColumns(query, config);
        }
    }

    private void appendParentTable(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        if (config.getExtendsAttribute() == null) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        String parentTableName = getSqlName(config.getExtendsAttribute());
        String parentTableAlias = getSqlAlias(config.getExtendsAttribute());

        query.append(" inner join ").append(parentTableName).append(" ")
                .append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(ID_COLUMN).append("=");
        query.append(parentTableAlias).append(".").append(ID_COLUMN);

        appendParentTable(query, config.getExtendsAttribute());
    }

    private void appendParentColumns(StringBuilder query,
            DomainObjectTypeConfig config) {
        DomainObjectTypeConfig parentConfig = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, config.getExtendsAttribute());

        String tableAlias = getSqlAlias(parentConfig.getName());

        for (FieldConfig fieldConfig : parentConfig.getFieldConfigs()) {
            if ("ID".equals(fieldConfig.getName())) {
                continue;
            }

            query.append(", ").append(tableAlias).append(".").append(getSqlName(fieldConfig));
        }

        if (parentConfig.getExtendsAttribute() != null) {
            appendParentColumns(query, parentConfig);
        } else {
            query.append(", ").append(CREATED_DATE_COLUMN);
            query.append(", ").append(UPDATED_DATE_COLUMN);
        }
    }

    private boolean isDerived(DomainObjectTypeConfig domainObjectTypeConfig) {
        return domainObjectTypeConfig.getExtendsAttribute() != null;
    }

    private RdbmsId getParentId(RdbmsId id, DomainObjectTypeConfig domainObjectTypeConfig) {
        if (!isDerived(domainObjectTypeConfig)) {
            return null;
        }

        int parentType = domainObjectTypeIdCache.getId(domainObjectTypeConfig.getExtendsAttribute());
        return new RdbmsId(parentType, id.getId());
    }

    private String getDOTypeName(Integer typeId) {
        return domainObjectTypeIdCache.getName(typeId);
    }

    private void applyOffsetAndLimitWithDefaultOrdering(StringBuilder query, String tableAlias, int offset,
            int limit) {
        if (limit != 0) {
            query.append(" order by ").append(tableAlias).append(".ID");
            PostgreSqlQueryHelper.applyOffsetAndLimit(query, offset, limit);
        }
    }

    private List<Id> extractIds(List<DomainObject> domainObjectList) {
        if (domainObjectList == null || domainObjectList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Id> result = new ArrayList<>(domainObjectList.size());
        for (DomainObject domainObject : domainObjectList) {
            result.add(domainObject.getId());
        }

        return result;
    }

    private Calendar getGMTDate(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);
        return calendar;
    }
}
