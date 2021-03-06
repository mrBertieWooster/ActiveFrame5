package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionContext;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionData;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionSettings;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Denis Mitavskiy Date: 23.10.13 Time: 15:10
 */
@ComponentName("start.process.action")
public class StartProcessActionHandler extends ActionHandler<StartProcessActionContext, StartProcessActionData> {

    @Autowired
    private ProcessService processservice;

    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private PermissionService permissionService;
    
    @Override
    public StartProcessActionData executeAction(StartProcessActionContext startProcessActionContext) {
        Id domainObjectId = startProcessActionContext.getRootObjectId();
        if (domainObjectId == null) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_OBJECT_NOT_SAVED,
                    "Объект ещё не сохранён",
                    GuiContext.getUserLocale()));
        }

        // todo: do some action with this domain object or with new domain
        // object
        final ActionConfig actionConfig = (ActionConfig) startProcessActionContext.getActionConfig();
        processservice.startProcess(((StartProcessActionSettings) actionConfig.getActionSettings()).getProcessName(),
                domainObjectId, null);

        //Пересчитываем права чтобы корректно отобразились панель с кнопками
        permissionService.refreshAcls();
        
        // get new form after process start
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(domainObjectId);
        config.setPluginState(startProcessActionContext.getPluginState());
        config.setFormViewerConfig(startProcessActionContext.getFormViewerConfig());
        StartProcessActionData result = new StartProcessActionData();
        result.setFormPluginData(handler.initialize(config));
        return result;
    }

    @Override
    public StartProcessActionContext getActionContext(final ActionConfig actionConfig) {
        return new StartProcessActionContext(actionConfig);
    }
}
