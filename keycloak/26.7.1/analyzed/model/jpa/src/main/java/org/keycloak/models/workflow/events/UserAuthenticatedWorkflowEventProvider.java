package org.keycloak.models.workflow.events;

import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.AbstractWorkflowEventProvider;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowExecutionContext;

/**
 * 用户认证工作流事件 provider：匹配 {@link EventType#LOGIN} 事件。
 * <p>
 * 配置参数可选指定 clientId，例如 {@code user-authenticated(account-console)}。
 */
public class UserAuthenticatedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserAuthenticatedWorkflowEventProvider(KeycloakSession session, String configParameter, String providerId) {
        super(session, configParameter,  providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(Event event) {
        return EventType.LOGIN.equals(event.getType());
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            // 配置参数为 clientId 时，须与登录事件的 clientId 一致
            Event loginEvent = (Event) context.getEvent().getEvent();
            return loginEvent != null && configParameter.equals(loginEvent.getClientId());
        } else {
            // 无额外参数时直接通过
            return true;
        }
    }
}
