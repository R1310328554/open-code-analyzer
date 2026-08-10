package org.keycloak.models.workflow.expression;

/**
 * 布尔条件表达式的抽象求值访问者：实现 AND/OR/NOT 组合逻辑，子类负责 {@link #visitConditionCall} 的具体语义。
 */
public abstract class AbstractBooleanEvaluator extends BooleanConditionParserBaseVisitor<Boolean> {

    @Override
    public Boolean visitEvaluator(BooleanConditionParser.EvaluatorContext ctx) {
        return visit(ctx.expression());
    }

    /** 处理 OR 短路求值；无 OR 时直接求 andExpression。 */
    @Override
    public Boolean visitExpression(BooleanConditionParser.ExpressionContext ctx) {
        if (ctx.expression() != null && ctx.OR() != null) {
            return visit(ctx.expression()) || visit(ctx.andExpression());
        }
        return visit(ctx.andExpression());
    }

    /** 处理 AND 短路求值；无 AND 时直接求 notExpression。 */
    @Override
    public Boolean visitAndExpression(BooleanConditionParser.AndExpressionContext ctx) {
        if (ctx.andExpression() != null && ctx.AND() != null) {
            return visit(ctx.andExpression()) && visit(ctx.notExpression());
        }
        return visit(ctx.notExpression());
    }

    /** 处理 NOT 取反；无 NOT 时直接求 atom。 */
    @Override
    public Boolean visitNotExpression(BooleanConditionParser.NotExpressionContext ctx) {
        if (ctx.NOT() != null) {
            return !visit(ctx.notExpression());
        }
        return visit(ctx.atom());
    }

    /** atom 为条件调用或括号嵌套表达式。 */
    @Override
    public Boolean visitAtom(BooleanConditionParser.AtomContext ctx) {
        if (ctx.conditionCall() != null) {
            return visit(ctx.conditionCall());
        }
        return visit(ctx.expression());
    }

    /** 子类实现：对单个条件调用（如 {@code has-role(admin)}）求值。 */
    @Override
    public abstract Boolean visitConditionCall(BooleanConditionParser.ConditionCallContext ctx);

    /**
     * 从语法树的 parameter 节点提取条件参数字符串。
     */
    protected String extractParameter(BooleanConditionParser.ParameterContext paramCtx) {
        // 情况 1：未使用括号，如 "user-logged-in"
        // 情况 2：空括号，如 "user-logged-in()"
        if (paramCtx == null || paramCtx.ParameterText() == null) {
            return null;
        }

        // 情况 3：带参数，如 "has-role(param)"
        String rawText = paramCtx.ParameterText().getText();
        return unEscapeParameter(rawText);
    }

    /**
     * 语法定义的转义序列为 {@code \)} 与 {@code \\}。
     *
     * @param rawText ParameterText 词法单元的原始文本。
     * @return 反转义后的参数字符串。
     */
    private String unEscapeParameter(String rawText) {
        // 将 \) 还原为 )，\\ 还原为 \
        // 注意：replaceAll 使用正则，反斜杠需双重转义
        return rawText.replace("\\)", ")")
                .replace("\\\\", "\\");
    }
}
