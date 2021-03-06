package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:00
 */
@ComponentName("integer-box")
public class IntegerBoxHandler extends ValueEditingWidgetHandler {
    @Override
    public IntegerBoxState getInitialState(WidgetContext context) {
        return new IntegerBoxState(context.<Long>getFieldPlainValue());
    }

    @Override
    public Value getValue(WidgetState state) {
        return new LongValue(((IntegerBoxState) state).getNumber());
    }
}
