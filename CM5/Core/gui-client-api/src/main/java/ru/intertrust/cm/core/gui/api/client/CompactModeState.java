package ru.intertrust.cm.core.gui.api.client;

/**
 * Defines full screen state and recovery point for all plugins which support full screen mode
 *
 * @author Sergey.Okolot
 *         Created on 23.01.14 9:38.
 */
public class CompactModeState {

    private int left;
    private boolean expanded;
    private boolean navigationTreePanelExpanded;

    public boolean isNavigationTreePanelExpanded() {
        return navigationTreePanelExpanded;
    }

    public void setNavigationTreePanelExpanded(boolean navigationTreePanelExpanded) {
        this.navigationTreePanelExpanded = navigationTreePanelExpanded;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
