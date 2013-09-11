package ru.intertrust.cm.core.config.model.gui.navigation;

import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Author: Yaroslav Bondarchuk Date: 04.09.13 Time: 16:01
 */
@SuppressWarnings("serial")
@Root(name = "domain-object-surfer")
public class DomainObjectSurferConfig extends PluginConfigParent {
    @Element(name = "collection", required = false)
    private CollectionConfig collectionConfig;

    @Element(name = "collection-view", required = false)
    private CollectionViewConfig collectionViewConfig;

    @ElementList(inline = true)
    private List<SortCriterionConfig> sortCriterionConfigList = new ArrayList<SortCriterionConfig>();

    public CollectionConfig getCollectionConfig() {
        return collectionConfig;
    }

    public void setCollectionConfig(CollectionConfig collectionConfig) {
        this.collectionConfig = collectionConfig;
    }

    public List<SortCriterionConfig> getSortCriterionConfigList() {
        return sortCriterionConfigList;
    }

    public void setSortCriterionConfigList(List<SortCriterionConfig> sortCriterionConfigList) {
        this.sortCriterionConfigList = sortCriterionConfigList;
    }

    public CollectionViewConfig getCollectionViewConfig() {
        return collectionViewConfig;
    }

    public void setCollectionViewConfig(CollectionViewConfig collectionViewConfig) {
        this.collectionViewConfig = collectionViewConfig;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainObjectSurferConfig that = (DomainObjectSurferConfig) o;

        if (sortCriterionConfigList != null ? !sortCriterionConfigList.equals(that.getSortCriterionConfigList()) : that.getSortCriterionConfigList() != null) {
            return false;
        }

        if (collectionConfig != null ? !collectionConfig.equals(that.getCollectionConfig()) : that.getCollectionConfig() != null) {
            return false;
        }

        if (collectionViewConfig != null ? !collectionViewConfig.equals(that.getCollectionViewConfig()) : that.getCollectionViewConfig() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sortCriterionConfigList != null ? sortCriterionConfigList.hashCode() : 0;
        result = 23 * result + (collectionConfig != null ? collectionConfig.hashCode() : 0);
        result = result + (collectionViewConfig != null ? collectionViewConfig.hashCode() : 0);
        return result;
    }
}

