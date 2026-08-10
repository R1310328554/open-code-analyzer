package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 客户端创建工作流事件工厂，provider ID 为 {@value #ID}（{@code client-created}）。
 */
public class ClientCreatedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：新客户端创建时触发。 */
    public static final String ID = "client-created";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new ClientCreatedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
