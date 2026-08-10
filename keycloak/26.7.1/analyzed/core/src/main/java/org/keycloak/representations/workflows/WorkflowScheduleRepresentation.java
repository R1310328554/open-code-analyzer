package org.keycloak.representations.workflows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_AFTER;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_BATCH_SIZE;

/**
 * 工作流定时调度配置表示，定义首次运行延迟与批处理大小。
 */
@JsonPropertyOrder({CONFIG_AFTER, CONFIG_BATCH_SIZE})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowScheduleRepresentation {

    /** 调度首次运行前的延迟表达式。 */
    private String after;

    /** 每批处理的实体数量。 */
    @JsonProperty(CONFIG_BATCH_SIZE)
    private Integer batchSize;

    /** @return 调度构建器 */
    public static Builder create() {
        return new Builder();
    }

    /** @return 延迟表达式 */
    public String getAfter() {
        return after;
    }

    /** @param after 延迟表达式 */
    public void setAfter(String after) {
        this.after = after;
    }

    /** @return 批处理大小 */
    public Integer getBatchSize() {
        return this.batchSize;
    }

    /** @param batchSize 批处理大小 */
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /** 流式 API 构建器，用于组装调度配置。 */
    public static class Builder {

        private final WorkflowScheduleRepresentation schedule = new WorkflowScheduleRepresentation();

        /** @param after 延迟表达式 */
        public Builder after(String after) {
            schedule.setAfter(after);
            return this;
        }

        /** @param batchSize 批处理大小 */
        public Builder batchSize(int batchSize) {
            schedule.setBatchSize(batchSize);
            return this;
        }

        /** @return 构建完成的调度配置 */
        public WorkflowScheduleRepresentation build() {
            return schedule;
        }
    }
}
