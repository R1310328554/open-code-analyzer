package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 用户离开组成员关系工作流事件工厂，provider ID 为 {@value #ID}。
 */
public class UserGroupMembershipRemovedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：用户被移出组。 */
    public static final String ID = "user-group-membership-removed";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new UserGroupMembershipRemovedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
