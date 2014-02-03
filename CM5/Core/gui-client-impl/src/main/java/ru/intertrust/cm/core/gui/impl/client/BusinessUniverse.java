package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.logging.Logger;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse extends BaseComponent implements EntryPoint, NavigationTreeItemSelectedEventHandler {
    static Logger logger = Logger.getLogger("Business universe");
    // глобальная шина событий - доступна во всем приложении
    private static EventBus eventBus = Application.getInstance().getEventBus();
    private CentralPluginPanel centralPluginPanel;
    NavigationTreePlugin navigationTreePlugin;
    PluginPanel navigationTreePanel;
    private int centralPluginWidth;
    private int centralPluginHeight;
    private int stickerPluginWidth = 30;
    private AbsolutePanel left;
    private AbsolutePanel header;
    private AbsolutePanel centrInner;

    CurrentUserInfo getUserInfo(BusinessUniverseInitialization result) {
        return new CurrentUserInfo(result.getCurrentLogin(), result.getFirstName(), result.getLastName(), result.geteMail());
    }

    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                header = new AbsolutePanel();
                header.setStyleName("header-section");
                header.getElement().setId(ComponentHelper.HEADER_ID);
                AbsolutePanel action = new AbsolutePanel();
                action.setStyleName("action-section");

                left = new AbsolutePanel();
                left.setStyleName("left-section-active");
                left.getElement().getStyle().setWidth(130, Style.Unit.PX);
                left.getElement().setId(ComponentHelper.LEFT_ID);
                centrInner = new AbsolutePanel();
                centrInner.setStyleName("centr-inner-section");
                centrInner.getElement().getStyle().setLeft(130, Style.Unit.PX);

                AbsolutePanel center = new AbsolutePanel();
                center.setStyleName("center-section");
                center.getElement().setId(ComponentHelper.CENTER_ID);
                AbsolutePanel right = new AbsolutePanel();
                right.setStyleName("right-section");
                AbsolutePanel footer = new AbsolutePanel();
                footer.setStyleName("footer-section");
                AbsolutePanel root = new AbsolutePanel();
                root.setStyleName("root-section");
                root.addStyleName("content");

                root.add(header);
                root.add(center);
                root.add(footer);

                centrInner.add(action);
                centrInner.add(right);


                center.add(left);
                center.add(centrInner);

                navigationTreePanel = new PluginPanel();
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно

                navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
                // данному плагину устанавливается глобальная шина событий
                navigationTreePlugin.setEventBus(eventBus);

                centralPluginPanel = new CentralPluginPanel();
                //11 - отступ справа
                centralPluginWidth = Window.getClientWidth() - 130 - 11;
                // header 60 ;
                // action panel 51
                //centralPluginHeight = Window.getClientHeight()- 9 ;
                centralPluginHeight = Window.getClientHeight() - 120;
                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                centralPluginPanel.setVisibleHeight(centralPluginHeight);
                eventBus.addHandler(CentralPluginChildOpeningRequestedEvent.TYPE, centralPluginPanel);
                eventBus.addHandler(NavigationTreeItemSelectedEvent.TYPE, BusinessUniverse.this);
                navigationTreePanel.setVisibleWidth(130);
                navigationTreePanel.open(navigationTreePlugin);
                String logoImagePath = result.getLogoImagePath();
                CurrentUserInfo currentUserInfo = getUserInfo(result);
                header.add(new HeaderContainer(currentUserInfo, logoImagePath));
                action.add(centralPluginPanel);
                left.add(navigationTreePanel);
                left.setHeight(Window.getClientHeight() + "px");

                eventBus.addHandler(SideBarResizeEvent.TYPE, new SideBarResizeEventHandler() {
                    @Override
                    public void sideBarFixPositionEvent(SideBarResizeEvent event) {
                        centralPluginWidth = Window.getClientWidth() - event.getSideBarWidts();
                        //11 отступ справа
                        centralPluginPanel.setVisibleWidth(centralPluginWidth - 11);

                        centrInner.getElement().getStyle().setLeft(event.getSideBarWidts(), Style.Unit.PX);
                        //60 - высота хеадера
                        //11 - отступ снизу

                        left.setStyleName("left-section-active");
                        //centrInner.getElement().getStyle().setHeight(Window.getClientHeight()-60 - 11, Style.Unit.PX);
                        eventBus.fireEvent(new PluginPanelSizeChangedEvent());
                        //centrInner.getElement().getStyle().setHeight(Window.getClientHeight() -60 - 11 - 11, Style.Unit.PX);
                        centrInner.getElement().getStyle().setHeight(Window.getClientHeight() -60 - 11 - 11, Style.Unit.PX);
                    }
                });

                eventBus.addHandler(SideBarResizeEventStyle.TYPE, new SideBarResizeEventStyleHandler() {
                    @Override
                    public void sideBarSetStyleEvent(SideBarResizeEventStyle event) {
                        //left.removeStyleName();
                        left.setStyleName(event.getStyleMouseOver());
                        left.getElement().getStyle().clearWidth();
                    }
                });

                // обработчик окончания расширенного поиска
                eventBus.addHandler(ExtendedSearchCompleteEvent.TYPE, new ExtendedSearchCompleteEventHandler() {
                    @Override
                    public void onExtendedSearchComplete(ExtendedSearchCompleteEvent event) {
                        extendedSearchComplete(event.getDomainObjectSurferPluginData());
                    }
                });

                addStickerPanel(root);
                centralPluginPanel.setSize("100%", "100%");
                //centralPluginPanel.asWidget().getElement().addClassName("hello999");
                centrInner.add(centralPluginPanel);
                addWindowResizeListener();
                RootLayoutPanel.get().add(root);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.assign("/cm-sochi/Login.html" + Window.Location.getQueryString());
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    @Override
    public void onNavigationTreeItemSelected(NavigationTreeItemSelectedEvent event) {
        final DomainObjectSurferPlugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        domainObjectSurfer.setConfig(event.getPluginConfig());
        domainObjectSurfer.setDisplayActionToolBar(true);
        centralPluginPanel.open(domainObjectSurfer);

    }

    // вывод результатов расширенного поиска
    public void extendedSearchComplete(DomainObjectSurferPluginData domainObjectSurferPluginData) {
        DomainObjectSurferPlugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        domainObjectSurfer.setConfig(domainObjectSurferPluginData.getDomainObjectSurferConfig());
        domainObjectSurfer.setInitialData(domainObjectSurferPluginData);
        domainObjectSurfer.setDisplayActionToolBar(true);

        centralPluginPanel.open(domainObjectSurfer);
    }

    private HorizontalPanel createToolPanel() {
        HorizontalPanel toolPanel = new HorizontalPanel();
        toolPanel.setStyleName("content-tools");
        toolPanel.setWidth("100%");
        return toolPanel;
    }

    private DockLayoutPanel createRootPanel() {
        DockLayoutPanel rootPanel = new DockLayoutPanel(Style.Unit.PX);
        rootPanel.setStyleName("content");
        return rootPanel;
    }

    private void addWindowResizeListener() {
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                //вставить запрет ресайза если меньше акшн панели
                //11 - margin для центральной панели
                int centralPanelWidth = event.getWidth() - navigationTreePanel.getVisibleWidth() - 11;
                //int centralPanelHeight = event.getHeight() - 60 - 11;
                int centralPanelHeight = event.getHeight() - header.getOffsetHeight() - 11;
                centralPluginHeight = centralPanelHeight;
                //int centralPanelWidth = event.getWidth() - navigationTreePanel.getVisibleWidth() - stickerPluginWidth;
                //int centralPanelHeight = event.getHeight() - 120;
                //60 - header height
                //51 height action panel + margin
//                int centralPanelHeight = event.getHeight() - 9;
                //81 Это высота хеадера (60) + тень хеадера(10) + нижний отступ (11)
                //int centralPanelHeight = event.getHeight() - 81;

                centralPluginPanel.setVisibleWidth(centralPanelWidth);
                centralPluginPanel.setVisibleHeight(centralPanelHeight);
                centralPluginPanel.asWidget().getElement().getFirstChildElement().addClassName("central-plugin-panel-table");
                centrInner.getElement().getStyle().setHeight(centralPanelHeight - 11, Style.Unit.PX);
                eventBus.fireEvent(new PluginPanelSizeChangedEvent());
                centrInner.getElement().getStyle().setHeight(centralPanelHeight - 11, Style.Unit.PX);
                left.getElement().getStyle().setHeight(centralPanelHeight + 11, Style.Unit.PX);
                centrInner.getElement().getStyle().setWidth(event.getWidth() - navigationTreePanel.getVisibleWidth() - 11, Style.Unit.PX);
            }
        });
    }

    private void addStickerPanel(final AbsolutePanel mainLayoutPanel) {

        final FlowPanel flowPanel = new FlowPanel();
        final ToggleButton toggleBtn = new ToggleButton("sticker");
        final FocusPanel focusPanel = new FocusPanel();
        toggleBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (toggleBtn.getValue()) {
                    //mainLayoutPanel.setWidgetSize(focusPanel, 300);
                    centralPluginWidth -= 300;
                    stickerPluginWidth = 300;
                } else {
                    //mainLayoutPanel.setWidgetSize(focusPanel, 30);
                    centralPluginWidth += 300;
                    stickerPluginWidth = 30;
                }

                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                eventBus.fireEvent(new PluginPanelSizeChangedEvent());
            }
        });


        focusPanel.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                //mainLayoutPanel.setWidgetSize(focusPanel, 300);
            }
        });

        focusPanel.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (toggleBtn.getValue()) {
                    return;
                }
                //mainLayoutPanel.setWidgetSize(focusPanel, 30);
                //mainLayoutPanel.setWidgetSize(focusPanel, stickerPluginWidth);

            }
        });

        flowPanel.add(toggleBtn);

        focusPanel.add(flowPanel);
        focusPanel.getElement().getStyle().setBackgroundColor("white");


        // mainLayoutPanel.addEast(focusPanel, 30);
        stickerPluginWidth = 30;
        //mainLayoutPanel.addEast(focusPanel, stickerPluginWidth);

    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }

    private static class CentralPluginPanel extends PluginPanel implements CentralPluginChildOpeningRequestedHandler {
        @Override
        public void openChildPlugin(CentralPluginChildOpeningRequestedEvent event) {
            final Plugin child = event.getOpeningChildPlugin();
            this.openChild(child);
        }
    }

}
