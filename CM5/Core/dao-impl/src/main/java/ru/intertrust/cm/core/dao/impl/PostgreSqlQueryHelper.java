package ru.intertrust.cm.core.dao.impl;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlSequenceName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class PostgreSqlQueryHelper extends BasicQueryHelper {

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
    protected String generateIndexQuery(String tableName, String indexType, List<String> fieldNames) {
        String indexFieldsPart = createIndexTableFieldsPart(fieldNames);
        String indexName = createExplicitIndexName(tableName, fieldNames);
        return "create index " + wrap(indexName) + " on " + wrap(tableName) +
                " USING " + indexType + " (" + indexFieldsPart + ")";
    }
}
