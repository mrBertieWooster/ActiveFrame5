package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.DeleteCollectionRowEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 *         Created on 22.09.2014 13:21.
 */
@ComponentName(SimpleActionContext.COMPONENT_NAME)
public class SimpleAction extends SimpleServerAction {

    @Override
    protected ActionContext appendCurrentContext(ActionContext initialContext) {
        final SimpleActionContext context = (SimpleActionContext) initialContext;
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        if(editor instanceof DomainObjectSurferPlugin){
            CollectionPlugin cPlugin = ((DomainObjectSurferPlugin)editor).getCollectionPlugin();
            if(cPlugin.getChangedRowsState()!=null){
                context.getObjectsIds().clear();
                context.setCollectionName(((CollectionViewerConfig)cPlugin.getConfig()).getCollectionRefConfig().getName());
                for(Id id : cPlugin.getChangedRowsState().keySet()){
                    if(cPlugin.getChangedRowsState().get(id)){
                        context.getObjectsIds().add(id);
                    }
                }
            }
        }
        context.setRootObjectId(editor.getRootDomainObject().getId());
        context.setMainFormState(editor.getFormState());
        context.setPluginState(editor.getFormPluginState());
        return initialContext;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((SimpleActionData) result).getPluginData();
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        if(((SimpleActionData) result).getDeleteAction() &&
            ((SimpleActionData) result).getDeletedObject()!=null){
            plugin.refresh();
            return;
        }

        if (plugin.getLocalEventBus() != null) {
            editor.setFormState(formPluginData.getFormDisplayData().getFormState());
            editor.setFormToolbarContext(formPluginData.getToolbarContext());
            plugin.getLocalEventBus().fireEvent(new UpdateCollectionEvent(
                    formPluginData.getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject()));
        }

        if(((SimpleActionData) result).getUrlToOpen()!=null){
            getURL(((SimpleActionData) result).getUrlToOpen());
        }


    }

    @Override
    public Component createNew() {
        return new SimpleAction();
    }

    public static native String getURL(String url)/*-{
        return $wnd.open(url,
            'target=_blank')
    }-*/;
}
