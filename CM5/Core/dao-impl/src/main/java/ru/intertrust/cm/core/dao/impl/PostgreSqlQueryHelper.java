package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

import java.util.List;

import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class PostgreSqlQueryHelper extends BasicQueryHelper {

    private static final String FOREIGN_KEYS_QUERY =
            "select conname constraint_name, conrelid::regclass table_name, a.attname column_name, " +
                    "confrelid::regclass foreign_table_name, af.attname foreign_column_name " +
                    "from (select conname, conrelid, confrelid, unnest(confkey) as column_index from pg_constraint) c " +
                    "join pg_attribute a on c.conrelid = a.attrelid and c.column_index = a.attnum " +
                    "join pg_attribute af on c.confrelid = af.attrelid and c.column_index = af.attnum " +
                    "join pg_tables t on c.confrelid::regclass::varchar = t.tablename " +
                    "where t.schemaname !~ '^pg_' and t.schemaname != 'information_schema' order by c.conname";

    private static final String COLUMNS_QUERY =
            "select table_name, column_name, is_nullable nullable, character_maximum_length length, " +
                    "numeric_precision, numeric_scale " +
                    "from information_schema.columns where table_schema = 'public'";

    protected PostgreSqlQueryHelper(DomainObjectTypeIdDao DomainObjectTypeIdDao, MD5Service md5Service) {
        super(DomainObjectTypeIdDao, md5Service);
    }

    @Override
    public String generateCountTablesQuery() {
        return "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
    }

    @Override
    protected String getIdType() {
        return "bigint";
    }

    @Override
    protected String getTextType() {
        return "text";
    }

    @Override
    protected String generateIndexQuery(DomainObjectTypeConfig config, String indexType, List<String> indexFields, List<String> indexExpressions) {
        String indexFieldsPart = createIndexFieldsPart(indexFields, indexExpressions);
        // имя индекса формируется из MD5 хеша DDL выражения
        String indexName = createExplicitIndexName(config, indexFields, indexExpressions);
        return "create index " + wrap(indexName) + " on " + wrap(getSqlName(config)) +
                " USING " + indexType + " (" + indexFieldsPart + ")";
    }

    @Override
    public String generateSetColumnNullableQuery(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        StringBuilder query = new StringBuilder();
        query.append("alter table ").append(wrap(getSqlName(config))).append(" alter column ").
                append(wrap(getSqlName(fieldConfig))).append(" drop not null;");

        if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            query.append("alter table ").append(wrap(getSqlName(config))).append(" alter column ").
                    append(wrap(getReferenceTypeColumnName(fieldConfig.getName()))).append(" drop not null;");
        } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
            query.append("alter table ").append(wrap(getSqlName(config))).append(" alter column ").
                    append(wrap(getTimeZoneIdColumnName(fieldConfig.getName()))).append(" drop not null;");
        }

        return query.toString();
    }

    @Override
    protected String generateIsTableExistQuery() {
        return "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name = ?";
    }

    @Override
    protected String generateGetSchemaTablesQuery() {
        return COLUMNS_QUERY;
    }

    @Override
    protected String generateGetForeignKeysQuery() {
        return FOREIGN_KEYS_QUERY;
    }

}
