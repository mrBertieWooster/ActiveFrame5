package ru.intertrust.cm.core.dao.impl.access;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.config.DynamicGroupTrackDomainObjectsConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;

public class DynamicGroupTrackDomainObjectCollector extends BaseDynamicGroupServiceImpl implements
        DynamicGroupCollector {

    private DynamicGroupConfig config;
    private DynamicGroupTrackDomainObjectsConfig trackDomainObjects;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public List<Id> getPersons(Id contextId) {
        List<Id> groupMembres = getGroupMembers(contextId, false);
        return groupMembres;
    }

    @Override
    public List<Id> getGroups(Id contextId) {
        List<Id> groupMembres = getGroupMembers(contextId, true);
        return groupMembres;
    }

    // Получение типов, на которые реагирует текер
    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        // Не указан TrackDomainObjects или не указан тип, следим только за текущим типом
        if (trackDomainObjects == null || trackDomainObjects.getType() == null) {
            result.add(config.getContext().getDomainObject().getType());
        } else {
            result.add(trackDomainObjects.getType());
        }
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject,
            List<FieldModification> modifiedFields) {

        List<Id> result = new ArrayList<Id>();

        if (trackDomainObjects.getBindContext() != null && trackDomainObjects.getBindContext().getDoel() != null) {
            DoelExpression expression = DoelExpression.parse(trackDomainObjects.getBindContext().getDoel());
            //Проверяем статус
            if (trackDomainObjects.getStatus() != null) {
                String status = getStatusFor(domainObject.getId());
                if (trackDomainObjects.getStatus().equals(status)) {
                    List<Value> invalidContexts = doelResolver.evaluate(expression, domainObject.getId());
                    result = getIdList(invalidContexts);
                }
            } else {
                List<Value> invalidContexts = doelResolver.evaluate(expression, domainObject.getId());
                result = getIdList(invalidContexts);
            }
        } else {
            //bind контекст не указан, значит отслеживаемый объект и является контекстом
            //Если указан статус то проверяем его, если нет то сразу добавляем текущий объект
            if (trackDomainObjects.getStatus() != null) {
                String status = getStatusFor(domainObject.getId());
                if (trackDomainObjects.getStatus().equals(status)) {
                    result.add(domainObject.getId());
                }

            } else {
                result.add(domainObject.getId());
            }
        }

        return result;
    }

    @Override
    public void init(DynamicGroupConfig config, CollectorSettings setings) {
        this.config = config;
        if (config.getMembers() != null) {
            this.trackDomainObjects = (DynamicGroupTrackDomainObjectsConfig) setings;
        }
    }

    /**
     * Получает список персон динамической группы по дескриптору группы и контекстному объекту.
     *
     * @param contextObjectid
     *            контекстному объекту динамической группы
     * @param groups
     *            флаг указывающий какой doel использовать. Для получения групп или для получения пользователей
     * @return список персон группы
     */
    private List<Id> getGroupMembers(Id contextObjectid, boolean groups) {
        List<Id> result = new ArrayList<Id>();

        // В зависимости от флага получаем или группы или персоны
        String doel = null;
        if (groups) {
            if (trackDomainObjects.getGetGroup() != null) {
                doel = trackDomainObjects.getGetGroup().getDoel();
            }
        } else {
            if (trackDomainObjects.getGetPerson() != null) {
                doel = trackDomainObjects.getGetPerson().getDoel();
            }
        }

        if (doel != null) {
            DoelExpression getMemberExpr = DoelExpression
                    .parse(doel);
            List<Value> valueList = doelResolver.evaluate(getMemberExpr,
                    contextObjectid);
            result.addAll(getIdList(valueList));

            if (groups) {
                result = getDynGroups(result, trackDomainObjects.getGetGroup().getGroupName());
            }
        }        
        return result;
    }

    private List<Id> getDynGroups(List<Id> groupOwners, String groupName) {
        List<Id> result = new ArrayList<Id>();

        if (groupName != null && groupName.length() > 0) {
            for (Id groupOwner : groupOwners) {
                DomainObject dynGroup = personManagementService.findDynamicGroup(groupName, groupOwner);
                if (dynGroup != null) {
                    result.add(dynGroup.getId());
                }
            }
        } else {
            result = groupOwners;
        }
        return result;
    }

}
