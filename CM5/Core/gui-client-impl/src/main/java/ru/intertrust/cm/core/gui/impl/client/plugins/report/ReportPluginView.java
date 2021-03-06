package ru.intertrust.cm.core.gui.impl.client.plugins.report;

import com.google.gwt.user.client.ui.IsWidget;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:53
 */
public class ReportPluginView extends PluginView {

    private FormPanel formPanel;

    ReportPluginView(Plugin plugin, FormDisplayData formDisplayData) {
        super(plugin);
        final FormPluginState state = new FormPluginState();
        formPanel = new FormPanel(formDisplayData, state, Application.getInstance().getEventBus());
    }

    @Override
    public IsWidget getViewWidget() {
        return formPanel;
    }
}
