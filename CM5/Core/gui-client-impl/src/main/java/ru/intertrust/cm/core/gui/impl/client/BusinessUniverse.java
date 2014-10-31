package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.ThemesConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.*;
import ru.intertrust.cm.core.gui.api.client.history.HistoryException;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.action.ActionManagerImpl;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedHandler;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchCompleteEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchCompleteEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventHandler;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse extends BaseComponent implements EntryPoint, NavigationTreeItemSelectedEventHandler {
    private CentralPluginPanel centralPluginPanel;
    private NavigationTreePlugin navigationTreePlugin;
    private PluginPanel navigationTreePanel;
    private int centralPluginWidth;
    private int centralPluginHeight;
    private int stickerPluginWidth = 30;
    private AbsolutePanel left;
    private AbsolutePanel header;
    private AbsolutePanel centrInner;
    private AbsolutePanel action;
    private AbsolutePanel center;
    private AbsolutePanel right;
    private AbsolutePanel footer;
    private String initialToken;
    private HeaderContainer headerContainer;

    CurrentUserInfo getUserInfo(BusinessUniverseInitialization result) {
        return new CurrentUserInfo(result.getCurrentLogin(), result.getFirstName(), result.getLastName(), result.geteMail());
    }

    CurrentVersionInfo getVersion(BusinessUniverseInitialization result) {
        return new CurrentVersionInfo(result.getApplicationVersion(), result.getProductVersion());
    }

    public void onModuleLoad() {
        initialToken = History.getToken();
        final AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandlerImpl());
                final EventBus glEventBus = Application.getInstance().getEventBus();
                SettingsPopupConfig settingsPopupConfig = result.getSettingsPopupConfig();
                ThemesConfig themesConfig = settingsPopupConfig == null ? null : settingsPopupConfig.getThemesConfig();
                GlobalThemesManager.initTheme(themesConfig);
                header = new AbsolutePanel();
                action = new AbsolutePanel();
                left = new AbsolutePanel();
                centrInner = new AbsolutePanel();
                center = new AbsolutePanel();
                right = new AbsolutePanel();
                footer = new AbsolutePanel();
                AbsolutePanel root = new AbsolutePanel();
                final AbsolutePanel centralDivPanel = new AbsolutePanel();
                centralDivPanel.setStyleName("central-div-panel-test");
                centralDivPanel.getElement().setId(ComponentHelper.DOMAIN_ID);
                centralDivPanel.add(right);

                header.setStyleName("header-section");
                header.getElement().getStyle().clearDisplay();
                header.getElement().setId(ComponentHelper.HEADER_ID);
                action.setStyleName("action-section");
                left.setStyleName("left-section-active");
                left.getElement().setId(ComponentHelper.LEFT_ID);
                centrInner.setStyleName("centr-inner-section");
                center.setStyleName("center-section");
                center.getElement().setId(ComponentHelper.CENTER_ID);
                right.setStyleName("right-section");
                footer.setStyleName("footer-section");
                root.setStyleName("root-section");
                root.add(header);
                root.add(center);
                root.add(footer);
                center.add(left);

                center.add(centralDivPanel);
                navigationTreePanel = new PluginPanel();
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно
                navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
                // данному плагину устанавливается глобальная шина событий
                navigationTreePlugin.setEventBus(glEventBus);

                Integer sideBarOpenningTime = result.getSideBarOpenningTimeConfig();
                navigationTreePlugin.setSideBarOpenningTime(sideBarOpenningTime);
                centralPluginPanel = new CentralPluginPanel();
                centralPluginPanel.setStyle("rightSectionWrapper");
                centralDivPanel.add(centralPluginPanel);
                centralPluginWidth = Window.getClientWidth() - 160;
                centralPluginHeight = Window.getClientHeight();
                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                centralPluginPanel.setVisibleHeight(centralPluginHeight);
                glEventBus.addHandler(CentralPluginChildOpeningRequestedEvent.TYPE, centralPluginPanel);
                glEventBus.addHandler(NavigationTreeItemSelectedEvent.TYPE, BusinessUniverse.this);
                String logoImagePath = GlobalThemesManager.getResourceFolder() + result.getLogoImagePath();
                CurrentUserInfo currentUserInfo = getUserInfo(result);
                CurrentVersionInfo version = getVersion(result);
                headerContainer = new HeaderContainer(currentUserInfo, logoImagePath, settingsPopupConfig, version);
                headerContainer.setInfoPage(result.getHelperLink());
                header.add(headerContainer);

                left.add(navigationTreePanel);

                glEventBus.addHandler(SideBarResizeEvent.TYPE, new SideBarResizeEventHandler() {
                    @Override
                    public void sideBarFixPositionEvent(SideBarResizeEvent event) {
                        final CompactModeState compactModeState = Application.getInstance().getCompactModeState();
                        if(!compactModeState.isExpanded()){
                            left.setStyleName(event.getStyleForLeftSector());
                            centralDivPanel.setStyleName(event.getStyleForCenterSector());
                        }
                    }
                });


                // обработчик окончания расширенного поиска
                glEventBus.addHandler(ExtendedSearchCompleteEvent.TYPE, new ExtendedSearchCompleteEventHandler() {
                    @Override
                    public void onExtendedSearchComplete(ExtendedSearchCompleteEvent event) {
                        extendedSearchComplete(event.getDomainObjectSurferPluginData());
                    }
                });
                addStickerPanel();
                addWindowResizeListener();
                final Application application = Application.getInstance();
                application.setPageNamePrefix(result.getPageNamePrefix());
                application.setTimeZoneIds(result.getTimeZoneIds());
                application.setHeaderNotificationPeriod(result.getHeaderNotificationPeriod());
                application.setCollectionCountersUpdatePeriod(result.getCollectionCountersUpdatePeriod());
                application.setActionManager(new ActionManagerImpl(centralPluginPanel));

                RootLayoutPanel.get().add(root);
                RootLayoutPanel.get().getElement().addClassName("root-layout-panel");

               if (initialToken != null && !initialToken.isEmpty()) {
                    Application.getInstance().getHistoryManager().setToken(initialToken);
                }
                History.addValueChangeHandler(new HistoryValueChangeHandler());
                navigationTreePanel.setVisibleWidth(BusinessUniverseConstants.START_SIDEBAR_WIDTH);
                navigationTreePanel.open(navigationTreePlugin);
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught.getMessage() != null && caught.getMessage().contains("LoginPage")) {
                    String queryString = Window.Location.getQueryString() == null ? "" : Window.Location.getQueryString();
                    final StringBuilder loginPathBuilder = new StringBuilder(GWT.getHostPageBaseURL())
                            .append(BusinessUniverseConstants.LOGIN_PAGE).append(queryString);
                    if (initialToken != null && !initialToken.isEmpty()) {
                        loginPathBuilder.append('#').append(initialToken);
                    }
                    Window.Location.assign(loginPathBuilder.toString());
                }
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    @Override
    public void onNavigationTreeItemSelected(NavigationTreeItemSelectedEvent event) {
        final HistoryManager manager = Application.getInstance().getHistoryManager();
        if (manager.hasLink() || manager.getSelectedIds().isEmpty()) {
            Application.getInstance().showLoadingIndicator();
            PluginConfig pluginConfig = event.getPluginConfig();
            String pluginName = pluginConfig.getComponentName();
            final Plugin plugin = ComponentRegistry.instance.get(pluginName);
            plugin.setConfig(pluginConfig);
            manager.setMode(HistoryManager.Mode.WRITE, plugin.getClass().getSimpleName())
                    .setLink(event.getLinkName());
            plugin.setDisplayActionToolBar(true);
            plugin.setNavigationConfig(event.getNavigationConfig());
            navigationTreePlugin.setNavigationConfig(event.getNavigationConfig());
            centralPluginPanel.open(plugin);
        } else {
            History.fireCurrentHistoryState();
        }
    }

    // вывод результатов расширенного поиска
    public void extendedSearchComplete(DomainObjectSurferPluginData domainObjectSurferPluginData) {
        DomainObjectSurferPlugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        domainObjectSurfer.setConfig(domainObjectSurferPluginData.getDomainObjectSurferConfig());
        domainObjectSurfer.setInitialData(domainObjectSurferPluginData);
        domainObjectSurfer.setDisplayActionToolBar(true);

        centralPluginPanel.open(domainObjectSurfer);
    }

    private void addWindowResizeListener() {
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                //вставить запрет ресайза если меньше акшн панели
                //11 - margin для центральной панели
                //int centralPanelWidth = event.getWidth() - navigationTreePanel.getVisibleWidth() - 11;
                //int centralPanelHeight = event.getHeight() - 60 - 11;
                //int centralPanelHeight = event.getHeight() - header.getOffsetHeight() - 11;
                //centralPluginHeight = centralPanelHeight;
                int centralPanelWidth = event.getWidth() - navigationTreePanel.getVisibleWidth() - stickerPluginWidth;
                //int centralPanelHeight = event.getHeight() - 120;
                //60 - header height
                //51 height action panel + margin
//                int centralPanelHeight = event.getHeight() - 9;
                //81 Это высота хеадера (60) + тень хеадера(10) + нижний отступ (11)
                //int centralPanelHeight = event.getHeight() - 81;

                centralPluginPanel.setVisibleWidth(centralPanelWidth);
                //centralPluginPanel.setVisibleHeight(centralPanelHeight);
                centralPluginPanel.asWidget().getElement().getFirstChildElement().addClassName
                        ("central-plugin-panel-table");
                //centrInner.getElement().getStyle().setHeight(centralPanelHeight - 11, Style.Unit.PX);
                Application.getInstance().getEventBus().fireEvent(new PluginPanelSizeChangedEvent());

            }
        });
    }


    private void addStickerPanel() {

        final FlowPanel flowPanel = new FlowPanel();
        final ToggleButton toggleBtn = new ToggleButton("sticker");
        final FocusPanel focusPanel = new FocusPanel();
        toggleBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (toggleBtn.getValue()) {

                    centralPluginWidth -= 300;
                    stickerPluginWidth = 300;
                } else {
                    centralPluginWidth += 300;
                    stickerPluginWidth = 30;
                }

                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                Application.getInstance().getEventBus().fireEvent(new PluginPanelSizeChangedEvent());
            }
        });
        flowPanel.add(toggleBtn);
        focusPanel.add(flowPanel);
        focusPanel.getElement().getStyle().setBackgroundColor("white");
        stickerPluginWidth = 30;

    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }

    private void handleHistory(String url) {
        Application.getInstance().showLoadingIndicator();
        final HistoryManager manager = Application.getInstance().getHistoryManager();
        if (url != null && !url.isEmpty()) {
            manager.setToken(url);
            if (manager.hasLink()) {
                if (!navigationTreePlugin.restoreHistory()) {
                    final Plugin plugin = centralPluginPanel.getCurrentPlugin();
                    plugin.restoreHistory();
                }
            } else if (!manager.getSelectedIds().isEmpty()) {
                final Id selectedId = manager.getSelectedIds().get(0);
                final FormPluginConfig formPluginConfig = new FormPluginConfig(selectedId);
                final FormPluginState formPluginState = new FormPluginState();
                formPluginState.setInCentralPanel(Application.getInstance().getCompactModeState().isExpanded());
                formPluginConfig.setPluginState(formPluginState);

                final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
                formPlugin.setConfig(formPluginConfig);
                formPlugin.setDisplayActionToolBar(true);
                formPlugin.setLocalEventBus((EventBus) GWT.create(SimpleEventBus.class));
                manager.setMode(HistoryManager.Mode.WRITE, FormPlugin.class.getSimpleName());
                navigationTreePlugin.clearCurrentSelectedItemValue();
                Window.setTitle("Форма документа");
                centralPluginPanel.open(formPlugin);
            } else {
                throw new HistoryException("Переход по данным '" + url + "' невозможен");
            }
        }
        Application.getInstance().hideLoadingIndicator();
    }

    private class HistoryValueChangeHandler implements ValueChangeHandler<String> {

        @Override
        public void onValueChange(final ValueChangeEvent<String> event) {
            if (!"logout".equals(event.getValue())) {
                final HistoryManager manager = Application.getInstance().getHistoryManager();
                ActionManager actionManager = Application.getInstance().getActionManager();
                actionManager.checkChangesBeforeExecution(new ConfirmCallback() {

                    @Override
                    public void onAffirmative() {
                        handleHistory(event.getValue());
                    }

                    @Override
                    public void onCancel() {
                        manager.applyUrl();
                    }
                });
            }
        }
    }

    private static class CentralPluginPanel extends PluginPanel implements CentralPluginChildOpeningRequestedHandler {
        @Override
        public void openChildPlugin(CentralPluginChildOpeningRequestedEvent event) {
            final Plugin child = event.getOpeningChildPlugin();
            this.openChild(child);
        }
    }

    private static class UncaughtExceptionHandlerImpl implements GWT.UncaughtExceptionHandler {
        @Override
        public void onUncaughtException(Throwable ex) {
            Application.getInstance().hideLoadingIndicator();
            final String message;
            if (ex.getCause() instanceof HistoryException) {
                message = ex.getCause().getMessage();
            } else if (ex instanceof GuiException) {
                message = ex.getMessage();
            } else {
                GWT.log("Uncaught exception escaped", ex);
                message = null;
            }
            if (message != null) {
                ApplicationWindow.errorAlert(message);
            }
        }
    }
}
