package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "sort-criterion")
public class SortCriterionConfig implements Dto {

    private static final String DESCENDING = "desc";
    @Attribute(name = "field", required = false)
    private String field;

    @Attribute(name = "order", required = false)
    private String order;

    public SortCriterion.Order getOrder() {

        if (DESCENDING.equalsIgnoreCase(order)) {
            return SortCriterion.Order.DESCENDING;
        }

        return SortCriterion.Order.ASCENDING;
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
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (order != null ? order.hashCode() : 0);
        return result;
    }
}
