package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.WorkflowEventProvider;
import org.keycloak.models.workflow.WorkflowEventProviderFactory;

/**
 * 用户创建工作流事件工厂，provider ID 为 {@value #ID}（{@code user-created}）。
 */
public class UserCreatedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    /** 事件 provider 标识：用户注册或管理员创建用户时触发。 */
    public static final String ID = "user-created";

    @Override
    public WorkflowEventProvider create(KeycloakSession session, String configParameter) {
        return new UserCreatedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
