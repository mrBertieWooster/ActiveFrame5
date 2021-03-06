package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 8/9/13
 *         Time: 12:05 PM
 */
@Root(name = "attribute")
public class AttributeConfig implements Dto {

    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "value", required = false)
    private String value;

    @ElementList(inline = true, required = false)
    private List<AttributeConfig> attributeConfigList = new ArrayList<AttributeConfig>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<AttributeConfig> getAttributeConfigList() {
        return attributeConfigList;
    }

    public void setAttributeConfigList(List<AttributeConfig> attributeConfigList) {
        this.attributeConfigList = attributeConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttributeConfig that = (AttributeConfig) o;

        if (attributeConfigList != null ? !attributeConfigList.equals(that.getAttributeConfigList()) :that.
                getAttributeConfigList() != null) {
                    return false;
        }

        if (value != null ? !value.equals(that.getValue()) : that.getValue() != null) {
            return false;
        }
        if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (attributeConfigList != null ? attributeConfigList.hashCode() : 0);
        return result;
    }
}

