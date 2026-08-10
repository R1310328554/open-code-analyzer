package org.keycloak.models.workflow.expression;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowConditionProvider;

import static org.keycloak.models.workflow.Workflows.getConditionProvider;

/**
 * 将工作流布尔条件表达式解析为 JPA {@link Predicate} 的访问器。
 * <p>
 * 遍历 ANTLR 语法树，按 OR/AND/NOT 组合条件调用，并委托 {@link WorkflowConditionProvider} 生成具体查询谓词。
 */
public class PredicateEvaluator extends BooleanConditionParserBaseVisitor<Predicate> {

    /** JPA Criteria 构建器。 */
    private final CriteriaBuilder cb;
    /** 当前 Criteria 查询（用于条件 Provider 构建子查询）。 */
    private final CriteriaQuery<String> query;
    /** 查询根实体。 */
    private final Root<?> root;
    /** Keycloak 会话，用于解析条件 Provider。 */
    private final KeycloakSession session;

    public PredicateEvaluator(KeycloakSession session, CriteriaBuilder cb, CriteriaQuery<String> query, Root<?> root) {
        this.session = session;
        this.cb = cb;
        this.query = query;
        this.root = root;
    }

    @Override
    public Predicate visitEvaluator(BooleanConditionParser.EvaluatorContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Predicate visitExpression(BooleanConditionParser.ExpressionContext ctx) {
        // 处理 'expression OR andExpression'
        if (ctx.OR() != null) {
            Predicate left = visit(ctx.expression());
            Predicate right = visit(ctx.andExpression());
            return cb.or(left, right);
        }
        // 处理 'andExpression'
        return visit(ctx.andExpression());
    }

    @Override
    public Predicate visitAndExpression(BooleanConditionParser.AndExpressionContext ctx) {
        // 处理 'andExpression AND notExpression'
        if (ctx.AND() != null) {
            Predicate left = visit(ctx.andExpression());
            Predicate right = visit(ctx.notExpression());
            return cb.and(left, right);
        }
        // 处理 'notExpression'
        return visit(ctx.notExpression());
    }

    @Override
    public Predicate visitNotExpression(BooleanConditionParser.NotExpressionContext ctx) {
        // 处理 '!' notExpression
        if (ctx.NOT() != null) {
            return cb.not(visit(ctx.notExpression()));
        }
        // 处理 'atom'
        return visit(ctx.atom());
    }

    @Override
    public Predicate visitAtom(BooleanConditionParser.AtomContext ctx) {
        if (ctx.conditionCall() != null) {
            return visit(ctx.conditionCall());
        }
        return visit(ctx.expression());
    }

    @Override
    public Predicate visitConditionCall(BooleanConditionParser.ConditionCallContext ctx) {
        String conditionName = ctx.Identifier().getText();
        WorkflowConditionProvider conditionProvider = getConditionProvider(session, conditionName, extractParameter(ctx.parameter()));
        return conditionProvider.toPredicate(cb, query, root);
    }

    /**
     * 从条件调用的参数上下文中提取参数字符串。
     *
     * @param paramCtx 语法树中的 Parameter 节点，可为 null
     * @return 去转义后的参数文本；无参数时返回 null
     */
    protected String extractParameter(BooleanConditionParser.ParameterContext paramCtx) {
        // 情况 1：未使用括号（如 "user-logged-in"）
        // 情况 2：空括号（如 "user-logged-in()"）
        if (paramCtx == null || paramCtx.ParameterText() == null) {
            return null;
        }

        // 情况 3：提供了参数（如 "has-role(param)"）
        String rawText = paramCtx.ParameterText().getText();
        return unEscapeParameter(rawText);
    }

    /**
     * 语法定义的转义序列为 {@code \)} 与 {@code \\}。
     *
     * @param rawText ParameterText 词元的原始文本
     * @return 去转义后的干净字符串
     */
    private String unEscapeParameter(String rawText) {
        // 将 \) 还原为 )，\\ 还原为 \
        return rawText.replace("\\)", ")")
                .replace("\\\\", "\\");
    }
}
