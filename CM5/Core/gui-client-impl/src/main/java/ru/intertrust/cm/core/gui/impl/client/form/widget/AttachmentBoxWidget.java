package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.attachment.AttachmentUploaderView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.10.13
 *         Time: 13:15
 */
@ComponentName("attachment-box")
public class AttachmentBoxWidget extends BaseWidget {
    protected static final BusinessUniverseServiceAsync SERVICE = BusinessUniverseServiceAsync.Impl.getInstance();
    private Timer elapsedTimer;
    private boolean dontShowNewRow;

    @Override
    public Component createNew() {
        return new AttachmentBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        AttachmentBoxState state = (AttachmentBoxState) currentState;

        List<AttachmentItem> attachments = state.getAttachments();
        String selectionStyle = state.getSelectionStyle();

        AttachmentUploaderView view = (AttachmentUploaderView) impl;
        view.setAttachments(attachments);
        view.initDisplayStyle(selectionStyle);
        view.cleanUp();
        for (AttachmentItem attachmentItem : attachments) {
            view.displayAttachmentLinkItem(attachmentItem);
        }

    }

    @Override
    public WidgetState getCurrentState() {
        AttachmentBoxState state = new AttachmentBoxState();
        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        List<AttachmentItem> attachments = attachmentUploaderView.getAttachments();
        state.setAttachments(attachments);

        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        AttachmentUploaderView attachmentUploaderView = new AttachmentUploaderView();
        attachmentUploaderView.addFormSubmitCompleteHandler(new FormSubmitCompleteHandler());
        attachmentUploaderView.addFormSubmitHandler(new FormSubmitHandler());
        return attachmentUploaderView;
    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }

    private AttachmentItem handleFileNameFromServer(String filePath) {
        AttachmentItem attachmentItem = new AttachmentItem();
        String[] splitClearName = filePath.split("-_-");
        String clearName = splitClearName[1];
        attachmentItem.setTemporaryName(filePath);
        attachmentItem.setName(clearName);
        ((AttachmentUploaderView) impl).getAttachments().add(attachmentItem);

        return attachmentItem;
    }

    private void setUpProgressOfUpload(final boolean isUploadCancel) {

        if (isUploadCancel) {
            return;
        }
        SERVICE.getAttachmentUploadPercentage(isUploadCancel, new AsyncCallback<AttachmentUploadPercentage>() {

            @Override
            public void onFailure(final Throwable t) {
                cancelTimer();
            }

            @Override
            public void onSuccess(AttachmentUploadPercentage percentage) {
                Integer percentageValue = percentage.getPercentage();
                if (percentageValue == 100) {
                    cancelTimer();
                }
                AttachmentUploaderView view = (AttachmentUploaderView) impl;
                view.getPercentage().setText(percentageValue + "%");
            }
        });
    }

    private class FormSubmitHandler implements FormPanel.SubmitHandler {

        @Override
        public void onSubmit(FormPanel.SubmitEvent event) {

            AttachmentUploaderView view = (AttachmentUploaderView) impl;
            String browserFilename = view.getFileUpload().getFilename();

            String filename = getFilename(browserFilename);
            AttachmentItem item = new AttachmentItem();
            item.setName(filename);
            view.displayAttachmentItem(item, new CancelUploadAttachmentHandler(item));
            elapsedTimer = new Timer() {
                public void run() {
                    setUpProgressOfUpload(false);
                }
            };
            elapsedTimer.scheduleRepeating(100);

        }
    }

    private class FormSubmitCompleteHandler implements FormPanel.SubmitCompleteHandler {

        public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {

            if (dontShowNewRow) {
                dontShowNewRow = false;
                return;
            }
            String filePath = event.getResults();

            AttachmentItem item = handleFileNameFromServer(filePath);
            AttachmentUploaderView view = (AttachmentUploaderView) impl;
            view.getPercentage().getParent().removeFromParent();    //removing attachment with progress
            view.displayAttachmentLinkItem(item);
            cancelTimer();
            view.getProgressbar().getElement().removeFromParent();

        }
    }

    public class CancelUploadAttachmentHandler implements ClickHandler {

        AttachmentItem item;

        public CancelUploadAttachmentHandler(AttachmentItem item) {
            this.item = item;

        }

        @Override
        public void onClick(ClickEvent event) {
            if (item.getTemporaryName() == null && item.getId() == null) {
                dontShowNewRow = true;
                setUpProgressOfUpload(true);
                cancelTimer();

            }

        }
    }

    private String getFilename(String browserFilename) {

        if (browserFilename.contains("/")) {
            String[] split = browserFilename.split("/");
            return split[split.length - 1];
        }
        if (browserFilename.contains("\\")) {
            String[] split = browserFilename.split("\\\\");
            return split[split.length - 1];
        }
        return browserFilename;
    }

    private void cancelTimer() {
        if (elapsedTimer != null) {
            elapsedTimer.cancel();
            elapsedTimer = null;
        }
    }
}