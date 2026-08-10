package org.keycloak.representations.workflows;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_CANCEL_IN_PROGRESS;
import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_RESTART_IN_PROGRESS;

/**
 * 工作流并发策略表示，控制在已有实例运行时的取消或重启行为。
 */
@JsonPropertyOrder({CONFIG_CANCEL_IN_PROGRESS, CONFIG_RESTART_IN_PROGRESS})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowConcurrencyRepresentation {

    /** 进行中的实例是否应被取消。 */
    @JsonProperty(CONFIG_CANCEL_IN_PROGRESS)
    private String cancelInProgress;

    /** 进行中的实例是否应被重启。 */
    @JsonProperty(CONFIG_RESTART_IN_PROGRESS)
    private String restartInProgress;

    /** 无参构造函数，供 Jackson 反序列化使用。 */
    public WorkflowConcurrencyRepresentation() {}

    /**
     * 以重启与取消策略构造并发配置。
     *
     * @param restartInProgress 重启策略
     * @param cancelInProgress  取消策略
     */
    public WorkflowConcurrencyRepresentation(String restartInProgress, String cancelInProgress) {
        this.restartInProgress = restartInProgress;
        this.cancelInProgress = cancelInProgress;
    }

    /** @return 取消进行中实例的策略 */
    public String getCancelInProgress() {
        return cancelInProgress;
    }

    /** @param cancelInProgress 取消进行中实例的策略 */
    public void setCancelInProgress(String cancelInProgress) {
        this.cancelInProgress = cancelInProgress;
    }

    /** @return 重启进行中实例的策略 */
    public String getRestartInProgress() {
        return restartInProgress;
    }

    /** @param restartInProgress 重启进行中实例的策略 */
    public void setRestartInProgress(String restartInProgress) {
        this.restartInProgress = restartInProgress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cancelInProgress, restartInProgress);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkflowConcurrencyRepresentation that = (WorkflowConcurrencyRepresentation) obj;
        return Objects.equals(cancelInProgress, that.cancelInProgress) &&
               Objects.equals(restartInProgress, that.restartInProgress);
    }

}
