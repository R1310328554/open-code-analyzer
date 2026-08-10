package org.keycloak.models.workflow;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;

/**
 * 工作流相关 {@link ProviderEvent} 的基础接口。
 * <p>描述工作流激活、步骤执行、迁移等生命周期事件。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface WorkflowProviderEvent extends ProviderEvent {

    /** 获取事件关联的 Keycloak 会话。 */
    /**
     * Gets the Keycloak session associated with this event.
     *
     * @return the {@link KeycloakSession}
     */
    KeycloakSession getKeycloakSession();

    /** 获取事件发生所在领域。 */
    /**
     * Gets the realm where the workflow event occurred.
     *
     * @return the {@link RealmModel}
     */
    RealmModel getRealm();

    /** 获取工作流 ID。 */
    /**
     * Gets the workflow ID.
     *
     * @return the workflow ID
     */
    String getWorkflowId();

    /** 获取工作流名称。 */
    /**
     * Gets the workflow name.
     *
     * @return the workflow name
     */
    String getWorkflowName();

    /** 获取关联资源 ID。 */
    /**
     * Gets the resource ID associated with this workflow event.
     *
     * @return the resource ID
     */
    String getResourceId();

    /** 获取关联资源类型。 */
    /**
     * Gets the resource type associated with this workflow event.
     *
     * @return the {@link ResourceType}
     */
    ResourceType getResourceType();


    /** 获取工作流执行 ID。 */
    /**
     * Gets the execution ID for this workflow activation.
     *
     * @return the execution ID
     */
    String getExecutionId();

    /** 工作流为资源激活时触发的事件。 */
    interface WorkflowActivatedEvent extends WorkflowProviderEvent {

        /**
         * Gets the trigger event type that activated the workflow.
         *
         * @return the event provider ID (e.g., "scheduled", "user-created", etc.)
         */
        String getTriggerEventType();
    }

    /** 工作流为资源停用时触发的事件。 */
    interface WorkflowDeactivatedEvent extends WorkflowProviderEvent {

        /**
         * Gets the reason for deactivation.
         *
         * @return the deactivation reason
         */
        String getReason();
    }

    /** 工作流为资源重启时触发的事件。 */
    interface WorkflowRestartedEvent extends WorkflowProviderEvent {
    }

    /** 工作流步骤被调度时触发的事件。 */
    interface WorkflowStepScheduledEvent extends WorkflowProviderEvent {

        /**
         * Gets the step ID.
         *
         * @return the step ID
         */
        String getStepId();

        /**
         * Gets the step provider ID.
         *
         * @return the step provider ID
         */
        String getStepProviderId();

        /**
         * Gets the scheduled time for the step.
         *
         * @return the scheduled time in milliseconds since epoch
         */
        long getScheduledTime();

        /**
         * Gets the delay duration before the step should be executed.
         *
         * @return the delay as a duration string (e.g., "PT5M" for 5 minutes)
         */
        String getDelay();
    }

    /** 工作流步骤成功执行时触发的事件。 */
    interface WorkflowStepExecutedEvent extends WorkflowProviderEvent {

        /**
         * Gets the step ID.
         *
         * @return the step ID
         */
        String getStepId();

        /**
         * Gets the step provider ID.
         *
         * @return the step provider ID
         */
        String getStepProviderId();
    }

    /** 工作流步骤执行失败时触发的事件。 */
    interface WorkflowStepFailedEvent extends WorkflowProviderEvent {

        /**
         * Gets the step ID.
         *
         * @return the step ID
         */
        String getStepId();

        /**
         * Gets the step provider ID.
         *
         * @return the step provider ID
         */
        String getStepProviderId();

        /**
         * Gets the error message.
         *
         * @return the error message
         */
        String getErrorMessage();
    }

    /** 工作流资源从一步骤/工作流迁移至另一处时触发的事件。 */
    interface WorkflowResourceMigratedEvent extends WorkflowProviderEvent {

        @Override
        default String getExecutionId() {
            return getOldExecutionId();
        }

        /**
         * Gets the source workflow ID.
         *
         * @return the source workflow ID
         */
        String getSourceWorkflowId();

        /**
         * Gets the source workflow name.
         *
         * @return the source workflow name
         */
        String getSourceWorkflowName();

        /**
         * Gets the destination workflow ID.
         *
         * @return the destination workflow ID
         */
        String getDestinationWorkflowId();

        /**
         * Gets the destination workflow name.
         *
         * @return the destination workflow name
         */
        String getDestinationWorkflowName();

        /**
         * Gets the source step ID.
         *
         * @return the source step ID
         */
        String getSourceStepId();

        /**
         * Gets the source step provider ID.
         *
         * @return the source step provider ID
         */
        String getSourceStepProviderId();

        /**
         * Gets the destination step ID.
         *
         * @return the destination step ID
         */
        String getDestinationStepId();

        /**
         * Gets the destination step provider ID.
         *
         * @return the destination step provider ID
         */
        String getDestinationStepProviderId();

        /**
         * Gets the old execution ID (from the source workflow).
         *
         * @return the old execution ID
         */
        String getOldExecutionId();

        /**
         * Gets the new execution ID (for the destination workflow).
         *
         * @return the new execution ID
         */
        String getNewExecutionId();
    }

    /** 工作流成功完成时触发的事件。 */
    interface WorkflowCompletedEvent extends WorkflowProviderEvent {
    }
}
