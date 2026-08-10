package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 客户端认证工作流事件工厂，provider ID 为 {@value #ID}（{@code client-authenticated}）。
 */
public class ClientAuthenticatedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：客户端成功登录时触发。 */
    public static final String ID = "client-authenticated";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new ClientAuthenticatedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
