package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.model.BusinessObjectConfig;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;

import java.util.List;

/**
 * DAO для работы с бизнесс объектами выполняет операции создания, модификации,
 * удаления, поиска и т.д.
 *
 */
public interface CrudServiceDAO {


    /**
     * Создает новый бизнес-объект
     * @param businessObject
     *            бизнес-объект который будет создан
     * @param businessObjectConfig
     *            конфигурация бизнесс объекта
     * @return созданыый бизнесс объект
     */
    public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);

    /**
     * Модифицирует переданный бизнес-объект
     * @param businessObject
     *            бизнес-объект который надо изменить
     * @param businessObjectConfig
     *            конфигурация бизнесс объекта
     * @return возвращет модифицированный бизнесс объект
     * @throws InvalidIdException
     *             если идентификатор бизнес-объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     * @throws OptimisticLockException
     *             если объект уже был модифицирован другим пользователем
     */
    public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig)
            throws InvalidIdException, ObjectNotFoundException, OptimisticLockException;

    /**
     * Удаляет бизнес-объект по уникальному идентифткатору
     * @param id
     *            уникальный идентификатор объекта который надо удалить
     * @param businessObjectConfig
     *            конфигурация бизнесс-объекта
     * @throws InvalidIdException
     *             если идентификатор бизнес-объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     */
    public void delete(Id id, BusinessObjectConfig businessObjectConfig) throws InvalidIdException, ObjectNotFoundException;

    /**
     * Проверяет существует ли бизнес-объект с переданным уникальным
     * идентификатором
     * @param id
     *            идентификатор бизнес-объекта
     * @param businessObjectConfig
     *            конфигурация бизнес-объекта
     * @throws InvalidIdException
     *             если идентификатор бизнес-объекта не корректный (не поддерживается или нулевой)
     * @return true если объект существует иначе возвращает false
     */
    public boolean exists(Id id, BusinessObjectConfig businessObjectConfig) throws InvalidIdException;

    /**
     * Поиск коллекции бизнес-объектов, используя фильтры и сортировку
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции.
     * @param ilterValues значения пераметров фильтров
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества возвращенных бизнес-объектов
     * @return
     */
    IdentifiableObjectCollection findCollection(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, List<Filter> filterValues, SortOrder sortOrder, int offset, int limit);

    /**
     * Поиск количества записей в коллекции бизнес-объектов используя фильтры
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции
     * @return
     */
    int findCollectionCount(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs, List<Filter> filterValues);

    /**
     * Поиск бизнес-объекта по уникальному идентификатору в системе.
     * @param id идентификатору бизнес-объекта
     * @return {@link BusinessObject}
     */
    BusinessObject find(Id id);

    /**
     * Поиск списка бизнес-объектов по уникальным идентификаторам в системе.
     * @param ids уникальные идентификаторы
     * @return {@link List<BusinessObject>}
     */
    List<BusinessObject> find(List<Id> ids);
}
