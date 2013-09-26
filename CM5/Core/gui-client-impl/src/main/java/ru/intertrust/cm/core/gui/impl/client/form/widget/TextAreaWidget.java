package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextAreaData;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 10:59
 */
@ComponentName("text-area")
public class TextAreaWidget extends TextBoxWidget {
    @Override
    public TextAreaWidget createNew() {
        return new TextAreaWidget();
    }

    @Override
    public TextAreaData getCurrentState() {
        TextAreaData data = new TextAreaData();
        data.setText(getTrimmedText((HasText) impl));
        return data;
    }

    @Override
    protected Widget asEditableWidget() {
        return new TextArea();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }

}
