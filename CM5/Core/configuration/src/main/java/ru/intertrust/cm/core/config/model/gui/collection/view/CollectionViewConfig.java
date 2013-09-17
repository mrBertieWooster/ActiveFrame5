package ru.intertrust.cm.core.config.model.gui.collection.view;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "collection-view")
public class CollectionViewConfig implements TopLevelConfig, Dto {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "is-default")
    private boolean isDefault;

    @Attribute(name = "collection", required = false)
    private String collection;

    @Element(name = "display", required = false)
    private CollectionDisplayConfig collectionDisplayConfig;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public CollectionDisplayConfig getCollectionDisplayConfig() {
        return collectionDisplayConfig;
    }

    public void setCollectionDisplayConfig(CollectionDisplayConfig collectionDisplayConfig) {
        this.collectionDisplayConfig = collectionDisplayConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionViewConfig)) return false;

        CollectionViewConfig that = (CollectionViewConfig) o;

        if (isDefault != that.isDefault) {
            return false;
        }
        if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
            return false;
        }
        if (collectionDisplayConfig != null ? !collectionDisplayConfig.equals(that.collectionDisplayConfig) : that.collectionDisplayConfig != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (isDefault ? 1 : 0);
        result = 31 * result + (collection != null ? collection.hashCode() : 0);
        result = 31 * result + (collectionDisplayConfig != null ? collectionDisplayConfig.hashCode() : 0);
        return result;
    }
}
