package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public interface HierarchyBrowserNodeClickEventHandler extends EventHandler {
    void onHierarchyBrowserNodeClick(HierarchyBrowserNodeClickEvent event);
}