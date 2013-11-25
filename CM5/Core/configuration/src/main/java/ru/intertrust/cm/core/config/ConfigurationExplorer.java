package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

import java.util.Collection;
import java.util.List;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 1:28 PM
 */
public interface ConfigurationExplorer {

    String REFERENCE_TYPE_ANY = "*";

    /**
     * Возвращает конфигурацию
     * @return конфигурация
     */
    Configuration getConfiguration();

    /**
     * Возвращает глобальные настройки конфигурации
     * @return конфигурация
     */
    GlobalSettingsConfig getGlobalSettings();

    /**
     * Возвращает конфигурацию верхнего уровня
     * @param type класс конфигурации верхнего уровня
     * @param name имя конфигурации верхнего уровня
     * @return конфигурация верхнего уровня
     */
    <T> T getConfig(Class<T> type, String name);

    /**
     * Возвращает все конфигурации верхнего уровня данного типа type
     * @param type класс конфигурации верхнего уровня
     * @return все конфигурации верхнего уровня данного типа type
     */
    <T> Collection<T> getConfigs(Class<T> type);

    /**
     * Находит конфигурацию всех типов доменных объектов, являющихся дочерними для заданного типа.
     * @param typeName имя типа доменного объекта
     * @param includeIndirect true, если в результат должны быть включены все уровни наследников
     * @return коллекция типов доменных объектов
     */
    Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля ()
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @param returnInheritedConfig указывает искать конфигруцию поля доменного объекта в иерархии типов
     *                                   доменных объектов или нет (искать только среди собственных конфигураций полей
     *                                   типа доменного объекта)
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig);

    /**
     * Находит конфигурацию отображаемого поля коллекции по имени коллекции и имени поля
     * @param collectionConfigName имя коллекции
     * @param columnConfigName имя поля
     * @return конфигурацию отображаемого поля коллекции
     */
    CollectionColumnConfig getCollectionColumnConfig(String collectionConfigName, String columnConfigName);

    /**
     * Поиск списка динамических групп по типу контекстного доменного объекта.
     * @param domainObjectType типу контекстного объекта
     * @return спискок динамических групп
     */
    List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType);

    /**
     * Поиск динамических групп по отслеживаемым объектам.
     * @param objectTypeName тип отслеживаемого объекта
     * @param status статус отслеживаемого объекта
     * @return список дескрипторов динамических групп
     */
    List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String objectTypeName, String status);

    /**
     * Поиск конфигурации матрицы доступа для доменного объекта данного типа в данном статусе.
     * @param domainObjectType тип доменного объекта
     * @param status статус доменного объекта
     * @return конфигурация матрицы доступа
     */
    AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status);

    /**
     * Возвращает конфигурацию контекстной роли по имени.
     * @param contextRoleName имя контекстной роли
     * @return конфигурация контекстной роли
     */
    ContextRoleConfig getContextRoleByName(String contextRoleName);

    /**
     * Возвращает конфигурацию динамической группы по имени.
     * @param dynamicGroupName имя динамической группы
     * @return конфигурация динамической группы
     */
    DynamicGroupConfig getDynamicGroupByName(String dynamicGroupName);
}
