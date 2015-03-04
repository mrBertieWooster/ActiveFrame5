package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 11:50.
 */
@ComponentName(SimpleActionContext.COMPONENT_NAME)
public class SimpleActionHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired
    private ProfileService profileService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        String locale = profileService.getPersonLocale();
        final List<String> errorMessages =
                PluginHandlerHelper.doServerSideValidation(context.getMainFormState(), applicationContext, locale);
        if (context.getConfirmFormState() != null) {
            errorMessages.addAll(PluginHandlerHelper.doServerSideValidation(
                    context.getConfirmFormState(), applicationContext, locale));
        }
        if (!errorMessages.isEmpty()) {
            throw new ValidationException(buildMessage(LocalizationKeys.SERVER_VALIDATION_EXCEPTION), errorMessages);
        }
        final SimpleActionConfig config = context.getActionConfig();
        final boolean isSaveContext = config.getBeforeConfig() == null
                ? true
                : config.getBeforeConfig().isSaveContext();
        if (isSaveContext) {
            final UserInfo userInfo = GuiContext.get().getUserInfo();
            final FormState mainFormState = context.getMainFormState();
            final FormState confirmFormState = context.getConfirmFormState();
            final DomainObject mainDomainObject;

            List<ValidatorConfig> validators = config.isImmediate() ? null : config.getCustomValidators();
            if (confirmFormState != null) {
                // Если confirmState существует, должен быть и referenceFieldPath
                final FieldPath path = FieldPath.createPaths(
                        config.getBeforeConfig().getLinkedDomainObjectConfig().getReferenceFieldPath())[0];
                if (path.isMultiBackReference()) {
                    throw new GuiException( buildMessage(LocalizationKeys.GUI_EXCEPTION_REF_PATH_NOT_SUPPORTED, new Pair("path", path)));
                }
                final FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver");
                final Map<FieldPath, Value> values = new HashMap<>();
                final DomainObject confirmDomainObject;
                if (path.isOneToOneBackReference()) {
                    mainDomainObject = guiService.saveForm(mainFormState, userInfo, validators);
                    values.put(FieldPath.createPaths(path.getLinkToParentName())[0],
                            new ReferenceValue(mainDomainObject.getId()));
                    formSaver.setContext(confirmFormState, values);
                    confirmDomainObject = formSaver.saveForm();
                } else {
                    confirmDomainObject = guiService.saveForm(confirmFormState, userInfo, null);
                    values.put(path, new ReferenceValue(confirmDomainObject.getId()));
                    PluginHandlerHelper.doCustomServerSideValidation(mainFormState, validators, locale);
                    if (!errorMessages.isEmpty()) {
                        throw new ValidationException(buildMessage(LocalizationKeys.SERVER_VALIDATION_EXCEPTION), errorMessages);
                    }
                    formSaver.setContext(mainFormState, values);
                    mainDomainObject = formSaver.saveForm();
                }
                context.setConfirmDomainObjectId(confirmDomainObject.getId());
            } else {
                mainDomainObject = guiService.saveForm(mainFormState, userInfo,  validators);
            }
            context.setContextSaved();
            context.setRootObjectId(mainDomainObject.getId());

        }
        final SimpleActionData result;
        if (SimpleActionContext.COMPONENT_NAME.equals(config.getActionHandler())) {
            result = new SimpleActionData();
        } else {
            final ActionHandler delegate = (ActionHandler) applicationContext.getBean(config.getActionHandler());
            result = (SimpleActionData) delegate.executeAction(context);
        }
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig formPluginConfig = new FormPluginConfig(context.getRootObjectId());
        formPluginConfig.setPluginState(context.getPluginState());
        formPluginConfig.setFormViewerConfig(context.getViewerConfig());
        final FormPluginData formPluginData = handler.initialize(formPluginConfig);
        result.setPluginData(formPluginData);
        if (config.getAfterConfig() != null) {
            result.setOnSuccessMessage(config.getAfterConfig().getMessageConfig() == null
                    ? null
                    : config.getAfterConfig().getMessageConfig().getText());
        }
        return result;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }

    private String buildMessage(String message) {
        return MessageResourceProvider.getMessage(message, profileService.getPersonLocale());
    }

    private String buildMessage(String message, Pair<String, String>... params) {
        Map<String, String> paramsMap = new HashMap<>();
        for (Pair<String, String> pair  : params) {
            paramsMap.put(pair.getFirst(), pair.getSecond());
        }
        return PlaceholderResolver.substitute(buildMessage(message), paramsMap);
    }
}
