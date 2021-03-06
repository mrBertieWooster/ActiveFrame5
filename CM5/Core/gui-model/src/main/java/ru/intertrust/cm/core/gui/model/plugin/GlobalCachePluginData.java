package ru.intertrust.cm.core.gui.model.plugin;


/**
 * Класс содержит модель данных для плагина статистики
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 27.10.2015
 */
public class GlobalCachePluginData extends PluginData {

    private GlobalCacheControlPanel controlPanelModel;
    private GlobalCacheStatPanel statPanel;
    private Boolean superUser = false;

    private String errorMsg;


    public GlobalCacheStatPanel getStatPanel() {
        return statPanel;
    }

    public void setStatPanel(GlobalCacheStatPanel statPanel) {
        this.statPanel = statPanel;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public GlobalCacheControlPanel getControlPanelModel() {
        if(controlPanelModel==null)
            controlPanelModel = new GlobalCacheControlPanel();
        return controlPanelModel;
    }

    public void setControlPanelModel(GlobalCacheControlPanel controlPanelModel) {
        this.controlPanelModel = controlPanelModel;
    }

    public GlobalCachePluginData(){}

    public Boolean isSuperUser() {
        return superUser;
    }

    public void setSuperUser(Boolean superUser) {
        this.superUser = superUser;
    }
}
