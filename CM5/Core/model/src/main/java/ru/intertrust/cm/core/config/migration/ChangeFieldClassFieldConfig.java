package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Java модель конфигурации поля, в котором меняется тип при миграции
 */
public class ChangeFieldClassFieldConfig implements Dto {

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
