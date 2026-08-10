package org.keycloak.models.workflow.events;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel.RoleRevokedEvent;
import org.keycloak.models.workflow.AbstractWorkflowEventProvider;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowExecutionContext;
import org.keycloak.provider.ProviderEvent;

/**
 * 用户被撤销角色时触发的工作流事件 provider。
 * <p>
 * 监听 {@link RoleRevokedEvent}；可选配置参数限定特定角色名（如 {@code user-role-revoked(myrole)}）。
 */
public class UserRoleRevokedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserRoleRevokedWorkflowEventProvider(final KeycloakSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    /** 本事件作用于用户资源。 */
    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    /** 仅支持角色撤销事件。 */
    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof RoleRevokedEvent;
    }

    /** 以被撤销角色的用户 ID 作为 workflow 资源标识。 */
    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof RoleRevokedEvent rre) {
            return rre.getUser().getId();
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
            // 角色名通过事件 provider 参数传入，例如 user-role-revoked(myrole)
            ProviderEvent roleEvent = (ProviderEvent) context.getEvent().getEvent();
            if (roleEvent instanceof RoleRevokedEvent roleRevokedEvent) {
                return configParameter.equals(roleRevokedEvent.getRole().getName());
            } else {
                return false;
            }
        } else {
            // 未配置角色名时，任意角色撤销事件均匹配
            return true;
        }
    }
}
