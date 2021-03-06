package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IPetrov on 06.03.14.
 */
@Root(name = "groups")
public class GroupsConfig implements Dto {
    @ElementList(inline = true)
    private List<GroupConfig> groupConfigList = new ArrayList<GroupConfig>();

    public List<GroupConfig> getGroupConfigList() {
        return groupConfigList;
    }

    public void setGroupConfigList(List<GroupConfig> groupConfigList) {
        this.groupConfigList = groupConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupsConfig that = (GroupsConfig) o;

        if (groupConfigList != null ? !groupConfigList.equals(that.groupConfigList) : that.groupConfigList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return groupConfigList != null ? groupConfigList.hashCode() : 0;
    }
}
