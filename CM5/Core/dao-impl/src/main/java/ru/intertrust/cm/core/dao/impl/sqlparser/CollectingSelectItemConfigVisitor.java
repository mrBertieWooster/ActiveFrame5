package ru.intertrust.cm.core.dao.impl.sqlparser;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * 
 * @author atsvetkov
 * 
 */
public class CollectingSelectItemConfigVisitor extends CollectingColumnConfigVisitor {

    public CollectingSelectItemConfigVisitor(ConfigurationExplorer configurationExplorer, PlainSelect plainSelect) {
        super(configurationExplorer, plainSelect);
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        //Если с данным именем уже нашли колонку в подзапросе, то ничего не делаем, конфигурация останется та, что получена в подзапросе
        if (selectExpressionItem.getExpression() instanceof Column
                && columnToConfigMapping.containsKey(((Column) selectExpressionItem.getExpression()).getColumnName())) {
            replaceColumnNameToAliasNameInConfigMap(selectExpressionItem);
            return;
        }

        selectExpressionItem.getExpression().accept(this);

        // Add column to config mapping for column alias
        if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null &&
                selectExpressionItem.getExpression() instanceof Column) {
            replaceColumnNameToAliasNameInConfigMap(selectExpressionItem);
        } else if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null) {
            String aliasName = DaoUtils.unwrap(Case.toLower(selectExpressionItem.getAlias().getName()));
            columnToConfigMapping.remove(aliasName);
        }
    }

    /**
     * Замена имени конфигурации колонки с имени колонки на алиас
     * @param selectExpressionItem
     */
    private void replaceColumnNameToAliasNameInConfigMap(SelectExpressionItem selectExpressionItem) {
        if (selectExpressionItem.getAlias() != null && selectExpressionItem.getAlias().getName() != null) {
            Column column = (Column) selectExpressionItem.getExpression();
            String aliasName = DaoUtils.unwrap(Case.toLower(selectExpressionItem.getAlias().getName()));
            //Выполняем замену только если алиас отличается от имени колонки
            if (!aliasName.equalsIgnoreCase(getColumnName(column))) {
                columnToConfigMapping.put(aliasName, columnToConfigMapping.get(getColumnName(column)));
                columnToConfigMapping.remove(getColumnName(column));
            }
        }
    }

}
