package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации создания уникального ключа при миграции
 */
@Root(name = "create-unique-key")
public class CreateUniqueKeyConfig extends MigrationScenarioConfig implements Dto {

    @Attribute
    private String type;

    @ElementList(entry="field", inline=true)
    private List<CreateUniqueKeyFieldConfig> fields = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CreateUniqueKeyFieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<CreateUniqueKeyFieldConfig> fields) {
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

        CreateUniqueKeyConfig that = (CreateUniqueKeyConfig) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (fields != null ? !fields.equals(that.fields) : that.fields != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}
