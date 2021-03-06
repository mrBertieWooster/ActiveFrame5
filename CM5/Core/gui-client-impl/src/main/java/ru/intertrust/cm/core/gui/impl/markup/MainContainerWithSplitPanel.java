package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class MainContainerWithSplitPanel extends SplitLayoutPanel {

    // HTML leftAndUpArea = new HTML("up");
    CmjMenuNavigationUpSection leftAndUpArea = new CmjMenuNavigationUpSection();
    CmjMenuNavigationDownSection rightAndDownArea = new CmjMenuNavigationDownSection();

    public MainContainerWithSplitPanel() {
        // super();
        leftAndUpArea.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        rightAndDownArea.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        this.addNorth(leftAndUpArea, 300);
        this.add(rightAndDownArea);

    }

}
