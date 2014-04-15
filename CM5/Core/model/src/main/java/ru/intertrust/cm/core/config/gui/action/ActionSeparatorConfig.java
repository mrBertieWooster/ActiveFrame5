package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:00.
 */
@Element(name = "action-separator")
public class ActionSeparatorConfig extends AbstractActionEntryConfig {
    @Attribute(required = false)
    private String id;

    @Attribute(required = false)
    private String style;

    @Attribute(required = false)
    private String styleClass;

    @Attribute(required = false)
    private boolean rendered = true;

    @Attribute(required = false)
    private Integer order;

    @Attribute(required = false)
    private boolean merged = false;

    @Attribute(name = "componentName", required = false)
    private String componentName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public boolean isRendered() {
        return rendered;
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
