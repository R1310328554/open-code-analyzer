package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 用户认证工作流事件工厂，provider ID 为 {@value #ID}（{@code user-authenticated}）。
 */
public class UserAuthenticatedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：用户成功登录时触发，可选按 clientId 过滤。 */
    public static final String ID = "user-authenticated";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new UserAuthenticatedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
