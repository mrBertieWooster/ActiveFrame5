package ru.intertrust.cm.core.gui.model;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.11.13
 *         Time: 14:05
 */
public class GuiUtil {

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1) ;
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
