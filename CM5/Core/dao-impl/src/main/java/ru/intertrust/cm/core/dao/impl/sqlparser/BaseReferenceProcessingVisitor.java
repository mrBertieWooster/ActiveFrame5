package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.Iterator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

public abstract class BaseReferenceProcessingVisitor extends BaseExpressionVisitor implements  SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor, SelectItemVisitor {

    @Override
    protected void visitSubSelect(SubSelect subSelect) {
        subSelect.getSelectBody().accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            selectItem.accept(this);
        }
        plainSelect.getFromItem().accept(this);

        if (plainSelect.getJoins() != null) {
            for (Iterator joinsIterartor = plainSelect.getJoins().iterator(); joinsIterartor.hasNext();) {
                Join join = (Join) joinsIterartor.next();
                join.getRightItem().accept(this);
            }
        }

        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }
    }

    @Override
    public void visit(InExpression inExpression) {
        inExpression.getLeftExpression().accept(this);
        inExpression.getRightItemsList().accept(this);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList expressionList : multiExprList.getExprList()) {
            visit(expressionList);
        }
    }

    @Override
    public void visit(Table tableName) {

    }

    @Override
    public void visit(SubJoin subjoin) {
        if (subjoin.getJoin().getRightItem() != null) {
            subjoin.getJoin().getRightItem().accept(this);
        }
        if (subjoin.getLeft() != null) {
            subjoin.getLeft().accept(this);
        }
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        visitSubSelect(lateralSubSelect.getSubSelect());
    }

    @Override
    public void visit(ValuesList valuesList) {
        visit(valuesList.getMultiExpressionList());
    }

    @Override
    public void visit(SetOperationList setOpList) {
        for (PlainSelect plainSelect : setOpList.getPlainSelects()) {
            visit(plainSelect);
        }
    }

    @Override
    public void visit(WithItem withItem) {
    }

    @Override
    public void visit(AllColumns allColumns) {
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(this);

    }

    @Override
    public void visit(Column column) {
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    @Override
    public void visit(Function function) {

    }

    @Override
    public void visit(SignedExpression signedExpression) {

    }


}
