package org.keycloak.models.workflow;

import java.util.Objects;

import org.keycloak.cluster.ClusterEvent;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 工作流定时调度集群事件：在节点间同步 schedule 的创建、更新或删除。
 * <p>
 * 由 {@link WorkflowScheduleEventListener} 接收并触发本地定时器重调度。
 */
@ProtoTypeId(65621)
public class WorkflowScheduleClusterEvent implements ClusterEvent {

    /** 所属 realm ID。 */
    private String realmId;
    /** 工作流组件 ID。 */
    private String workflowId;
    /** 是否为删除/取消调度事件。 */
    private boolean removed;
    /** 调度间隔（秒）；removed 时可为 0。 */
    private int intervalSecs;
    /** 上次调度运行时间（epoch 秒），用于计算初始延迟。 */
    private int lastScheduleRun;

    @ProtoField(1)
    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    @ProtoField(2)
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    @ProtoField(3)
    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @ProtoField(value = 4, defaultValue = "0")
    public int getIntervalSecs() {
        return intervalSecs;
    }

    public void setIntervalSecs(int intervalSecs) {
        this.intervalSecs = intervalSecs;
    }

    @ProtoField(value = 5, defaultValue = "0")
    public int getLastScheduleRun() {
        return lastScheduleRun;
    }

    public void setLastScheduleRun(int lastScheduleRun) {
        this.lastScheduleRun = lastScheduleRun;
    }

    /**
     * 创建集群调度事件实例。
     *
     * @param realmId realm ID
     * @param workflowId 工作流 ID
     * @param removed 是否取消调度
     * @param intervalSecs 间隔秒数
     * @param lastScheduleRun 上次运行 epoch 秒
     */
    public static WorkflowScheduleClusterEvent create(String realmId, String workflowId, boolean removed,
            int intervalSecs, int lastScheduleRun) {
        WorkflowScheduleClusterEvent event = new WorkflowScheduleClusterEvent();
        event.setRealmId(realmId);
        event.setWorkflowId(workflowId);
        event.setRemoved(removed);
        event.setIntervalSecs(intervalSecs);
        event.setLastScheduleRun(lastScheduleRun);
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowScheduleClusterEvent that = (WorkflowScheduleClusterEvent) o;
        return removed == that.removed && intervalSecs == that.intervalSecs && lastScheduleRun == that.lastScheduleRun
                && Objects.equals(realmId, that.realmId) && Objects.equals(workflowId, that.workflowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(realmId, workflowId, removed, intervalSecs, lastScheduleRun);
    }
}
