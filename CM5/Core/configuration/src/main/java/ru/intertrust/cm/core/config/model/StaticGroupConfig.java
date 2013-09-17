package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;

/**
 * Java модель конфигурации статической группы
 * @author atsvetkov
 *
 */

@Root(name = "static-group")
public class StaticGroupConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StaticGroupConfig that = (StaticGroupConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
