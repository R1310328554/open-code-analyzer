package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 用户被授予角色时的工作流事件工厂，provider ID 为 {@value #ID}。
 */
public class UserRoleGrantedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：用户获得角色映射。 */
    public static final String ID = "user-role-granted";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new UserRoleGrantedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
