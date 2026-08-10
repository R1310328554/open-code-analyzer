package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 用户被撤销角色时的工作流事件工厂，provider ID 为 {@value #ID}。
 */
public class UserRoleRevokedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：用户失去角色映射。 */
    public static final String ID = "user-role-revoked";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new UserRoleRevokedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
