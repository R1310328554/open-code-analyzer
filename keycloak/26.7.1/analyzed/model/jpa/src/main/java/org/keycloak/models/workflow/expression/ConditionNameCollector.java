package org.keycloak.models.workflow.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * 遍历布尔条件解析树，收集所有 {@code conditionCall} 的条件名称。
 */
public class ConditionNameCollector extends BooleanConditionParserBaseVisitor<Void> {

    /** 遍历过程中发现的条件调用名称列表。 */
    private final List<String> conditionNames = new ArrayList<>();

    /**
     * 返回已收集的全部条件调用名称。
     */
    public List<String> getConditionNames() {
        return conditionNames;
    }

    // --- 遍历方法：确保访问树中每个相关节点 ---

    @Override
    public Void visitEvaluator(BooleanConditionParser.EvaluatorContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Void visitExpression(BooleanConditionParser.ExpressionContext ctx) {
        // 访问 OR 两侧
        if (ctx.expression() != null) {
            visit(ctx.expression());
        }
        return visit(ctx.andExpression());
    }

    @Override
    public Void visitAndExpression(BooleanConditionParser.AndExpressionContext ctx) {
        // 访问 AND 两侧
        if (ctx.andExpression() != null) {
            visit(ctx.andExpression());
        }
        return visit(ctx.notExpression());
    }

    @Override
    public Void visitNotExpression(BooleanConditionParser.NotExpressionContext ctx) {
        // 访问 NOT 内部表达式
        if (ctx.notExpression() != null) {
            return visit(ctx.notExpression());
        }
        return visit(ctx.atom());
    }

    @Override
    public Void visitAtom(BooleanConditionParser.AtomContext ctx) {
        // 区分条件调用与括号嵌套表达式
        if (ctx.conditionCall() != null) {
            return visit(ctx.conditionCall());
        }
        return visit(ctx.expression());
    }

    // --- 收集逻辑 ---

    @Override
    public Void visitConditionCall(BooleanConditionParser.ConditionCallContext ctx) {
        String conditionName = ctx.Identifier().getText();
        conditionNames.add(conditionName);

        // 无需继续访问 parameter 等子节点
        return null;
    }
}
