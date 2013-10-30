package ru.intertrust.cm.core.gui.impl.client.panel;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;

/**
 * Entry point classes define <code>createHeader()</code>
 */
public class HeaderContainer extends SimplePanel {

    public static final int FIRST_ROW = 0;

    public HeaderContainer() {

        this.getElement().setId("container");
        this.getElement().getStyle().setProperty("position", "relative");

        SimplePanel head = createHeadPanel(this);
        FlexTable headTable = createHeadTable();

        TextBox suggestBox = new TextBox();
        suggestBox.setWidth("100%");

        headTable.setWidget(FIRST_ROW, 1, suggestBox);
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 1, "H_td_notes");

        InlineLabel userName = new InlineLabel("Sergey Borisov");
        userName.addStyleName("HeadUserName");
        InlineLabel userPosition = new InlineLabel("Head of department");
        userPosition.addStyleName("HeadUserPost");

        FlowPanel userInfoPanel = new FlowPanel();
        userInfoPanel.add(new SimplePanel(userName));
        userInfoPanel.add(new SimplePanel(userPosition));

        headTable.setWidget(FIRST_ROW, 2, userInfoPanel);
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 2, "H_td_user");

        FlowPanel linksPanel = new FlowPanel();
        Anchor settings = new Anchor("Settings", "Settings");
        linksPanel.add(new SimplePanel(settings));
        Anchor help = new Anchor("Help", "Help");
        linksPanel.add(new SimplePanel(help));

        headTable.setWidget(FIRST_ROW, 3, linksPanel);
        headTable.getCellFormatter().setStyleName(FIRST_ROW, 3, "H_td_links");
        Hyperlink logoutLink = new Hyperlink("Выход", "logout");
        headTable.setWidget(FIRST_ROW, 4, logoutLink);

        logoutLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
               logout();
            }
        });
        headTable.getCellFormatter().setStyleName(FIRST_ROW, 4, "H_td_logout");
        head.add(headTable);
    }

    private void logout(){
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Window.Location.assign("/cm-sochi/Login.html"+ Window.Location.getQueryString());
            }
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error logout!");
            }
        };
        BusinessUniverseAuthenticationServiceAsync.Impl.getInstance().logout(callback);
    }
    private FlexTable createHeadTable() {
        FlexTable headTable = new FlexTable();
        headTable.addStyleName("HeadTable");
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 0, "H_td_logo");
        headTable.setWidget(FIRST_ROW, 0, new Image("logo.gif"));
        return headTable;
    }

    private SimplePanel createHeadPanel(SimplePanel container) {
        SimplePanel head = new SimplePanel();
        container.add(head);
        head.getElement().setId("Head");
        return head;
    }


}
