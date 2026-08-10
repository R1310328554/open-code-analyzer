package org.keycloak.representations.workflows;

/**
 * 工作流定义与组件配置中使用的 JSON 键名常量集合。
 */
public final class WorkflowConstants {

    /** 临时（Ad-hoc）工作流标识。 */
    public static final String AD_HOC = "adhoc";
    /** 步骤或组件引用的提供者 ID 键。 */
    public static final String CONFIG_USES = "uses";
    /** 组件附加配置 Map 的 JSON 键。 */
    public static final String CONFIG_WITH = "with";
    /** 工作流支持的上下文或资源类型键。 */
    public static final String CONFIG_SUPPORTS = "supports";

    // 工作流（Workflow）顶层配置键
    /** 触发工作流的事件条件键。 */
    public static final String CONFIG_ON_EVENT = "on";
    /** 定时调度配置键。 */
    public static final String CONFIG_SCHEDULE = "schedule";
    /** 并发控制配置键。 */
    public static final String CONFIG_CONCURRENCY = "concurrency";
    /** 是否重启进行中的实例键。 */
    public static final String CONFIG_RESTART_IN_PROGRESS = "restart-in-progress";
    /** 是否取消进行中的实例键。 */
    public static final String CONFIG_CANCEL_IN_PROGRESS = "cancel-in-progress";
    /** 工作流名称键。 */
    public static final String CONFIG_NAME = "name";
    /** 工作流启用状态键。 */
    public static final String CONFIG_ENABLED = "enabled";
    /** 执行条件表达式键。 */
    public static final String CONFIG_CONDITIONS = "conditions";
    /** 步骤列表键。 */
    public static final String CONFIG_STEPS = "steps";
    /** 运行时错误信息键。 */
    public static final String CONFIG_ERROR = "error";
    /** 工作流状态键。 */
    public static final String CONFIG_STATE = "state";

    // 工作流条件（WorkflowCondition）配置键
    /** 条件表达式键。 */
    public static final String CONFIG_IF = "if";

    // 工作流步骤（WorkflowStep）配置键
    /** 前置步骤依赖键。 */
    public static final String CONFIG_AFTER = "after";
    /** 步骤优先级（毫秒）键。 */
    public static final String CONFIG_PRIORITY = "priority";
    /** 计划执行时间戳键。 */
    public static final String CONFIG_SCHEDULED_AT = "scheduled-at";
    /** 步骤执行状态键。 */
    public static final String CONFIG_STATUS = "status";

    // 工作流调度（WorkflowSchedule）配置键
    /** 调度延迟（after）的复合键。 */
    public static final String CONFIG_SCHEDULE_AFTER = "schedule." + CONFIG_AFTER;
    /** 批处理大小键。 */
    public static final String CONFIG_BATCH_SIZE = "batch-size";
    /** 调度批处理大小的复合键。 */
    public static final String CONFIG_SCHEDULE_BATCH_SIZE = "schedule." + CONFIG_BATCH_SIZE;
    /** 上次调度运行时间键。 */
    public static final String CONFIG_LAST_SCHEDULE_RUN = "schedule.lastRun";
}
