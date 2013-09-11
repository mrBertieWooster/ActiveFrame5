package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import java.io.Serializable;

/**
 * User: Yaroslav Bondarchuk Date: 04.09.13 Time: 14:40
 */
@SuppressWarnings("serial")
@Root(name = "sort-criterion")
public class SortCriterionConfig implements Serializable {
    @Attribute(name = "field", required = false)
    private String field;

    @Attribute(name = "order", required = false)
    private String order;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SortCriterionConfig that = (SortCriterionConfig) o;

        if (order != null ? !order.equals(that.getOrder()) : that.getOrder() != null) {
            return false;
        }

        if (field != null ? !field.equals(that.getField()) : that.getField() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = order != null ? order.hashCode() : 0;
        result = result * 23 + field != null ? field.hashCode() : 0;
        return result;
    }
}
