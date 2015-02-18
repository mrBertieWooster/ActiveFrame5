package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации удаляемых при миграции типов
 */
public class DeleteTypesConfig {

    @ElementList(entry="type", inline=true)
    private List<DeleteTypesTypeConfig> types = new ArrayList<>();

    public List<DeleteTypesTypeConfig> getTypes() {
        return types;
    }

    public void setTypes(List<DeleteTypesTypeConfig> types) {
        if (types == null) {
            this.types.clear();
        } else {
            this.types = types;
        }
    }
}
