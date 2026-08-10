package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 用户联邦身份移除工作流事件工厂，provider ID 为 {@value #ID}（{@code user-federated-identity-removed}）。
 */
public class UserFedIdentityRemovedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：用户解除 IdP 身份关联时触发，可选按 IdP alias 过滤。 */
    public static final String ID = "user-federated-identity-removed";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new UserFedIdentityRemovedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
