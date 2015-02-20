package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.SchemaCache;

/**
 * Проверяет соответствие конфигурации поля типа ДО соотв. колонке в базе
 */
public class FieldConfigDbValidator {

    @Autowired
    private SchemaCache schemaCache;

    /**
     * Проверяет соответствие конфигурации поля типа ДО соотв. колонке в базе
     */
    public void validate(FieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        validateBasicAttributes(fieldConfig, domainObjectTypeConfig, columnInfo);

        if (fieldConfig instanceof StringFieldConfig) {
            validate((StringFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        } else if (fieldConfig instanceof PasswordFieldConfig) {
            validate((PasswordFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        } else if (fieldConfig instanceof ReferenceFieldConfig) {
            validate((ReferenceFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        } else if (fieldConfig instanceof DecimalFieldConfig) {
            validate((DecimalFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        }
    }

    private void validate(StringFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (fieldConfig.getLength() != columnInfo.getLength()) {
            throw new ConfigurationException("Configuration loading aborted: validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because such column already exists but has a different length property '" +
                    columnInfo.getLength() + "'");
        }
    }

    private void validate(PasswordFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (fieldConfig.getLength() != columnInfo.getLength()) {
            throw new ConfigurationException("Configuration loading aborted: validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because such column already exists but has a different length property '" +
                    columnInfo.getLength() + "'");
        }
    }

    private void validate(ReferenceFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (!schemaCache.isReferenceColumnExist(domainObjectTypeConfig, fieldConfig)) {
            throw new ConfigurationException("Configuration loading aborted: validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because required reference type column doesn't exist");
        }

        if (fieldConfig.getType().equals(ConfigurationExplorer.REFERENCE_TYPE_ANY)) {
            return;
        }

        String foreignKeyName = schemaCache.getForeignKeyName(domainObjectTypeConfig, fieldConfig);
        if (foreignKeyName == null) {
            throw new ConfigurationException("Configuration loading aborted: validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because required foreign key doesn't exist");
        }
    }

    private void validate(DecimalFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (fieldConfig.getPrecision() != columnInfo.getPrecision()) {
            if (fieldConfig.isNotNull() != columnInfo.isNotNull()) {
                throw new ConfigurationException("Configuration loading aborted: validation against DB failed for field '" +
                        domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                        "' because such column already exists but has a different precision property '" +
                        columnInfo.isNotNull() + "'");
            }
        }

        if (fieldConfig.getScale() != columnInfo.getScale()) {
            if (fieldConfig.isNotNull() != columnInfo.isNotNull()) {
                throw new ConfigurationException("Configuration loading aborted: validation against DB failed for field '" +
                        domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                        "' because such column already exists but has a different scale property '" +
                        columnInfo.isNotNull() + "'");
            }
        }
    }

    private void validateBasicAttributes(FieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (fieldConfig.isNotNull() != columnInfo.isNotNull()) {
            throw new ConfigurationException("Configuration loading aborted: validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because such column already exists but has a different not-null property '" +
                    columnInfo.isNotNull() + "'");
        }

        for (UniqueKeyConfig uniqueKeyConfig : domainObjectTypeConfig.getUniqueKeyConfigs()) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                if (!uniqueKeyFieldConfig.getName().equalsIgnoreCase(fieldConfig.getName())) {
                    continue;
                }

                String uniqueKeyConfigName = schemaCache.getUniqueKeyName(domainObjectTypeConfig, uniqueKeyConfig);
                if (uniqueKeyConfigName != null) {
                    continue;
                }

                StringBuilder message = new StringBuilder();
                message.append("Configuration loading aborted: validation against DB failed for field '").
                        append(domainObjectTypeConfig.getName()).append(".").append(fieldConfig.getName()).
                        append("' because required unique key doesn't exist. The required unique key fields: ");

                for (UniqueKeyFieldConfig uniqueKeyFieldConfig2 : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                    message.append(uniqueKeyFieldConfig2.getName()).append(", ");
                }

                message.delete(message.length() - 3, message.length() - 1);
                throw new ConfigurationException(message.toString());
            }
        }
    }
}