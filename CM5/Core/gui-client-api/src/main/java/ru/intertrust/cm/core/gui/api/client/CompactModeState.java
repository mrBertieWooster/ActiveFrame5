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
    private boolean rightPanelExpanded;
    private boolean rightPanelConfigured;
    private int secondLevelNavigationPanelWidth;
    private int firstLevelNavigationPanelWidth;
    private int levelsIntersection;

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

    public boolean isRightPanelConfigured() {
        return rightPanelConfigured;
    }

    public void setRightPanelConfigured(boolean rightPanelConfigured) {
        this.rightPanelConfigured = rightPanelConfigured;
    }

    public boolean isRightPanelExpanded() {
        return rightPanelExpanded;
    }

    public void setRightPanelExpanded(boolean rightPanelExpanded) {
        this.rightPanelExpanded = rightPanelExpanded;
    }

    public int getSecondLevelNavigationPanelWidth() {
        return secondLevelNavigationPanelWidth;
    }

    public void setSecondLevelNavigationPanelWidth(int secondLevelNavigationPanelWidth) {
        this.secondLevelNavigationPanelWidth = secondLevelNavigationPanelWidth;
    }

    public int getFirstLevelNavigationPanelWidth() {
        return firstLevelNavigationPanelWidth;
    }

    public void setFirstLevelNavigationPanelWidth(int firstLevelNavigationPanelWidth) {
        this.firstLevelNavigationPanelWidth = firstLevelNavigationPanelWidth;
    }

   public int getFullNavigationPanelWidth(){
       return firstLevelNavigationPanelWidth + secondLevelNavigationPanelWidth - levelsIntersection;
   }

    public void setLevelsIntersection(int levelsIntersection) {
        this.levelsIntersection = levelsIntersection;
    }
}
