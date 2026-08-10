package org.keycloak.models.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.util.DurationConverter;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.expression.BooleanConditionParser;
import org.keycloak.models.workflow.expression.ConditionNameCollector;
import org.keycloak.models.workflow.expression.ConditionTypeCollector;
import org.keycloak.models.workflow.expression.EvaluatorUtils;
import org.keycloak.representations.workflows.WorkflowRepresentation;
import org.keycloak.representations.workflows.WorkflowStepRepresentation;
import org.keycloak.utils.StringUtil;

import static java.util.Optional.ofNullable;

/**
 * 工作流定义校验器：验证名称、条件表达式、步骤配置及 restart 语义。
 */
public class WorkflowValidator {

    /**
     * 校验工作流表示是否合法（名称、on/if/cancel/restart 表达式、步骤与 restart 规则等）。
     *
     * @param session Keycloak 会话
     * @param provider 工作流 provider
     * @param rep 待校验的工作流表示
     * @throws WorkflowInvalidStateException 校验失败时
     */
    public static void validateWorkflow(KeycloakSession session, WorkflowProvider provider, WorkflowRepresentation rep) throws WorkflowInvalidStateException {

        validateWorkflowName(provider, rep);

        // TODO: 待事件条件 provider 实现后，用自定义 evaluator 调用各 condition provider 的 validate 校验 on/if 属性
        if (StringUtil.isNotBlank(rep.getOn())) {
            validateConditionExpression(session, rep.getOn(), "on");
        }
        if (StringUtil.isNotBlank(rep.getConditions())) {
            validateConditionExpression(session, rep.getConditions(), "if");
        }
        if (StringUtil.isNotBlank(rep.getCancelInProgress())) {
            validateConditionExpression(session, rep.getCancelInProgress(), "cancel-in-progress");
        }
        if (StringUtil.isNotBlank(rep.getRestartInProgress())) {
            validateConditionExpression(session, rep.getRestartInProgress(), "restart-in-progress");
        }

        // 含 restart 步骤时，从 restart 位置起须至少有一个带 after 的调度步骤，避免立即执行的无限循环
        List<WorkflowStepRepresentation> steps = ofNullable(rep.getSteps()).orElse(List.of());
        if (steps.isEmpty()) {
            return;
        }
        steps.forEach(step -> validateStep(session, step));

        List<WorkflowStepRepresentation> restartSteps = steps.stream()
                .filter(step -> Objects.equals("restart", step.getUses()))
                .toList();

        if (!restartSteps.isEmpty()) {
            if (restartSteps.size() > 1) {
                throw new WorkflowInvalidStateException("Workflow can have only one restart step.");
            }
            WorkflowStepRepresentation restartStep = restartSteps.get(0);
            if (steps.indexOf(restartStep) != steps.size() - 1) {
                throw new WorkflowInvalidStateException("Workflow restart step must be the last step.");
            }
            MultivaluedHashMap<String, String> config = restartStep.getConfig();
            int position = config == null ? 0 : Integer.parseInt(config.getFirstOrDefault("position", "0"));
            if (position < 0 || position >= steps.size()) {
                throw new WorkflowInvalidStateException("Workflow restart step has invalid position: " + position);
            }
            boolean hasScheduledStep = steps.stream()
                    .skip(position)
                    .anyMatch(step -> DurationConverter.isPositiveDuration(step.getAfter()));
            if (!hasScheduledStep) {
                throw new WorkflowInvalidStateException("No scheduled step found if restarting at position " + position);
            }
        }

        if (rep.getSupports() != null) {
            // supports 已设置表示正在更新工作流，需校验条件类型与资源类型兼容
            try {
                ResourceType type = ResourceType.valueOf(rep.getSupports());
                validateWorkflowConditionType(session, rep.getConditions(), type);
            } catch (IllegalArgumentException e) {
                throw new WorkflowInvalidStateException("Invalid workflow type: " + rep.getSupports());
            }
        }
    }

    /**
     * 校验条件表达式中引用的 provider 是否支持给定资源类型。
     *
     * @param session Keycloak 会话
     * @param condition 条件表达式
     * @param workflowType 工作流资源类型
     */
    public static void validateWorkflowConditionType(KeycloakSession session, String condition, ResourceType workflowType) throws WorkflowInvalidStateException {
        if (StringUtil.isBlank(condition)) {
            return;
        }

        BooleanConditionParser.EvaluatorContext context = EvaluatorUtils.createEvaluatorContext(condition);
        ConditionTypeCollector typeCollector = new ConditionTypeCollector(session);
        // ConditionTypeCollector.visit 在找不到 provider 时会抛出 WorkflowInvalidStateException
        typeCollector.visit(context);

        Set<ResourceType> supportedTypes = typeCollector.getConditionTypes();
        if (!supportedTypes.contains(workflowType)) {
            String formatted = supportedTypes.stream().map(Enum::name).collect(Collectors.joining(", "));
            throw new WorkflowInvalidStateException("Provided condition types (%s) are not compatible with workflow type (%s).".formatted(formatted, workflowType));
        }
    }

    /** 校验单个步骤的 uses、after 及 step provider 是否存在。 */
    private static void validateStep(KeycloakSession session, WorkflowStepRepresentation step) throws WorkflowInvalidStateException {

        // 步骤必须定义 uses
        if (StringUtil.isBlank(step.getUses())) {
            throw new WorkflowInvalidStateException("Step 'uses' cannot be null or empty.");
        }

        // 校验 after 时间（若存在）
        try {
            Duration duration = DurationConverter.parseDuration(step.getAfter());
            if (duration != null && duration.isNegative()) { // 未配置时 duration 可为 null
                throw new WorkflowInvalidStateException("Step 'after' configuration cannot be negative.");
            }
        } catch (IllegalArgumentException e) {
            throw new WorkflowInvalidStateException("Step 'after' configuration is not valid: " + step.getAfter());
        }

        // 确认 step provider 工厂存在
        WorkflowStepProviderFactory<WorkflowStepProvider> factory = (WorkflowStepProviderFactory<WorkflowStepProvider>) session
                .getKeycloakSessionFactory().getProviderFactory(WorkflowStepProvider.class, step.getUses());

        if (factory == null) {
            throw new WorkflowInvalidStateException("Could not find step provider: " + step.getUses());
        }
    }

    /** 校验条件/事件表达式中引用的 provider 名称是否有效。 */
    private static void validateConditionExpression(KeycloakSession session, String expression, String fieldName) throws WorkflowInvalidStateException {
        if (Boolean.parseBoolean(expression)) {
            // 部分字段允许字面量 "true"，无需进一步校验
            return;
        }
        BooleanConditionParser.EvaluatorContext context = EvaluatorUtils.createEvaluatorContext(expression);
        ConditionNameCollector collector = new ConditionNameCollector();
        collector.visit(context);

        // 检查条件与事件表达式中引用的 provider 是否有效
        if ("on".equals(fieldName) || "restart-in-progress".equals(fieldName) || "cancel-in-progress".equals(fieldName)) {
            collector.getConditionNames().forEach(name -> Workflows.getEventProviderFactory(session, name.replace("_", "-").toLowerCase()));
        } else if ("if".equals(fieldName)) {
            // 尝试获取 condition provider 工厂；不存在则抛出 WorkflowInvalidStateException
            collector.getConditionNames().forEach(name -> Workflows.getConditionProviderFactory(session, name.replace("_", "-").toLowerCase()));
        }
    }

    /** 校验工作流名称非空且在 realm 内唯一。 */
    private static void validateWorkflowName(WorkflowProvider provider, WorkflowRepresentation representation) throws WorkflowInvalidStateException {
        String name = representation.getName();
        if (StringUtil.isBlank(name)) {
            throw new WorkflowInvalidStateException("Workflow name cannot be null or empty.");
        }

        // 名称唯一性
        if (provider.getWorkflows().anyMatch(wf -> wf.getName().equals(name) && !wf.getId().equals(representation.getId()))) {
            throw new WorkflowInvalidStateException("Workflow name must be unique. A workflow with name '" + name + "' already exists.");
        }
    }
}
