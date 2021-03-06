package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.01.14
 *         Time: 13:15
 */
public class StyledDialogBox extends DialogBox {
    private Button okButton;
    private Button cancelButton;
    boolean hideCancelButton;
    public StyledDialogBox(String text){
         init(text);
    }
    public StyledDialogBox(String text, boolean hideCancelButton){
        this.hideCancelButton = hideCancelButton;
        init(text);
    }
    private void init(String text){
        // Enable animation.
        this.setAnimationEnabled(true);
        // Enable glass background.
        this.addStyleName("dialogBoxBody");
        this.removeStyleName("gwt-DialogBox");
        // DialogBox is a SimplePanel, so you have to set its widget
        // property to whatever you want its contents to be.
        okButton= new Button("ОК");
        okButton.addStyleName("darkButton");
        okButton.removeStyleName("gwt-Button");

        Label label = new Label(text);
        label.addStyleName("dialog-box-message");
        label.removeStyleName("gwt-Label");
        AbsolutePanel panel = new AbsolutePanel();
        panel.addStyleName("dialog-box-content");
        SimplePanel header = new SimplePanel();
        header.addStyleName("dialog-box-header");

        panel.add(header);
        panel.add(label);
        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("attachments-buttons-panel");
        buttonsPanel.getElement().getStyle().clearPosition();
        buttonsPanel.add(okButton);
        if(!hideCancelButton){
        cancelButton = new Button("Нет");
        cancelButton.addStyleName("lightButton");
        cancelButton.removeStyleName("gwt-Button");
        buttonsPanel.add(cancelButton);
        }
        panel.add(buttonsPanel);
        this.add(panel);
    }

    public void showDialogBox() {
        this.center();
    }
    public void addOkButtonClickHandler(ClickHandler okClickHandler){
        okButton.addClickHandler(okClickHandler);
    }
    public void addCancelButtonClickHandler(ClickHandler cancelClickHandler){
        if(!hideCancelButton) {
        cancelButton.addClickHandler(cancelClickHandler);
        }
    }
}
