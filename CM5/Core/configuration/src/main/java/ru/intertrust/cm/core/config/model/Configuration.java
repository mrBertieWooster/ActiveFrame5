package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:05 PM
 */
@Root
public class Configuration implements Serializable {
    @ElementListUnion({
            @ElementList(entry="domain-object-type", type=DomainObjectTypeConfig.class, inline=true),
            @ElementList(entry="collection", type=CollectionConfig.class, inline=true)
    })
    private List<Object> configurationList = new ArrayList<>();

    public List getConfigurationList() {
        return configurationList;
    }

    public void setConfigurationList(List configurationList) {
        if(configurationList != null) {
            this.configurationList = configurationList;
        } else {
            this.configurationList.clear();
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

        Configuration that = (Configuration) o;

        if (configurationList != null ? !configurationList.equals(that.configurationList) : that.configurationList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return configurationList != null ? configurationList.hashCode() : 0;
    }
}
