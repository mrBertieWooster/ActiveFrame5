package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.util.GuiDateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 0:48
 */
public abstract class RangeDatePicker extends DatePickerPopup {
    public RangeDatePicker(Date startDate, Date endDate, EventBus eventBus, boolean showTime, boolean showSeconds) {
        super(eventBus);
        initWidget(startDate, endDate, showTime, showSeconds);
    }

    protected abstract void initWidget(Date startDate, Date endDate, boolean showTime, boolean showSeconds);

    protected abstract Panel initDatePickerPanel(final DateTimePicker startDateTimePicker, final DateTimePicker endDateTimePicker);

    protected Panel initDateSelector(String dateDescription, ClickHandler handler) {
        Panel panel = new AbsolutePanel();
        panel.setStyleName("composite-datetime-selection-item");
        Label label = new Label(dateDescription);
        panel.add(label);
        panel.addDomHandler(handler, ClickEvent.getType());
        return panel;

    }

    protected Panel initDateSelectorWithPicker(String dateDescription) {
        Panel panel = new AbsolutePanel();
        panel.setStyleName("composite-datetime-selection-item");
        Label label = new Label(dateDescription);
        panel.add(label);
        return panel;

    }

    protected List<Date> getRequiredDate(String dateDescription) {
        Date startDate = new Date();
        Date endDate = new Date();

        switch (dateDescription) {
            case FOR_TODAY_LABEL:
                GuiDateUtil.setStartOfDay(startDate);
                return getRangeDateList(startDate, endDate);

            case FOR_YESTERDAY_LABEL:
                CalendarUtil.addDaysToDate(startDate, -1);
                GuiDateUtil.setStartOfDay(startDate);
                CalendarUtil.addDaysToDate(endDate, -1);
                GuiDateUtil.setEndOfDay(endDate);
                return getRangeDateList(startDate, endDate);

            case FOR_LAST_WEEK_LABEL:
                CalendarUtil.addDaysToDate(startDate, -6);
                GuiDateUtil.setStartOfDay(startDate);
                return getRangeDateList(startDate, endDate);

            case FOR_LAST_YEAR_LABEL:
                CalendarUtil.addDaysToDate(startDate, -364);
                GuiDateUtil.setStartOfDay(startDate);
                return getRangeDateList(startDate, endDate);


        }
        return null;
    }

    private List<Date> getRangeDateList(Date startDate, Date endDate) {
        List<Date> dates = new ArrayList<Date>();
        dates.add(startDate);
        dates.add(endDate);
        return dates;
    }

    protected class DatetimeClickHandler implements ClickHandler {
        private Panel container;

        protected DatetimeClickHandler(Panel container) {
            this.container = container;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            container.setStyleName("compositeDateTimeContainerShown");

        }
    }


}
