package ru.intertrust.cm.core.dao.access;

/**
 * Интерфейс, реализуемый маркерами доступа.
 * 
 * Управление маркерами доступа &mdash; их создание и проверку &mdash; осуществляет служба контроля доступа
 * ({@link AccessControlService}). Она работает только с объектами, созданными ей самой.
 * Реализация этого интерфейса другими классами не имеет смысла.
 * 
 * @author apirozhkov
 */
public interface AccessToken {

    /**
     * Тип ограничения прав доступа
     */
    public enum AccessLimitationType {LIMITED, UNLIMITED}

    /**
     * Возвращает субъект доступа. Это может быть пользователь ({@link UserSubject})
     * или системный процесс ({@link SystemSubject}).
     * 
     * @return Субъект доступа
     */
    Subject getSubject();

    //DomainObject getObject();

    //AccessType getAccessType();

    /**
     * Определяет, является ли маркер доступа отложенным.
     * <p>Для маркера, не являющегося отложенным, сам факт его существования говорит о том, что проверка
     * соответствующих прав прошла успешно. Для отложенных маркеров проверка прав доступа производится
     * одновременно с выполнением операции и может привести к возникновению ошибки доступа.
     * <p>Обязанностью базовых сервисов при получении отложенного маркера доступа является осуществление
     * физической проверки прав доступа через запросы к БД. Рекомендуется совмещать эти запросы с запросами,
     * выполняющими собственно операцию.
     * 
     * @return true, если маркер доступа является отложенным
     */
    boolean isDeferred();

    /**
     * Возвращает тип ограничения прав доступа, который представляет данный токен
     * @return тип ограничения прав доступа, который представляет данный токен
     */
    AccessLimitationType getAccessLimitationType();
}
