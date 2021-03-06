package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;


@Root(name = "collection-count-refresh")
public class CollectionCountRefreshConfig implements Dto {
    @Attribute(name = "time")
    private Integer time;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionCountRefreshConfig that = (CollectionCountRefreshConfig) o;

        if (!time.equals(that.time)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }
}
