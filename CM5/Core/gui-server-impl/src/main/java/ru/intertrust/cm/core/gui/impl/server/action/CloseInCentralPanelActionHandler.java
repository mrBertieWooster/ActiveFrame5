package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Sergey.Okolot
 *         Created on 12.06.2014 16:24.
 */
@ComponentName("close.in.central.panel.action")
public class CloseInCentralPanelActionHandler extends ActionHandler<ActionContext, ActionData> {
    @Override
    public ActionData executeAction(final ActionContext context) {
        return null;
    }

    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new ActionContext(actionConfig);
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return new FormPluginHandlerStatusData();
    }
}
