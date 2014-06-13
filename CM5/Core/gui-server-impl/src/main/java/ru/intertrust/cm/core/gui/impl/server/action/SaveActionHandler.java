package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.impl.server.validation.validators.*;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;
import ru.intertrust.cm.core.gui.model.validation.ValidationMessage;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:18
 */
@ComponentName("save.action")
public class SaveActionHandler extends ActionHandler<SaveActionContext, SaveActionData> {

    @Override
    public SaveActionData executeAction(SaveActionContext context) {
        List<String> errorMessages = doServerSideValidation(context);
        if (!errorMessages.isEmpty()) {
            throw new ValidationException("Server-side validation failed", errorMessages);
        }

        final UserInfo userInfo = GuiContext.get().getUserInfo();
        DomainObject rootDomainObject = guiService.saveForm(context.getFormState(), userInfo);
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(rootDomainObject.getId());
        config.setPluginState(context.getPluginState());
        SaveActionData result = new SaveActionData();
        result.setFormPluginData(handler.initialize(config));
        return result;
    }

    @Override
    public SaveActionContext getActionContext() {
        return new SaveActionContext();
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return new FormPluginHandlerStatusData();
    }

    @Override
    public Status getHandlerStatus(String conditionExpression, HandlerStatusData condition) {
        conditionExpression = conditionExpression.replaceAll(TOGGLE_EDIT_ATTR, TOGGLE_EDIT_KEY);
        final boolean result = evaluateExpression(conditionExpression, condition);
        return result ? Status.SUCCESSFUL : Status.SKIPPED;
    }

    private List<String> doServerSideValidation(ActionContext context) {
        //Simple Server Validation
        List<Constraint> constraints = new ArrayList<Constraint>();
        FormState formState = ((SaveActionContext) context).getFormState();

        for (WidgetState state : formState.getFullWidgetsState().values()) {
            constraints.addAll(state.getConstraints());
        }
        List<String> errorMessages = new ArrayList<String>();
        for (Constraint constraint : constraints) {
            Value valueToValidate = getValueToValidate(constraint, formState);
            ServerValidator validator = createValidator(constraint);
            if (validator != null) {
                validator.init(context);
                ValidationResult validationResult = validator.validate(valueToValidate);
                if (validationResult.hasErrors()) {
                    errorMessages.addAll(getMessages(validationResult, constraint.getParams()));
                }
            }
        }
        // Custom Server Validation
//        if (context.getActionConfig() != null) {
//            for (ValidatorConfig config : context.getActionConfig().getValidatorConfigs()) {
//                String widgetId = config.getWidgetId();
//                ServerValidator customValidator = CustomValidatorFactory.createInstance(config.getClassName(), widgetId);
//                if (customValidator != null) {
//                    WidgetState state = formState.getWidgetState(widgetId);
//                    customValidator.init(context);
//                    ValidationResult validationResult = customValidator.validate(state);
//                    if (validationResult.hasErrors()) {
//                        errorMessages.addAll(getMessages(validationResult, null));
//                    }
//                }
//            }
//        }
        return errorMessages;
    }

    private ServerValidator createValidator(Constraint constraint) {
        switch (constraint.getType()) {
            case SIMPLE:
                return new SimpleValidator(constraint);
            case LENGTH:
                return new LengthValidator(constraint);
            case INT_RANGE:
                return new IntRangeValidator(constraint);
            case DECIMAL_RANGE:
                return new DecimalRangeValidator(constraint);
            case DATE_RANGE:
                return new DateRangeValidator(constraint);
            case SCALE_PRECISION:
                return new ScaleAndPrecisionValidator(constraint);
        }
        return null;
    }

    private WidgetHandler getWidgetHandler(String componentName) {
        return (WidgetHandler) applicationContext.getBean(componentName);
    }

    private Value getValueToValidate(Constraint constraint, FormState formState) {
        String widgetId = constraint.param(Constraint.PARAM_WIDGET_ID);
        String componentName = formState.getWidgetComponent(widgetId);

        WidgetState state = formState.getWidgetState(widgetId);
        if (state != null && componentName != null) {
            WidgetHandler handler = getWidgetHandler(componentName);
            return handler.getValue(state);
        }
        return null;
    }

    private List<String> getMessages(ValidationResult validationResult,  Map<String, String> params) {
        List<String> messages = new ArrayList<String>();
        for (ValidationMessage msg : validationResult.getMessages()) {
            messages.add(getMessageText(msg.getMessage(), params));
        }
        return messages;
    }

    private String getMessageText(String messageKey, Map<String, String> props) {
        if ( MessageResourceProvider.getMessages().get(messageKey) != null) {
            return PlaceholderResolver.substitute(MessageResourceProvider.getMessage(messageKey), props);
        } else {
            return messageKey;//let's return at least messageKey if the message is not found
        }
    }
}
