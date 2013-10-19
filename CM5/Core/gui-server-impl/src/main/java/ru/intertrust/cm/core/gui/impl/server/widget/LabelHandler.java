package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.config.model.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:04
 */
@ComponentName("label")
public class LabelHandler extends SingleObjectWidgetHandler {
    @Override
    public LabelState getInitialState(WidgetContext context) {
        FieldPath fieldPath = context.getFieldPath();
        if (fieldPath != null) {
            String plainValue = context.getFieldPathSinglePlainValue();
            return new LabelState(plainValue == null || plainValue.isEmpty() ? "" : plainValue);
        } else {
            LabelConfig widgetConfig = context.getWidgetConfig();
            return new LabelState(widgetConfig.getText());
        }
    }
}
