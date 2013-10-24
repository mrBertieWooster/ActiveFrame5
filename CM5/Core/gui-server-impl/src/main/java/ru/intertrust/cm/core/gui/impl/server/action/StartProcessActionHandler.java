package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionContext;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.10.13
 *         Time: 15:10
 */
@ComponentName("start.process.action")
public class StartProcessActionHandler extends ActionHandler {

    @Override
    public <T extends ActionData> T executeAction(ActionContext context) {
        StartProcessActionContext startProcessActionContext = (StartProcessActionContext) context;
        Id domainObjectId = startProcessActionContext.getRootObjectId();

        // todo: do some action with this domain object

        // get new form after process start
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(domainObjectId);
        SaveActionData result = new SaveActionData();
        result.setFormPluginData((FormPluginData) handler.initialize(config));
        return (T) result;
    }
}
