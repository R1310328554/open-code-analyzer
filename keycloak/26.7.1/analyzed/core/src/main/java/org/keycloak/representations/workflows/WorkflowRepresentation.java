package org.keycloak.representations.workflows;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.keycloak.common.util.MultivaluedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_CANCEL_IN_PROGRESS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_CONCURRENCY;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_CONDITIONS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_ENABLED;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_IF;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_NAME;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_ON_EVENT;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_RESTART_IN_PROGRESS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_SCHEDULE;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_SCHEDULE_AFTER;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_SCHEDULE_BATCH_SIZE;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_STATE;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_STEPS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_SUPPORTS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_USES;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_WITH;

/**
 * 工作流定义的 JSON/API 表示，聚合触发条件、调度、并发策略、步骤列表及运行时状态。
 */
@JsonPropertyOrder({"id", CONFIG_NAME, CONFIG_USES, CONFIG_ENABLED, CONFIG_ON_EVENT, CONFIG_SCHEDULE, CONFIG_CONCURRENCY, CONFIG_IF, CONFIG_STEPS, CONFIG_STATE})
@JsonIgnoreProperties({CONFIG_WITH, CONFIG_SUPPORTS})
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class WorkflowRepresentation extends AbstractWorkflowComponentRepresentation {

    /**
     * 以工作流名称启动构建器。
     *
     * @param name 工作流名称
     * @return 构建器实例
     */
    public static Builder withName(String name) {
        return new Builder().withName(name);
    }

    /** 工作流步骤列表。 */
    private List<WorkflowStepRepresentation> steps;

    /** 运行时状态（含错误信息）。 */
    private WorkflowStateRepresentation state;

    /** 并发控制配置。 */
    @JsonProperty(CONFIG_CONCURRENCY)
    private WorkflowConcurrencyRepresentation concurrency;

    /** 定时调度配置。 */
    @JsonProperty(CONFIG_SCHEDULE)
    private WorkflowScheduleRepresentation schedule;

    /** 默认构造函数。 */
    public WorkflowRepresentation() {
        super(null, null);
    }

    /**
     * 完整构造函数。
     *
     * @param id     工作流标识
     * @param name   工作流名称
     * @param config 附加配置
     * @param steps  步骤列表
     */
    public WorkflowRepresentation(String id, String name, MultivaluedHashMap<String, String> config, List<WorkflowStepRepresentation> steps) {
        super(id, config);
        setName(name);
        this.steps = steps;
    }

    /** @return 触发事件条件 */
    public String getOn() {
        return getConfigValue(CONFIG_ON_EVENT, String.class);
    }

    /** @param eventConditions 触发事件条件 */
    public void setOn(String eventConditions) {
        setConfigValue(CONFIG_ON_EVENT, eventConditions);
    }

    /**
     * 返回调度配置；若尚未解析且配置中存在相关键，则懒加载构造。
     *
     * @return 调度配置，未配置时可能为 {@code null}
     */
    public WorkflowScheduleRepresentation getSchedule() {
        if (schedule == null) {
            String after = getConfigValue(CONFIG_SCHEDULE_AFTER, String.class);
            Integer batchSize = getConfigValue(CONFIG_SCHEDULE_BATCH_SIZE, Integer.class);

            if (after != null || batchSize != null) {
                this.schedule = new WorkflowScheduleRepresentation();
                this.schedule.setAfter(after);
                this.schedule.setBatchSize(batchSize);
            }
        }

        return this.schedule;
    }

    /** @param schedule 调度配置 */
    public void setSchedule(WorkflowScheduleRepresentation schedule) {
        this.schedule = schedule;
        if (schedule != null) {
            setConfigValue(CONFIG_SCHEDULE_AFTER, schedule.getAfter());
            setConfigValue(CONFIG_SCHEDULE_BATCH_SIZE, schedule.getBatchSize());
        }
    }

    /** @return 工作流名称 */
    public String getName() {
        return getConfigValue(CONFIG_NAME, String.class);
    }

    /** @param name 工作流名称 */
    public void setName(String name) {
        setConfigValue(CONFIG_NAME, name);
    }

    /** @return 是否启用 */
    public Boolean getEnabled() {
        return getConfigValue(CONFIG_ENABLED, Boolean.class);
    }

    /** @param enabled 是否启用 */
    public void setEnabled(Boolean enabled) {
        setConfigValue(CONFIG_ENABLED, enabled);
    }

    /** @return 执行条件表达式 */
    @JsonProperty(CONFIG_IF)
    public String getConditions() {
        return getConfigValue(CONFIG_CONDITIONS, String.class);
    }

    /** @param conditions 执行条件表达式 */
    public void setConditions(String conditions) {
        setConfigValue(CONFIG_CONDITIONS, conditions);
    }

    /** @param steps 步骤列表 */
    public void setSteps(List<WorkflowStepRepresentation> steps) {
        this.steps = steps;
    }

    /** @return 步骤列表 */
    public List<WorkflowStepRepresentation> getSteps() {
        return steps;
    }

    /**
     * 返回运行时状态；若无错误则返回 {@code null} 以省略 JSON 字段。
     *
     * @return 含错误信息的状态，或 {@code null}
     */
    public WorkflowStateRepresentation getState() {
        if (state == null) {
            state = new WorkflowStateRepresentation(this);
        }

        if (state.getErrors().isEmpty()) {
            return null;
        }

        return state;
    }

    /** @param state 运行时状态 */
    public void setState(WorkflowStateRepresentation state) {
        this.state = state;
    }

    /**
     * 返回并发配置；若尚未解析且配置中存在相关键，则懒加载构造。
     *
     * @return 并发配置，未配置时可能为 {@code null}
     */
    public WorkflowConcurrencyRepresentation getConcurrency() {
        String cancelInProgress = getConfigValue(CONFIG_CANCEL_IN_PROGRESS, String.class);
        String restartInProgress = getConfigValue(CONFIG_RESTART_IN_PROGRESS, String.class);
        if (this.concurrency == null) {
            if (cancelInProgress != null || restartInProgress != null) {
                this.concurrency = new WorkflowConcurrencyRepresentation();
                this.concurrency.setCancelInProgress(cancelInProgress);
                this.concurrency.setRestartInProgress(restartInProgress);
            }
        }
        return this.concurrency;
    }

    /** @param concurrency 并发配置 */
    public void setConcurrency(WorkflowConcurrencyRepresentation concurrency) {
        this.concurrency = concurrency;
        if (concurrency != null) {
            setConfigValue(CONFIG_CANCEL_IN_PROGRESS, concurrency.getCancelInProgress());
            setConfigValue(CONFIG_RESTART_IN_PROGRESS, concurrency.getRestartInProgress());
        }
    }

    /** @return 取消进行中实例的策略 */
    @JsonIgnore
    public String getCancelInProgress() {
        return concurrency != null ? concurrency.getCancelInProgress() : null;
    }

    /** @return 重启进行中实例的策略 */
    @JsonIgnore
    public String getRestartInProgress() {
        return concurrency != null ? concurrency.getRestartInProgress() : null;
    }

    /** @return 支持的上下文或资源类型 */
    public String getSupports() {
        return getConfigValue(CONFIG_SUPPORTS, String.class);
    }

    /** @param supports 支持的上下文或资源类型 */
    public void setSupports(String supports) {
        setConfigValue(CONFIG_SUPPORTS, supports);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WorkflowRepresentation)) {
            return false;
        }
        WorkflowRepresentation that = (WorkflowRepresentation) obj;
        return Objects.equals(getConfig(), that.getConfig()) && Objects.equals(getSteps(), that.getSteps());
    }

    /** 流式 API 构建器，用于组装完整工作流定义。 */
    public static class Builder {

        private WorkflowRepresentation representation;

        private Builder() {
            this.representation = new WorkflowRepresentation();
        }

        /** @param operation 触发事件 */
        public Builder onEvent(String operation) {
            representation.addConfigValue(CONFIG_ON_EVENT, operation);
            return this;
        }

        /** @param operation 多个触发事件（以 " or " 连接） */
        public Builder onEvent(String... operation) {
            return onEvent(String.join(" or ", operation));
        }

        /** @param condition 执行条件 */
        public Builder onCondition(String condition) {
            representation.setConditions(condition);
            return this;
        }

        /** 初始化并发配置块。 */
        public Builder concurrency() {
            if (representation.getConcurrency() == null) {
                representation.setConcurrency(new WorkflowConcurrencyRepresentation());
            }
            return this;
        }

        // 若并发能力扩展，可拆分为独立 Builder
        /** @param cancelInProgress 取消进行中实例的策略 */
        public Builder cancelInProgress(String cancelInProgress) {
            if (representation.getConcurrency() == null) {
                representation.setConcurrency(new WorkflowConcurrencyRepresentation());
            }
            representation.getConcurrency().setCancelInProgress(cancelInProgress);
            return this;
        }

        /** @param restartInProgress 重启进行中实例的策略 */
        public Builder restartInProgress(String restartInProgress) {
            if (representation.getConcurrency() == null) {
                representation.setConcurrency(new WorkflowConcurrencyRepresentation());
            }
            representation.getConcurrency().setRestartInProgress(restartInProgress);
            return this;
        }

        /** @param steps 工作流步骤 */
        public Builder withSteps(WorkflowStepRepresentation... steps) {
            representation.setSteps(Arrays.asList(steps));
            return this;
        }

        /**
         * 设置单值配置项。
         *
         * @param key   配置键
         * @param value 配置值
         */
        public Builder withConfig(String key, String value) {
            representation.addConfigValue(key, value);
            return this;
        }

        /**
         * 设置多值配置项。
         *
         * @param key    配置键
         * @param values 配置值列表
         */
        public Builder withConfig(String key, List<String> values) {
            representation.setConfigValue(key, values);
            return this;
        }

        /** @param name 工作流名称 */
        public Builder withName(String name) {
            representation.setName(name);
            return this;
        }

        /** @param schedule 调度配置 */
        public Builder schedule(WorkflowScheduleRepresentation schedule) {
            representation.setSchedule(schedule);
            return this;
        }

        /** @return 构建完成的工作流表示 */
        public WorkflowRepresentation build() {
            return representation;
        }
    }
}
