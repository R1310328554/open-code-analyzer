package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel.RoleGrantedEvent;
import org.keycloak.models.workflow.AbstractWorkflowEventProvider;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowExecutionContext;
import org.keycloak.provider.ProviderEvent;

/**
 * 用户被授予角色时触发的工作流事件 provider。
 * <p>
 * 监听 {@link RoleGrantedEvent}；可选配置参数限定特定角色名（如 {@code user-role-granted(myrole)}）。
 */
public class UserRoleGrantedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserRoleGrantedWorkflowEventProvider(final KeycloakSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    /** 本事件作用于用户资源。 */
    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    /** 仅支持角色授予事件。 */
    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof RoleGrantedEvent;
    }

    /** 以被授权用户的 ID 作为 workflow 资源标识。 */
    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof RoleGrantedEvent rge) {
            return rge.getUser().getId();
        }
        return null;
    }

    /**
     * 评估事件是否匹配：先执行基类校验，再按配置参数比对角色名。
     */
    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            // 角色名通过事件 provider 参数传入，例如 user-role-granted(myrole)
            ProviderEvent roleEvent = (ProviderEvent) context.getEvent().getEvent();
            if (roleEvent instanceof RoleGrantedEvent roleGrantedEvent) {
                return configParameter.equals(roleGrantedEvent.getRole().getName());
            } else {
                return false;
            }
        } else {
            // 未配置角色名时，任意角色授予事件均匹配
            return true;
        }
    }
}
