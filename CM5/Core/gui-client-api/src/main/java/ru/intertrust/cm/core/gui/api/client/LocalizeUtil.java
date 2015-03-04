package ru.intertrust.cm.core.gui.api.client;

/**
 * @author Lesia Puhova
 *         Date: 27.02.2015
 *         Time: 19:13
 */
public class LocalizeUtil {

    private LocalizeUtil(){} //non-instantiable

    /**
        Returns localized message for current locale. If there's no localized value for the key, returns the key.
     */
    public static String get(String key) {
        return get(key, key);
    }

    public static String get(String key, String defaultValue) {
        String value = Application.getInstance().getLocalizedResources().get(key);
        return value != null ? value : defaultValue;
    }
}
