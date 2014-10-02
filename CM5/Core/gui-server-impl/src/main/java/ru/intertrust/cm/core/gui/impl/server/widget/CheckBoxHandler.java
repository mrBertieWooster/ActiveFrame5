package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CheckBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 12:41
 */
@ComponentName("check-box")
public class CheckBoxHandler extends ValueEditingWidgetHandler {
    @Override
    public CheckBoxState getInitialState(WidgetContext context) {
        CheckBoxState checkBoxState;
        if (context.getDefaultValue() == null) {
            checkBoxState = new CheckBoxState(context.<Boolean>getFieldPlainValue());
        } else {
            checkBoxState = new CheckBoxState(((BooleanValue) context.getDefaultValue()).get());
        }
        return checkBoxState;
    }

    @Override
    public Value getValue(WidgetState state) {
        final boolean booleanValue = ((CheckBoxState) state).isSelected();
        return new BooleanValue(booleanValue);
    }
}
