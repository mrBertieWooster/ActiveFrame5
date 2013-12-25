package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.dao.access.AccessToken;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:56 PM
 */
public interface CollectionsDao {

    /**
     * Поиск коллекции доменных объектов, используя фильтры и сортировку
     *
     *
     * @param collectionName конфигурация коллекции
     * @param filterValues значения пераметров фильтров
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества возвращенных доменных объектов
     * @return
     */
    IdentifiableObjectCollection findCollection(String collectionName, List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit, AccessToken accessToken);

    /**
     * Поиск коллекции доменных объектов, используя запрос
     * @param query запрос
     * @param offset смещение
     * @param limit ограничение количества возвращенных доменных объектов
     * @return
     */
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit,
                                                              AccessToken accessToken);

    /**
     * Поиск количества записей в коллекции доменных объектов используя фильтры
     *
     *
     * @param collectionName конфигурация коллекции
     * @return
     */
    int findCollectionCount(String collectionName, List<? extends Filter> filterValues, AccessToken accessToken);

}
