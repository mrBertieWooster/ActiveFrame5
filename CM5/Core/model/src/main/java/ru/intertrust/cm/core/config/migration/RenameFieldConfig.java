package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации переименования поля при миграции
 */
@Root(name = "rename-field")
public class RenameFieldConfig extends MigrationScenarioConfig implements Dto {

    @Attribute
    private String type;

    @ElementList(entry="field", inline=true)
    private List<RenameFieldFieldConfig> fields = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RenameFieldFieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<RenameFieldFieldConfig> fields) {
        if (fields == null) {
            this.fields.clear();
        } else {
            this.fields = fields;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RenameFieldConfig that = (RenameFieldConfig) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (fields != null ? !fields.equals(that.fields) : that.fields != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
