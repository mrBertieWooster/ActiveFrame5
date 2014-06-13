package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;

public class DomainObjectSurferPluginData extends ActivePluginData {
    private CollectionPluginData collectionPluginData;
    private FormPluginData formPluginData;
    private DomainObjectSurferConfig domainObjectSurferConfig;

    public DomainObjectSurferConfig getDomainObjectSurferConfig() {
        return domainObjectSurferConfig;
    }

    public void setDomainObjectSurferConfig(DomainObjectSurferConfig domainObjectSurferConfig) {
        this.domainObjectSurferConfig = domainObjectSurferConfig;
    }

    public CollectionPluginData getCollectionPluginData() {
        return collectionPluginData;
    }

    public void setCollectionPluginData(CollectionPluginData collectionPluginData) {
        this.collectionPluginData = collectionPluginData;
    }

    public FormPluginData getFormPluginData() {
        return formPluginData;
    }

    public void setFormPluginData(FormPluginData formPluginData) {
        this.formPluginData = formPluginData;
    }

    @Override
    public ToolbarContext getToolbarContext() {
        final ToolbarContext result = new ToolbarContext();
        result.copyToolbar(collectionPluginData.getToolbarContext());
        result.mergeToolbar(formPluginData.getToolbarContext());
        return result;
    }
}
