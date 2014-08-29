package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * <p>
 * Действие, которое можно выполнить из плагина при помощи соответствующего элемент пользовательского интерфейса
 * (например, кнопкой или гиперссылкой).
 * </p>
 * <p>
 * Действие является компонентом GUI и должно быть именовано {@link ru.intertrust.cm.core.gui.model.ComponentName}.
 * </p>
 *
 * @author Denis Mitavskiy
 *         Date: 27.08.13
 *         Time: 16:13
 */
public abstract class Action extends BaseComponent {
    /**
     * Плагин, инициирующий данное действие
     */
    protected Plugin plugin;

    /**
     * Конфигурация данного действия
     */
    protected ActionContext initialContext;

    /**
     * Возвращает плагин, инициирующий данное действие
     * @return плагин, инициирующий данное действие
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Устанавливает плагин, инициирующий данное действие
     * @param plugin плагин, инициирующий данное действие
     */
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Возвращает конфигурацию данного действия
     * @return конфигурация данного действия
     */
    public <T extends ActionContext> T getInitialContext() {
        return (T) initialContext;
    }

    /**
     * Устанавливает конфигурацию данного действия
     * @param initialContext конфигурация данного действия
     */
    public void setInitialContext(ActionContext initialContext) {
        this.initialContext = initialContext;
    }

    public final void perform() {
        final ActionConfig config = getInitialContext().getActionConfig();
        final ConfirmCallback checkDirtyCallback = new DirtyExecuteConfirmCallback();
        if (config != null && config.isDirtySensitivity()) {
            Application.getInstance().getActionManager().checkChangesBeforeExecution(checkDirtyCallback);
        } else {
            checkDirtyCallback.onAffirmative();
        }
    }

    public boolean shouldBeValidated() {
        ActionConfig config =
                initialContext.getActionConfig() == null ? null : (ActionConfig) initialContext.getActionConfig();
        return config != null && !config.isImmediate();
    }

    public boolean isValid() {
        return true;
    }

    protected abstract void execute();

    protected void showOnSuccessMessage(final String defaultMessage) {
        final ActionConfig config = getInitialContext().getActionConfig();
        final String onSuccessMessage =
                (config == null || config.getAfterConfig() == null || config.getAfterConfig().getMessageConfig() == null)
                        ? defaultMessage
                        : config.getAfterConfig().getMessageConfig().getText();
        if (onSuccessMessage != null) {
            ApplicationWindow.infoAlert(onSuccessMessage);
        }
    }

    private class DirtyExecuteConfirmCallback implements ConfirmCallback {
        @Override
        public void onAffirmative() {
            final ConfirmCallback beforeExecutionCallback = new BeforeExecutionConfirmationCallback();
            final ActionConfig config = getInitialContext().getActionConfig();
            final String confirmMessage =
                    (config == null || config.getBeforeConfig() == null || config.getBeforeConfig().getMessageConfig() == null)
                            ? null
                            : config.getBeforeConfig().getMessageConfig().getText();
            if (confirmMessage != null) {
                ApplicationWindow.confirm(confirmMessage, beforeExecutionCallback);
            } else {
                beforeExecutionCallback.onAffirmative();
            }
        }

        @Override
        public void onCancel() {
        }
    }


    private class BeforeExecutionConfirmationCallback implements ConfirmCallback {
        @Override
        public void onAffirmative() {
            if (!shouldBeValidated() || isValid()) {
                execute();
            }
        }

        @Override
        public void onCancel() {
        }
    }
}
