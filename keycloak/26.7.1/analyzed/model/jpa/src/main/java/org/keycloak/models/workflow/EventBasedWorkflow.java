package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.expression.BooleanConditionParser;
import org.keycloak.models.workflow.expression.ConditionEvaluator;
import org.keycloak.models.workflow.expression.EvaluatorUtils;
import org.keycloak.models.workflow.expression.EventEvaluator;
import org.keycloak.representations.workflows.WorkflowConstants;
import org.keycloak.utils.StringUtil;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_CANCEL_IN_PROGRESS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_CONDITIONS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_ON_EVENT;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_RESTART_IN_PROGRESS;

/**
 * 基于事件的 workflow 激活/停用/重启决策：解析 on-event、conditions 与并发配置。
 */
final class EventBasedWorkflow {

    private final KeycloakSession session;
    private final ResourceType supportedType;
    private final ComponentModel model;

    EventBasedWorkflow(KeycloakSession session, ResourceType supportedType, ComponentModel model) {
        this.supportedType = supportedType;
        this.session = session;
        this.model = model;
    }

    boolean supports(ResourceType type) {
        return supportedType == type;
    }

    /**
     * 判断 workflow 是否应激活：事件类型匹配且资源条件为真。
     *
     * @param executionContext a reference to the workflow execution context.
     * @return {@code true} if the workflow should be activated, {@code false} otherwise.
     */
    boolean activate(WorkflowExecutionContext executionContext) {
        WorkflowEvent event = executionContext.getEvent();
        if (event == null) {
            return false;
        }
        return supports(event.getResourceType()) && activateOnEvent(executionContext) && validateResourceConditions(executionContext);
    }

    /**
     * 判断 workflow 是否应停用：事件匹配 cancel-in-progress 配置。
     *
     * @param executionContext a reference to the workflow execution context.
     * @return {@code true} if the workflow should be deactivated, {@code false} otherwise.
     */
    boolean deactivate(WorkflowExecutionContext executionContext) {
        String cancelInProgress = model.getConfig().getFirst(CONFIG_CANCEL_IN_PROGRESS);
        return matchesConcurrencySetting(executionContext, cancelInProgress);
    }

    /**
     * 判断 workflow 是否应重启：事件匹配 restart-in-progress 配置。
     *
     * @param executionContext a reference to the workflow execution context.
     * @return {@code true} if the workflow should be restarted, {@code false} otherwise.
     */
    boolean restart(WorkflowExecutionContext executionContext) {
        String restartInProgress = model.getConfig().getFirst(CONFIG_RESTART_IN_PROGRESS);
        return matchesConcurrencySetting(executionContext, restartInProgress);
    }

    /**
     * 校验 workflow 资源条件；未配置 conditions 时视为通过。
     *
     * @param context a reference to the workflow execution context.
     * @return {@code true} if the resource conditions are met or not defined, {@code false} otherwise.
     */
    public boolean validateResourceConditions(WorkflowExecutionContext context) {
        String conditions = getModel().getConfig().getFirst(CONFIG_CONDITIONS);
        if (StringUtil.isNotBlank(conditions)) {
            BooleanConditionParser.EvaluatorContext evaluatorContext = EvaluatorUtils.createEvaluatorContext(model, conditions);
            ConditionEvaluator evaluator = new ConditionEvaluator(session, context);
            return evaluator.visit(evaluatorContext);
        } else {
            return true;
        }
    }

    /**
     * 根据配置的事件表达式判断 workflow 是否应被当前事件激活。
     *
     * @param executionContext a reference to the workflow execution context.
     * @return {@code true} if the workflow should be activated, {@code false} otherwise.
     */
    private boolean activateOnEvent(WorkflowExecutionContext executionContext) {
        // AD_HOC 为特殊事件，无视 on-event 配置始终触发
        if (WorkflowConstants.AD_HOC.equals(executionContext.getEvent().getEventProviderId())) {
            return true;
        }

        String eventConditions = model.getConfig().getFirst(CONFIG_ON_EVENT);
        if (StringUtil.isNotBlank(eventConditions)) {
            BooleanConditionParser.EvaluatorContext context = EvaluatorUtils.createEvaluatorContext(model, eventConditions);
            EventEvaluator eventEvaluator = new EventEvaluator(session, executionContext);
            return eventEvaluator.visit(context);
        } else {
            return false;
        }
    }

    /**
     * 判断事件是否匹配并发配置（restart-in-progress 或 cancel-in-progress）。
     * 值为 "true" 时按激活条件决策；否则解析为事件表达式求值。
     *
     * @param executionContext a reference to the workflow execution context.
     * @param concurrencySetting the concurrency setting to evaluate.
     * @return {@code true} if the event matches the concurrency setting, {@code false} otherwise.
     */
    private boolean matchesConcurrencySetting(WorkflowExecutionContext executionContext, String concurrencySetting) {
        WorkflowEvent event = executionContext.getEvent();
        if (event == null) {
            return false;
        }

        if (StringUtil.isNotBlank(concurrencySetting)) {
            // 配置为 "true" 时：仅当 workflow 配置了 on-event 且满足激活条件才生效
            if (Boolean.parseBoolean(concurrencySetting)) {
                return StringUtil.isNotBlank(model.getConfig().getFirst(CONFIG_ON_EVENT)) && activate(executionContext);
            }
            else {
                // 否则将配置值视为事件表达式并求值
                BooleanConditionParser.EvaluatorContext context = EvaluatorUtils.createEvaluatorContext(model, concurrencySetting);
                EventEvaluator eventEvaluator = new EventEvaluator(session, executionContext);
                return eventEvaluator.visit(context);
            }
        }
        return false;
    }

    private ComponentModel getModel() {
        return model;
    }

    private KeycloakSession getSession() {
        return session;
    }
}
