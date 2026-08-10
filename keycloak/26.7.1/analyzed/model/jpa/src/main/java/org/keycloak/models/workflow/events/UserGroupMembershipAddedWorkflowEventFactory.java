package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 用户加入群组工作流事件工厂，provider ID 为 {@value #ID}（{@code user-group-membership-added}）。
 */
public class UserGroupMembershipAddedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：用户被加入群组时触发，可选按群组路径过滤。 */
    public static final String ID = "user-group-membership-added";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new UserGroupMembershipAddedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
