package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:47
 */
@ComponentName("integer-box")
public class IntegerBoxWidget extends TextBoxWidget {
    @Override
    public Component createNew() {
        return new IntegerBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        Long value = ((IntegerBoxState) currentState).getNumber();
        setTrimmedText((HasText) impl, value == null ? "" : value.toString());
    }

    @Override
    protected boolean isChanged() {
        final Long initLong = ((IntegerBoxState) getInitialData()).getNumber();
        final String initValue = initLong == null ? null : initLong.toString().trim();
        final String currentValue = getTrimmedText((HasText) impl);
        return initValue == null ? currentValue != null : !initValue.equals(currentValue);
    }

    @Override
    protected WidgetState createNewState() {
        IntegerBoxState data = new IntegerBoxState();
        String text = getTrimmedText((HasText) impl);
        if (text == null) {
            return data;
        }
        Long value;
        try {
            value = Long.parseLong(text);
            data.setValue(value);
            return data;
        } catch (NumberFormatException e) {
            throw new GuiException(getMessageText("validate.integer"));
        }
    }
}
