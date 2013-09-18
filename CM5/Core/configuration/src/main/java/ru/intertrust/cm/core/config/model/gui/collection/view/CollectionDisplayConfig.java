package ru.intertrust.cm.core.config.model.gui.collection.view;

import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author atsvetkov
 *
 */
public class CollectionDisplayConfig implements Dto {

    @ElementList(entry="column", type=CollectionColumnConfig.class, inline=true)
    private List<CollectionColumnConfig> columnConfig = new ArrayList<CollectionColumnConfig>();

    public List<CollectionColumnConfig> getColumnConfig() {
        return columnConfig;
    }

    public void setColumnConfig(List<CollectionColumnConfig> columnConfig) {
        if(columnConfig != null) {
            this.columnConfig = columnConfig;
        } else {
            this.columnConfig.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionDisplayConfig that = (CollectionDisplayConfig) o;

        if (columnConfig != null ? !columnConfig.equals(that.columnConfig) : that.columnConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return columnConfig != null ? columnConfig.hashCode() : 0;
    }
}
