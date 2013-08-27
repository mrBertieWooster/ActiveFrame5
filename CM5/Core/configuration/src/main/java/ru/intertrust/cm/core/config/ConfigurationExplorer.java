package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.AccessMatrixConfig;
import ru.intertrust.cm.core.config.model.CollectionColumnConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;

import java.util.Collection;
import java.util.List;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 1:28 PM
 */
public interface ConfigurationExplorer {

    /**
     * Инициализирует экземпляр {@link ConfigurationExplorer}
     */
    void build();

    /**
     * Возвращает конфигурацию
     * @return конфигурация
     */
    Configuration getConfiguration();

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
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType);

    /**
     * Поиск конфигурации матрицы доступа для доменного объекта данного типа в данном статусе.
     * @param domainObjectType тип доменного объекта
     * @param status статус доменного объекта
     * @return конфигурация матрицы доступа
     */
    AccessMatrixConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status);

    /**
     * Возвращает конфигурацию контекстной роли по имени.
     * @param contextRoleName имя контекстной роли
     * @return конфигурация контекстной роли
     */
    ContextRoleConfig getContextRoleByName(String contextRoleName);
}
