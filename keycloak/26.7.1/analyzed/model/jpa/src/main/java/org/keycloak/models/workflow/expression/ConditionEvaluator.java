package org.keycloak.models.workflow.expression;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowConditionProvider;
import org.keycloak.models.workflow.WorkflowExecutionContext;

import static org.keycloak.models.workflow.Workflows.getConditionProvider;

/**
 * 工作流条件表达式求值器：将解析树中的条件调用委托给 {@link WorkflowConditionProvider}。
 */
public class ConditionEvaluator extends AbstractBooleanEvaluator {

    protected final KeycloakSession session;
    protected final WorkflowExecutionContext context;

    public ConditionEvaluator(KeycloakSession session, WorkflowExecutionContext context) {
        this.session = session;
        this.context = context;
    }

    /**
     * 按标识符解析条件 provider（下划线转连字符），并调用其 {@code evaluate}。
     */
    @Override
    public Boolean visitConditionCall(BooleanConditionParser.ConditionCallContext ctx) {
        String conditionName = ctx.Identifier().getText();
        WorkflowConditionProvider conditionProvider = getConditionProvider(session, conditionName.replace("_", "-").toLowerCase(), super.extractParameter(ctx.parameter()));
        return conditionProvider.evaluate(context);
    }

}
