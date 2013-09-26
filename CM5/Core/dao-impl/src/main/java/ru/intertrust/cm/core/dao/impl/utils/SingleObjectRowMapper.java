package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DomainObjectCacheServiceImpl;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Отображает {@link java.sql.ResultSet} на доменный объект {@link ru.intertrust.cm.core.business.api.dto.DomainObject}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class SingleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<DomainObject> {

    private DomainObjectCacheServiceImpl domainObjectCacheService;

    public SingleObjectRowMapper(String domainObjectType, ConfigurationExplorer configurationExplorer,
                                 DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectType, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer, domainObjectTypeIdCache);
    }

    @Override
    public DomainObject extractData(ResultSet rs) throws SQLException, DataAccessException {
        GenericDomainObject object = null;

        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumnNames().add(fieldName);
        }

        while (rs.next()) {
            object = new GenericDomainObject();
            object.setTypeName(domainObjectType);

            for (String columnName : columnModel.getColumnNames()) {
                FieldValueModel valueModel = new FieldValueModel();
                fillValueModel(rs, valueModel, columnName);
                fillObjectValue(object, valueModel, columnName);
            }
            
            //TODO добавлено Лариным. М. после выноса системных арибутов в родительский класс надо будет убрать эти 2 строчки
            object.setCreatedDate(object.getTimestamp("created_date"));
            object.setModifiedDate(object.getTimestamp("updated_date"));

            if (object.getId() != null) {
                getDomainObjectCacheService().putObjectToCache(object);
            }
        }
        return object;
    }

    private DomainObjectCacheServiceImpl getDomainObjectCacheService() {
        if (domainObjectCacheService == null) {
            domainObjectCacheService = SpringApplicationContext.getContext().getBean("domainObjectCacheService", DomainObjectCacheServiceImpl.class);
        }
        return domainObjectCacheService;
    }
}
