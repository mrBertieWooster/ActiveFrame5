package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserCheckBoxUpdateEvent extends GwtEvent<HierarchyBrowserCheckBoxUpdateEventHandler> {

    public static Type<HierarchyBrowserCheckBoxUpdateEventHandler> TYPE = new Type<HierarchyBrowserCheckBoxUpdateEventHandler>();
    private HierarchyBrowserItem item;
    private boolean removeItemOnly;

    public HierarchyBrowserCheckBoxUpdateEvent(HierarchyBrowserItem item) {
        this.item = item;
    }

    public HierarchyBrowserCheckBoxUpdateEvent(HierarchyBrowserItem item, boolean removeItemOnly) {
        this.item = item;
        this.removeItemOnly = removeItemOnly;
    }

    @Override
    public Type<HierarchyBrowserCheckBoxUpdateEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserCheckBoxUpdateEventHandler handler) {
        handler.onHierarchyBrowserCheckBoxUpdate(this);
    }

    public HierarchyBrowserItem getItem() {
        return item;
    }

    public boolean isRemoveItemOnly() {
        return removeItemOnly;
    }
}