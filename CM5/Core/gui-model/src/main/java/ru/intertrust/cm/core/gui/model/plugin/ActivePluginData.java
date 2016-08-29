package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.action.infobar.InfoBarContext;

/**
 * Данные плагина, в котором существует панель действий.
 *
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:14
 */
public class ActivePluginData extends PluginData {
    // @defaultUID
    private static final long serialVersionUID = 0L;

    private ToolbarContext toolbarContext;

    private InfoBarContext infoBarContext;

    /**
     * Возвращает список конфигураций действий, отображаемых в "Панели действий"
     * @return список конфигураций действий, отображаемых в "Панели действий"
     */
    public ToolbarContext getToolbarContext() {
        if (toolbarContext == null) {
            toolbarContext = new ToolbarContext();
        }
        return toolbarContext;
    }

    /**
     * Устанавливает список конфигураций действий, отображаемых в "Панели действий"
     * @param toolbarContext список конфигураций действий, отображаемых в "Панели действий"
     */
    public void setToolbarContext(final ToolbarContext toolbarContext) {
        this.toolbarContext = toolbarContext;
    }

    /**
     * Возвращает список конфигураций информационной панели, отображаемых под "Панели действий"
     * @return список элементов информационной панели, отображаемых в "Панели действий"
     */
    public InfoBarContext getInfoBarContext() {
        if (infoBarContext == null) {
            infoBarContext = new InfoBarContext();
        }
        return infoBarContext;
    }

    public void setInfoBarContext(InfoBarContext infoBarContext) {
        this.infoBarContext = infoBarContext;
    }
}
