package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkPluginDefinition;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.List;

@ComponentName("navigation.tree")
public class NavigationTreePlugin extends Plugin implements RootNodeSelectedEventHandler, LeaveLeftPanelEventHandler,
    PluginPanelSizeChangedEventHandler {
  private Integer sideBarOpenningTime;
  protected EventBus eventBus;

  // установка шины событий плагину™
  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public PluginView createView() {
    return new NavigationTreePluginView(this);

  }

  @Override
  public Component createNew() {
    return new NavigationTreePlugin();
  }

  @Override
  public void setInitialData(PluginData initialData) {
    final NavigationTreePluginData data = (NavigationTreePluginData) initialData;
    final HistoryManager historyManager = Application.getInstance().getHistoryManager();
    if (historyManager.hasLink()) {
      final Pair<String, String> historyNavigationItems = getHistoryNavigationItems(data);
      if (historyNavigationItems != null) {
        data.setRootLinkSelectedName(historyNavigationItems.getFirst());
        data.setChildToOpen(historyNavigationItems.getSecond());
      }
    } else if (!historyManager.getSelectedIds().isEmpty()) {
      data.setChildToOpen(null);
    }

    ((NavigationTreePluginData) initialData).setSideBarOpenningTime(sideBarOpenningTime);
    super.setInitialData(initialData);
  }

  @Override
  protected GwtEvent.Type[] getEventTypesToHandle() {
    return new GwtEvent.Type[]{RootLinkSelectedEvent.TYPE, PluginPanelSizeChangedEvent.TYPE};
  }

  @Override
  public void onRootNodeSelected(RootLinkSelectedEvent event) {
    openPluginAndRedrawNavigationTree(event.getSelectedRootLinkName(), null);

  }

  private void openPluginAndRedrawNavigationTree(String linkName, String childToOpen) {
    NavigationTreePluginView pluginView = (NavigationTreePluginView) getView();

    NavigationTreePluginData pluginData = getInitialData();
    LinkConfig linkConfig = findLinkConfig(linkName, pluginData.getNavigationConfig().getLinkConfigList());
    if (linkConfig == null) {
      return;
    }
    if (linkConfig.getChildLinksConfigList().isEmpty()) {
      NavigationTreeItemSelectedEvent event = new NavigationTreeItemSelectedEvent(linkConfig.getPluginDefinition().getPluginConfig(),
          linkConfig.getName(), pluginData.getNavigationConfig());
      Application.getInstance().getEventBus().fireEventFromSource(event, NavigationTreePlugin.this);

    } else {
      pluginView = (NavigationTreePluginView) getView();
      pluginView.repaintNavigationTrees(linkConfig, childToOpen);
    }
  }

  public void clearCurrentSelectedItemValue() {
    ((NavigationTreePluginView) getView()).clearCurrentSelectedItemValue();
  }

  @Override
  public boolean restoreHistory() {
    final HistoryManager historyManager = Application.getInstance().getHistoryManager();
    final NavigationTreePluginView view = (NavigationTreePluginView) getView();
    final String selectedLinkName = view.getSelectedLinkName() == null ? "" : view.getSelectedLinkName();
    //TODO: [CMFIVE-451] commented out to be able to move back from hierarchical link to normal. Need to find a better way.
    //if (!selectedLinkName.equals(historyManager.getLink())) {
    final NavigationTreePluginData data = getInitialData();
    final Pair<String, String> selectedNavigationItems = getHistoryNavigationItems(data);
    if (selectedNavigationItems != null) {
      if (selectedNavigationItems.getSecond() == null || isChildOfRootLink(selectedNavigationItems.getFirst(), selectedNavigationItems.getSecond())) {
        view.showAsSelectedRootLink(selectedNavigationItems.getFirst());
        openPluginAndRedrawNavigationTree(selectedNavigationItems.getFirst(), selectedNavigationItems.getSecond());
      } else {
        navigateToAnyLink(historyManager);
      }
      return true;
    } else {
      if (navigateToAnyLink(historyManager)) {
        return true;
      }
      ApplicationWindow.errorAlert("Пункт меню '" + historyManager.getLink() + "' не найден");
    }
    // }
    return false;
  }

  private boolean isChildOfRootLink(String rootName, String childName) {
    NavigationConfig navigationConfig = getNavigationConfig();
    for (LinkConfig l : navigationConfig.getLinkConfigList()) {
      if (l.getName().equalsIgnoreCase(rootName)) {
        for (ChildLinksConfig c : l.getChildLinksConfigList()) {
          for (LinkConfig k : c.getLinkConfigList()) {
            if (k.getName().equalsIgnoreCase(childName)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * try to find requested link among hierarchical links and emulate tree item selection
   *
   * @param historyManager
   * @return true if navigation successful
   */
  private boolean navigateToAnyLink(HistoryManager historyManager) {
    NavigationConfig navigationConfig = getNavigationConfig();
    for (LinkConfig hierarchicalLink : navigationConfig.getHierarchicalLinkList()) {
      if (hierarchicalLink.getName().equals(historyManager.getLink())) {
        Application.getInstance().getEventBus().fireEvent(new NavigationTreeItemSelectedEvent(
            hierarchicalLink.getPluginDefinition().getPluginConfig(), hierarchicalLink.getName(),
            navigationConfig));
        return true;
      }
    }
    for (LinkConfig hierarchicalLink : navigationConfig.getLinkConfigList()) {
      LinkConfig lC = findChildLinkConfig(hierarchicalLink.getChildLinksConfigList(), historyManager);
      if (lC == null)
        continue;
      LinkPluginDefinition lPlugin = lC.getPluginDefinition();
      if (lPlugin == null) {
        lPlugin = getChildPluginDefinition(lC);
      }
      Application.getInstance().getEventBus().fireEvent(new NavigationTreeItemSelectedEvent(
          lPlugin.getPluginConfig(), historyManager.getLink(),
          navigationConfig));
      return true;
    }
    return false;
  }


  private LinkPluginDefinition getChildPluginDefinition(LinkConfig config) {
    String childToOpen = config.getChildToOpen();
    if (childToOpen != null) {
      for (ChildLinksConfig c : config.getChildLinksConfigList()) {
        for (LinkConfig lnk : c.getLinkConfigList()) {
          if (lnk.getName().equalsIgnoreCase(childToOpen)) {
            return lnk.getPluginDefinition();
          }
        }
      }
    }
    return config.getChildLinksConfigList().get(0).getLinkConfigList().get(0).getPluginDefinition();

  }

  public Pair<String, String> getHistoryNavigationItems(final NavigationTreePluginData data) {
    final HistoryManager historyManager = Application.getInstance().getHistoryManager();
    final List<LinkConfig> linkConfigs = data.getNavigationConfig().getLinkConfigList();
    final Pair<String, String> result = new Pair<>();
    boolean notExists = true;
    for (LinkConfig linkConfig : linkConfigs) {
      final String rootName = linkConfig.getName();
      result.setFirst(rootName);
      if (rootName.equals(historyManager.getLink())) {
        notExists = false;
        break;
      } else {
        final String childLink = findChildLink(linkConfig.getChildLinksConfigList(), historyManager);
        if (childLink != null) {
          notExists = false;
          result.setSecond(childLink);
          break;
        }
      }
    }
    if (notExists) {
      return null;
    } else {
      return result;
    }
  }

  private String findChildLink(final List<ChildLinksConfig> childLinksConfigs, final HistoryManager manager) {
    for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
      final List<LinkConfig> linkConfigs = childLinksConfig.getLinkConfigList();
      for (LinkConfig linkConfig : linkConfigs) {
        if (linkConfig.getName().equals(manager.getLink())) {
          return linkConfig.getName();
        }
        final String childLink = findChildLink(linkConfig.getChildLinksConfigList(), manager);
        if (childLink != null) {
          return childLink;
        }
      }
    }
    return null;
  }

  private LinkConfig findChildLinkConfig(final List<ChildLinksConfig> childLinksConfigs, final HistoryManager manager) {
    for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
      final List<LinkConfig> linkConfigs = childLinksConfig.getLinkConfigList();
      for (LinkConfig linkConfig : linkConfigs) {
        if (linkConfig.getName().equals(manager.getLink())) {
          return linkConfig;
        }
        final LinkConfig childLink = findChildLinkConfig(linkConfig.getChildLinksConfigList(), manager);
        if (childLink != null) {
          return childLink;
        }
      }
    }
    return null;
  }

  public Integer getSideBarOpenningTime() {
    return sideBarOpenningTime;
  }

  public void setSideBarOpenningTime(Integer sideBarOpenningTime) {
    this.sideBarOpenningTime = sideBarOpenningTime;
  }

  @Override
  public void onLeavingLeftPanel(LeaveLeftPanelEvent event) {
    final PluginView view = getView();
    if (view != null) { // LeveLeftPanelEvent event thrown from ru.intertrust.cm.core.gui.impl.client.BusinessUniverse.initialize() when views aren't initialized yet
      ((NavigationTreePluginView) view).onLeavingLeftPanel();
    }
  }

  private LinkConfig findLinkConfig(final String linkName, List<LinkConfig> linkConfigs) {
    return GuiUtil.find(linkConfigs, new Predicate<LinkConfig>() {
      @Override
      public boolean evaluate(LinkConfig input) {
        return input.getName().equalsIgnoreCase(linkName);
      }
    });
  }

  @Override
  public void updateSizes() {
    ((NavigationTreePluginView) (getView())).changeSecondLevelNavigationPanelHeight();
  }
}
