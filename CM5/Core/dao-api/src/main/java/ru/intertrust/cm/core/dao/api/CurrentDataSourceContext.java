package ru.intertrust.cm.core.dao.api;

/**
 * Итерфейс для получения/установления контекста используемого источника данных
 * Created by vmatsukevich on 8.4.15.
 */
public interface CurrentDataSourceContext {

    /**
     * Возвращает контекст используемого источника данных
     * @return контекст используемого источника данных
     */
    String get();

    /**
     * Возвращает описание используемого источника данных
     * @return
     */
    String getDescription();

    /**
     * Устанавливает контекст используемого источника данных
     */
    void set(String context);

    /**
     * Устанавливает контекст используемого источника данных в источник данных коллекций
     */
    void setToCollections();

    /**
     * Устанавливает контекст используемого источника данных в источник данных отчетов
     */
    void setToReports();

    /**
     * Устанавливает контекст используемого источника данных в основной источник данных
     */
    void setToMaster();

    /**
     * Возвращает true, если текущий контекст источника данных является основным (Master)
     * @return
     */
    boolean isMaster();

    /**
     * Оменяет установленный контекст источника данных
     */
    void reset();
}
