package org.keycloak.models.workflow.expression;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowExecutionContext;

import static org.keycloak.models.workflow.Workflows.getEventProvider;

/**
 * 工作流事件表达式求值器：将解析树中的条件调用委托给 {@link WorkflowEventProvider}。
 * <p>
 * 用于 {@code on-event}、{@code cancel-in-progress} 等配置中的事件表达式求值。
 */
public class EventEvaluator extends AbstractBooleanEvaluator {

    private final WorkflowExecutionContext context;
    private final KeycloakSession session;

    public EventEvaluator(KeycloakSession session, WorkflowExecutionContext context) {
        this.context = context;
        this.session = session;
    }

    /**
     * 按标识符解析事件 provider（下划线转连字符），并调用其 {@code evaluate}。
     */
    @Override
    public Boolean visitConditionCall(BooleanConditionParser.ConditionCallContext ctx) {
        String name = ctx.Identifier().getText();
        WorkflowEventProvider provider = getEventProvider(session, name.replace("_", "-").toLowerCase(), super.extractParameter(ctx.parameter()));
        return provider.evaluate(context);
    }
}
