package org.keycloak.representations.workflows;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import org.keycloak.common.util.MultivaluedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_AFTER;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_PRIORITY;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_SCHEDULED_AT;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_STATUS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_USES;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_WITH;

/**
 * 工作流单个步骤的表示，包含引用的提供者、依赖关系、优先级及执行状态。
 */
@JsonPropertyOrder({CONFIG_USES, CONFIG_AFTER, CONFIG_PRIORITY, CONFIG_WITH, CONFIG_SCHEDULED_AT, CONFIG_STATUS})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowStepRepresentation extends AbstractWorkflowComponentRepresentation {

    /** 步骤引用的提供者或动作 ID。 */
    private final String uses;
    /** 计划执行时间戳（毫秒）。 */
    private Long scheduledAt;
    /** 步骤当前执行状态。 */
    private StepExecutionStatus executionStatus;

    /** @return 步骤构建器 */
    public static Builder create() {
        return new Builder();
    }

    /** 默认构造函数。 */
    public WorkflowStepRepresentation() {
        this(null, null, null);
    }

    /**
     * 以提供者 ID 构造步骤。
     *
     * @param uses 提供者 ID
     */
    public WorkflowStepRepresentation(String uses) {
        this(null, uses, null);
    }

    /**
     * 以标识符、提供者 ID 与配置构造步骤。
     *
     * @param id     步骤标识
     * @param uses   提供者 ID
     * @param config 附加配置
     */
    public WorkflowStepRepresentation(String id, String uses, MultivaluedHashMap<String, String> config) {
        this(id, uses, config, null);
    }

    /**
     * 完整构造函数。
     *
     * @param id          步骤标识
     * @param uses        提供者 ID
     * @param config      附加配置
     * @param scheduledAt 计划执行时间戳
     */
    public WorkflowStepRepresentation(String id, String uses, MultivaluedHashMap<String, String> config, Long scheduledAt) {
        super(id, config);
        this.uses = uses;
        this.scheduledAt = scheduledAt;
    }

    /** @return 引用的提供者 ID */
    public String getUses() {
        return this.uses;
    }

    /**
     * 返回步骤附加配置，使用自定义序列化/反序列化器处理多值 Map。
     *
     * @return 配置 Map
     */
    @JsonSerialize(using = MultivaluedHashMapValueSerializer.class)
    @JsonDeserialize(using = MultivaluedHashMapValueDeserializer.class)
    @JsonInclude(value=JsonInclude.Include.NON_EMPTY, content=JsonInclude.Include.NON_NULL)
    public MultivaluedHashMap<String, String> getConfig() {
        return super.getConfig();
    }

    /** @return 前置步骤依赖表达式 */
    public String getAfter() {
        return getConfigValue(CONFIG_AFTER, String.class);
    }

    /** @param after 前置步骤依赖表达式 */
    public void setAfter(String after) {
        setConfig(CONFIG_AFTER, after);
    }

    /** @return 优先级（毫秒字符串） */
    @JsonIgnore
    public String getPriority() {
        return getConfigValue(CONFIG_PRIORITY, String.class);
    }

    /** @param ms 优先级（毫秒） */
    public void setPriority(long ms) {
        setConfig(CONFIG_PRIORITY, String.valueOf(ms));
    }

    /** @return 计划执行时间戳 */
    @JsonProperty(CONFIG_SCHEDULED_AT)
    public Long getScheduledAt() {
        return this.scheduledAt;
    }

    /** @param scheduledAt 计划执行时间戳 */
    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    /** @return 执行状态 */
    @JsonProperty(CONFIG_STATUS)
    public StepExecutionStatus getExecutionStatus() {
        return this.executionStatus;
    }

    /** @param executionStatus 执行状态 */
    public void setExecutionStatus(StepExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WorkflowStepRepresentation)) {
            return false;
        }
        WorkflowStepRepresentation that = (WorkflowStepRepresentation) obj;
        return Objects.equals(getUses(), that.getUses()) && Objects.equals(getConfig(), that.getConfig());
    }

    /** 流式 API 构建器，用于组装工作流步骤。 */
    public static class Builder {

        private WorkflowStepRepresentation step;

        /**
         * 指定步骤引用的提供者 ID。
         *
         * @param providerId 提供者 ID
         */
        public Builder of(String providerId) {
            this.step = new WorkflowStepRepresentation(providerId);
            return this;
        }

        /** @param duration 前置延迟 */
        public Builder after(Duration duration) {
            return after(String.valueOf(duration.getSeconds()));
        }

        /** @param after 前置步骤依赖表达式 */
        public Builder after(String after) {
            step.setAfter(after);
            return this;
        }

        /**
         * 设置单值配置项。
         *
         * @param key   配置键
         * @param value 配置值
         */
        public Builder withConfig(String key, String value) {
            step.setConfig(key, value);
            return this;
        }

        /**
         * 设置多值配置项。
         *
         * @param key   配置键
         * @param value 多个配置值
         */
        public Builder withConfig(String key, String... value) {
            step.setConfig(key, Arrays.asList(value));
            return this;
        }

        /** @return 构建完成的步骤 */
        public WorkflowStepRepresentation build() {
            return step;
        }
    }
}
