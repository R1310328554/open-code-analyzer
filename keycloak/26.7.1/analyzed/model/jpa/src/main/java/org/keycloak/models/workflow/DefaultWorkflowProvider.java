package org.keycloak.models.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;

import org.keycloak.common.util.DurationConverter;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.workflow.WorkflowStateProvider.ScheduledStep;
import org.keycloak.representations.workflows.StepExecutionStatus;
import org.keycloak.representations.workflows.WorkflowConstants;
import org.keycloak.representations.workflows.WorkflowRepresentation;
import org.keycloak.representations.workflows.WorkflowStepRepresentation;
import org.keycloak.timer.TimerProvider;

import org.jboss.logging.Logger;

import static java.util.Optional.ofNullable;

/**
 * 默认 {@link WorkflowProvider}：管理 workflow 组件 CRUD、事件驱动激活/停用/重启及定时步骤执行。
 */
public class DefaultWorkflowProvider implements WorkflowProvider {

    private static final Logger log = Logger.getLogger(DefaultWorkflowProvider.class);
    private static final Logger scheduleLog = Logger.getLogger("org.keycloak.workflow.schedule");

    private final KeycloakSession session;
    private final WorkflowStateProvider stateProvider;
    private final WorkflowExecutor executor;
    private final KeycloakSessionFactory sessionFactory;
    private final RealmModel realm;

    DefaultWorkflowProvider(KeycloakSession session, WorkflowExecutor executor) {
        this.session = session;
        this.executor = executor;
        this.sessionFactory = session.getKeycloakSessionFactory();
        this.stateProvider = sessionFactory.getProviderFactory(WorkflowStateProvider.class).create(session);
        this.realm = session.getContext().getRealm();
    }

    @Override
    public ResourceTypeSelector getResourceTypeSelector(ResourceType type) {
        Objects.requireNonNull(type, "type");

        return switch (type) {
            case USERS -> new UserResourceTypeWorkflowProvider(session);
            case CLIENTS -> new ClientResourceTypeWorkflowProvider(session);
        };
    }

    @Override
    public void updateWorkflow(Workflow workflow, WorkflowRepresentation representation) {
        // 第一步：校验更新后的 workflow 定义
        WorkflowValidator.validateWorkflow(session, this, representation);
        WorkflowValidator.validateWorkflowConditionType(session, representation.getConditions(), workflow.getSupportedType());

        // 若无已调度步骤，可直接删后重建（保持 id 不变）
            removeWorkflow(workflow);
            representation.setId(workflow.getId());
            toModel(representation);
        } else {
            // 存在已调度资源时不允许修改 workflow 的 on 配置
            WorkflowRepresentation currentRepresentation = toRepresentation(workflow);
            if (!Objects.equals(currentRepresentation.getOn(), representation.getOn())) {
                throw new ModelValidationException("Cannot update 'on' configuration when there are scheduled resources for the workflow.");
            }

            // 不允许变更 workflow 资源类型
            if (representation.getSupports() != null && !Objects.equals(representation.getSupports(), currentRepresentation.getSupports())) {
                throw new ModelValidationException("Cannot update 'supports' configuration.");
            }

            // 步骤数量、顺序与 uses 不可变；仅允许更新各步骤 config
            List<WorkflowStepRepresentation> currentSteps = currentRepresentation.getSteps();
            List<WorkflowStepRepresentation> newSteps = ofNullable(representation.getSteps()).orElse(List.of());
            if (currentSteps.size() != newSteps.size()) {
                throw new ModelValidationException("Cannot change the number or order of steps when there are scheduled resources for the workflow.");
            }
            for (int i = 0; i < currentSteps.size(); i++) {
                WorkflowStepRepresentation currentStep = currentSteps.get(i);
                WorkflowStepRepresentation newStep = newSteps.get(i);
                if (!Objects.equals(currentStep.getUses(), newStep.getUses())) {
                    throw new ModelValidationException("Cannot change the number or order of steps when there are scheduled resources for the workflow.");
                }
                // 保留原 step id 以便更新 config
                newStep.setId(currentStep.getId());
                newStep.setPriority(Long.parseLong(currentStep.getPriority()));
            }

            // 保留原 supports 类型
            representation.setSupports(currentRepresentation.getSupports());

            // 更新 workflow 与各步骤 config 后重新调度
            workflow.updateConfig(representation.getConfig(), newSteps);

            cancelScheduledWorkflow(workflow);
            scheduleWorkflow(workflow);
            notifyScheduleChange(workflow, false);
        }
    }

    @Override
    public void removeWorkflow(Workflow workflow) {
        Objects.requireNonNull(workflow, "workflow");
        ComponentModel component = getWorkflowComponent(workflow.getId());
        realm.getComponentsStream(workflow.getId(), WorkflowStepProvider.class.getName()).forEach(realm::removeComponent);
        realm.removeComponent(component);
        stateProvider.removeByWorkflow(workflow.getId());
        cancelScheduledWorkflow(workflow);
        notifyScheduleChange(workflow, true);
    }

    @Override
    public Workflow getWorkflow(String id) {
        return new Workflow(session, getWorkflowComponent(id));
    }

    @Override
    public Stream<Workflow> getWorkflows() {
        return realm.getComponentsStream(realm.getId(), WorkflowProvider.class.getName())
                .map(c -> new Workflow(session, c));
    }

    @Override
    public Stream<WorkflowRepresentation> getScheduledWorkflowsByResource(String resourceId) {
        return stateProvider.getScheduledStepsByResource(resourceId).map(scheduledStep -> {
            Workflow workflow = getWorkflow(scheduledStep.workflowId());
            // 构建步骤列表并标记已完成/待执行状态及 scheduledAt
            List<WorkflowStepRepresentation> steps = workflow.getSteps().map(this::toRepresentation).toList();
            boolean scheduledStepFound = false;
            Long scheduledAt = null;
            for (WorkflowStepRepresentation step : steps) {
                if (!scheduledStepFound) {
                    // 定位当前已调度的步骤
                    if (step.getId().equals(scheduledStep.stepId())) {
                        scheduledStepFound = true;
                    } else {
                        step.setExecutionStatus(StepExecutionStatus.COMPLETED);
                    }
                }
                if (scheduledStepFound) {
                    if (scheduledAt == null) {
                        scheduledAt = scheduledStep.scheduledAt();
                    } else if (step.getAfter() != null) {
                        scheduledAt += DurationConverter.parseDuration(step.getAfter()).toMillis();
                    }
                    step.setScheduledAt(scheduledAt);
                    step.setExecutionStatus(StepExecutionStatus.PENDING);
                }
            }
            return new WorkflowRepresentation(workflow.getId(), workflow.getName(), workflow.getConfig(), steps);
        });
    }

    @Override
    public void submit(WorkflowEvent event) {
        processEvent(getWorkflows(), event);
    }

    @Override
    public void runScheduledSteps() {
        getWorkflows().filter(Workflow::isEnabled).forEach((workflow) -> {
            stateProvider.getDueScheduledSteps(workflow).forEach((scheduled) -> {
                try {
                    // 恢复执行前再次校验资源是否仍满足 workflow 条件
                    DefaultWorkflowExecutionContext context = new DefaultWorkflowExecutionContext(session, workflow, scheduled);
                    EventBasedWorkflow provider = new EventBasedWorkflow(session, workflow.getSupportedType(), getWorkflowComponent(workflow.getId()));
                    if (!provider.validateResourceConditions(context)) {
                        log.debugf("Resource %s is no longer eligible for workflow %s. Cancelling execution of the workflow.",
                                scheduled.resourceId(), scheduled.workflowId());
                        WorkflowProviderEvents.fireWorkflowDeactivatedEvent(session, workflow, scheduled.resourceId(),
                                scheduled.executionId(), "Resource no longer meets workflow conditions");
                        stateProvider.remove(scheduled.executionId());
                    } else {
                        WorkflowStep step = context.getStep();
                        if (step == null) {
                            log.warnf("Could not find step %s in workflow %s for resource %s. Cancelling execution of the workflow.",
                                    scheduled.stepId(), scheduled.workflowId(), scheduled.resourceId());
                            WorkflowProviderEvents.fireWorkflowDeactivatedEvent(session, workflow, scheduled.resourceId(),
                                    scheduled.executionId(), "Step not found in workflow");
                            stateProvider.remove(scheduled.executionId());
                        } else {
                            runWorkflow(context);
                        }
                    }
                } catch(Exception e) {
                    log.warnf(e, "Error resuming workflow %s for resource %s: %s", scheduled.workflowId(), scheduled.resourceId(), e.getMessage());
                }
            });
        });
    }

    @Override
    public void migrateScheduledResources(String stepIdFrom, String stepIdTo) {
        if (stepIdFrom.equals(stepIdTo)) {
            return; // 源步骤与目标步骤相同则无需迁移
        }

        // 通过步骤组件定位涉及的 workflow
        ComponentModel stepFromModel = getWorkflowComponent(stepIdFrom, WorkflowStepProvider.class.getName());
        Workflow workflowFrom = getWorkflow(stepFromModel.getParentId());
        ComponentModel stepToModel = getWorkflowComponent(stepIdTo, WorkflowStepProvider.class.getName());
        Workflow workflowTo = getWorkflow(stepToModel.getParentId());

        // get the scheduled steps from the source step
        List<ScheduledStep> scheduledStepsFrom = stateProvider.getScheduledStepsByStep(workflowFrom.getId(), stepIdFrom).toList();

        // 跨 workflow 迁移时需额外校验
        if (!workflowFrom.getId().equals(workflowTo.getId())) {

            // 两 workflow 须支持相同资源类型
            if (workflowFrom.getSupportedType() != workflowTo.getSupportedType()) {
                throw new ModelValidationException("Cannot migrate scheduled resources between workflows that support different resource types.");
            }

            // 源步骤上所有已调度资源须满足目标 workflow 激活条件
            EventBasedWorkflow eventBasedWorkflow = new EventBasedWorkflow(session, workflowTo.getSupportedType(), getWorkflowComponent(workflowTo.getId()));
            for (ScheduledStep scheduledStep : scheduledStepsFrom) {
                DefaultWorkflowExecutionContext context = new DefaultWorkflowExecutionContext(session, workflowTo, scheduledStep);
                if (!eventBasedWorkflow.validateResourceConditions(context)) {
                    throw new ModelValidationException("Cannot migrate resource %s to workflow %s as it does not satisfy the workflow's activation conditions."
                            .formatted(scheduledStep.resourceId(), workflowTo.getName()));
                }
            }
        }

        // 逐条迁移：移除源调度记录并在目标 workflow 指定步骤重启
        int stepPosition = workflowTo.getStepById(stepIdTo).getPriority() - 1;
        WorkflowStep stepFrom = workflowFrom.getStepById(stepIdFrom);
        WorkflowStep stepTo = workflowTo.getStepById(stepIdTo);

        for (ScheduledStep scheduledStep : scheduledStepsFrom) {
            String oldExecutionId = scheduledStep.executionId();

            // 从源 workflow 移除调度状态
            stateProvider.remove(oldExecutionId);

            // 在目标 workflow 指定步骤为资源激活 execution
            DefaultWorkflowExecutionContext context = getWorkflowExecutionContext(scheduledStep, workflowFrom, workflowTo);
            restartWorkflow(context, stepPosition);

            String newExecutionId = context.getExecutionId();
            // 发布 workflow 资源迁移事件
            WorkflowProviderEvents.fireWorkflowResourceMigratedEvent(session, workflowFrom, workflowTo, stepFrom, stepTo,
                    scheduledStep.resourceId(), oldExecutionId, newExecutionId);

            if (log.isDebugEnabled()) {
                log.debugf("Migrated resource %s from workflow %s (step %s) to workflow %s (step %s). Old execution id: %s, new execution id: %s",
                        scheduledStep.resourceId(), workflowFrom.getName(), stepFrom.getProviderId(), workflowTo.getName(),
                        stepTo.getProviderId(), oldExecutionId, newExecutionId);
            }
        }
    }

    private DefaultWorkflowExecutionContext getWorkflowExecutionContext(ScheduledStep scheduledStep, Workflow workflowFrom, Workflow workflowTo) {
        DefaultWorkflowExecutionContext context;
        if (workflowFrom.getId().equals(workflowTo.getId())) {
            // 同一 workflow 内迁移时复用 executionId
            context = new DefaultWorkflowExecutionContext(session, workflowTo, new AdhocWorkflowEvent(workflowTo.getSupportedType(), scheduledStep.resourceId()),
                    scheduledStep.executionId());
        } else {
            context = new DefaultWorkflowExecutionContext(session, workflowTo, new AdhocWorkflowEvent(workflowTo.getSupportedType(),
                    scheduledStep.resourceId()));
        }
        return context;
    }

    @Override
    public void activate(Workflow workflow, ResourceType type, String resourceId) {
        if (type != workflow.getSupportedType()) {
            throw new BadRequestException("Resource Type '%s' is not supported for this workflow (supports %s)".formatted(type.name(), workflow.getSupportedType()));
        }

        processEvent(Stream.of(workflow), new AdhocWorkflowEvent(type, resourceId));
    }

    @Override
    public void deactivate(Workflow workflow, String resourceId) {
        WorkflowStateProvider.ScheduledStep step = stateProvider.getScheduledStep(workflow.getId(), resourceId);
        if (step != null) {
            stateProvider.removeByWorkflowAndResource(workflow.getId(),  resourceId);
            log.debugf("Deactivating workflow %s for resource %s (execution id: %s)", workflow.getName(), resourceId, step.executionId());
            WorkflowProviderEvents.fireWorkflowDeactivatedEvent(session, workflow, resourceId, step.executionId(), "manual deactivation");
        }
    }

    @Override
    public void activateForAllEligibleResources(Workflow workflow) {
        if (workflow.isEnabled()) {
            WorkflowProvider provider = getWorkflowProvider(workflow);
            ResourceType supportedType = workflow.getSupportedType();
            ResourceTypeSelector selector = provider.getResourceTypeSelector(supportedType);
            selector.getResourceIds(workflow)
                    .forEach(resourceId -> processEvent(Stream.of(workflow), new AdhocWorkflowEvent(supportedType, resourceId)));
            }
    }

    @Override
    public WorkflowRepresentation toRepresentation(Workflow workflow) {
        List<WorkflowStepRepresentation> steps = workflow.getSteps().map(this::toRepresentation).toList();
        return new WorkflowRepresentation(workflow.getId(), workflow.getName(), workflow.getConfig(), steps);
    }

    @Override
    public Workflow toModel(WorkflowRepresentation rep) {
        WorkflowValidator.validateWorkflow(session, this, rep);

        MultivaluedHashMap<String, String> config = ofNullable(rep.getConfig()).orElse(new MultivaluedHashMap<>());
        if (rep.getCancelInProgress() != null) {
            config.putSingle(WorkflowConstants.CONFIG_CANCEL_IN_PROGRESS, rep.getCancelInProgress());
        }
        if (rep.getRestartInProgress() != null) {
            config.putSingle(WorkflowConstants.CONFIG_RESTART_IN_PROGRESS, rep.getRestartInProgress());
        }

        Workflow workflow = addWorkflow(new Workflow(session, rep.getId(), config));
        workflow.addSteps(rep.getSteps());

        // 添加步骤后校验条件类型与 workflow 资源类型兼容
        WorkflowValidator.validateWorkflowConditionType(session, workflow.getCondition(), workflow.getSupportedType());

        return workflow;
    }

    @Override
    public void close() {
    }

    private ComponentModel getWorkflowComponent(String id) {
        return this.getWorkflowComponent(id, WorkflowProvider.class.getName());
    }

    private ComponentModel getWorkflowComponent(String id, String providerType) {
        ComponentModel component = realm.getComponent(id);

        if (component == null || !Objects.equals(providerType, component.getProviderType())) {
            throw new BadRequestException("Not a valid workflow resource: " + id);
        }
        return component;
    }


    /* ================= workflow 组件 provider 与 factory 辅助 ================= */

    private WorkflowProvider getWorkflowProvider(Workflow workflow) {
        ComponentFactory<?, ?> factory = (ComponentFactory<?, ?>) sessionFactory
                .getProviderFactory(WorkflowProvider.class, DefaultWorkflowProviderFactory.ID);
        return (WorkflowProvider) factory.create(session, realm.getComponent(workflow.getId()));
    }

    private void processEvent(Stream<Workflow> workflows, WorkflowEvent event) {
        Map<String, ScheduledStep>[] scheduledSteps = new Map[] { null };

        workflows.filter(Workflow::isEnabled).forEach(workflow -> {

            EventBasedWorkflow provider = new EventBasedWorkflow(session, workflow.getSupportedType(), getWorkflowComponent(workflow.getId()));

            try {
                if (!provider.supports(event.getResourceType())) {
                    // 资源类型不匹配则跳过，避免加载无关调度状态
                    return;
                }

                DefaultWorkflowExecutionContext context = new DefaultWorkflowExecutionContext(session, workflow, event);

                if (scheduledSteps[0] == null) {
                    // 惰性加载该资源当前所有 workflow 调度状态
                    scheduledSteps[0] = stateProvider.getScheduledStepsByResource(event.getResourceId())
                            .collect(Collectors.toMap(ScheduledStep::workflowId, Function.identity()));
                }
                ScheduledStep scheduledStep = scheduledSteps[0].get(workflow.getId());

                // 资源尚未激活此 workflow：判断是否应激活
                if (scheduledStep == null) {
                    if (provider.activate(context)) {
                        if (isAlreadyScheduledInSession(event, workflow)) {
                            return;
                        }


                        // 配置了 notBefore 时先调度首步而非立即执行
                        if (DurationConverter.isPositiveDuration(workflow.getNotBefore())) {
                            scheduleWorkflow(context);
                        } else {
                            // 逐步执行或调度 workflow 步骤
                            runWorkflow(context);
                        }
                    }
                } else {
                    // 资源已激活：根据事件决定重启或停用
                    String executionId = scheduledStep.executionId();
                    String resourceId = scheduledStep.resourceId();
                    if (provider.restart(context)) {
                        restartWorkflow(new DefaultWorkflowExecutionContext(session, workflow, event, executionId), 0);
                        WorkflowProviderEvents.fireWorkflowRestartedEvent(session, workflow, resourceId, executionId);
                    } else if (provider.deactivate(context)) {
                        log.debugf("Workflow '%s' cancelled for resource %s (execution id: %s)", workflow.getName(), resourceId, executionId);
                        WorkflowProviderEvents.fireWorkflowDeactivatedEvent(session, workflow, resourceId, executionId, "event-based deactivation");
                        stateProvider.remove(executionId);
                    }
                }
            } catch (Exception e) {
                log.warnf("Error processing event %s for workflow %s: %s", event.getEventProviderId(), workflow.getName(), e.getMessage());
            }
        });
    }

    private boolean isAlreadyScheduledInSession(WorkflowEvent event, Workflow workflow) {
        @SuppressWarnings("unchecked")
        Map<String, Set<String>> scheduled = (Map<String, Set<String>>) session.getAttribute("kc.workflow.scheduled");

        if (scheduled == null) {
            scheduled = new HashMap<>();
            session.setAttribute("kc.workflow.scheduled", scheduled);
        }

        String resourceId = event.getResourceId();

        boolean isAlreadyScheduled = !scheduled.computeIfAbsent(resourceId, k -> new HashSet<>()).add(workflow.getId());

        if (isAlreadyScheduled) {
            log.debugf("Event %s for workflow %s and resource %s was previously processed for the resource", workflow.getName(), resourceId);
        }

        return isAlreadyScheduled;
    }

    private void scheduleWorkflow(WorkflowExecutionContext context) {
        executor.runTask(session, new ScheduleWorkflowTask((DefaultWorkflowExecutionContext) context));
    }

    private void runWorkflow(DefaultWorkflowExecutionContext context) {
        executor.runTask(session, new RunWorkflowTask(context));
    }

    private void restartWorkflow(DefaultWorkflowExecutionContext context, int position) {
        executor.runTask(session, new RestartWorkflowTask(context, position));
    }

    private WorkflowStepRepresentation toRepresentation(WorkflowStep step) {
        return new WorkflowStepRepresentation(step.getId(), step.getProviderId(), step.getConfig());
    }

    private Workflow addWorkflow(Workflow workflow) {
        ComponentModel model = new ComponentModel();

        model.setId(workflow.getId());
        model.setParentId(realm.getId());
        model.setProviderId(DefaultWorkflowProviderFactory.ID);
        model.setProviderType(WorkflowProvider.class.getName());

        MultivaluedHashMap<String, String> config = workflow.getConfig();

        if (config != null) {
            model.setConfig(config);
        }

        workflow = new Workflow(session, realm.addComponentModel(model));

        scheduleWorkflow(workflow);
        notifyScheduleChange(workflow, false);

        return workflow;
    }

    private void scheduleWorkflow(Workflow workflow) {
        String scheduled = workflow.getConfig().getFirst(WorkflowConstants.CONFIG_SCHEDULE_AFTER);

        if (workflow.isEnabled() && scheduled != null) {
            initLastScheduleRun(workflow);
            int intervalSecs = (int) DurationConverter.parseDuration(scheduled).toSeconds();
            int initialDelaySecs = ScheduledWorkflowRunner.computeInitialDelay(workflow, intervalSecs);
            TimerProvider timer = session.getProvider(TimerProvider.class);
            ScheduledWorkflowRunner runner = new ScheduledWorkflowRunner(workflow.getId(), realm.getId(), intervalSecs);
            timer.scheduleTask(runner, initialDelaySecs * 1000L, intervalSecs * 1000L);
            scheduleLog.debugf("Scheduled workflow '%s' with interval %d s, initial delay %d s", workflow.getName(), intervalSecs, initialDelaySecs);
        }
    }

    private void initLastScheduleRun(Workflow workflow) {
        if (ScheduledWorkflowRunner.getLastScheduleRun(workflow) <= 0) {
            ComponentModel component = realm.getComponent(workflow.getId());
            component.put(WorkflowConstants.CONFIG_LAST_SCHEDULE_RUN, String.valueOf(Time.currentTime()));
            realm.updateComponent(component);
        }
    }

    void cancelScheduledWorkflow(Workflow workflow) {
        session.getProvider(TimerProvider.class).cancelTask(ScheduledWorkflowRunner.taskName(workflow.getId()));
    }

    void rescheduleWorkflow(Workflow workflow) {
        cancelScheduledWorkflow(workflow);
        scheduleWorkflow(workflow);
    }

    private void notifyScheduleChange(Workflow workflow, boolean removed) {
        DefaultWorkflowProviderFactory factory = (DefaultWorkflowProviderFactory) sessionFactory
                .getProviderFactory(WorkflowProvider.class, DefaultWorkflowProviderFactory.ID);
        WorkflowScheduleEventListener listener = factory.getScheduleEventListener();

        if (listener != null) {
            int intervalSecs = 0;
            int lastScheduleRun = 0;

            if (!removed) {
                String scheduled = workflow.getConfig().getFirst(WorkflowConstants.CONFIG_SCHEDULE_AFTER);

                if (workflow.isEnabled() && scheduled != null) {
                    intervalSecs = (int) DurationConverter.parseDuration(scheduled).toSeconds();
                    lastScheduleRun = ScheduledWorkflowRunner.getLastScheduleRun(workflow);
                }
            }

            listener.notifyCluster(session, realm.getId(), workflow.getId(), removed, intervalSecs, lastScheduleRun);
        }
    }
}
