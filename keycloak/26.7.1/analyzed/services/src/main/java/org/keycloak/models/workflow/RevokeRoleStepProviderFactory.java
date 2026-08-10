package org.keycloak.models.workflow;

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 撤销角色步骤的 {@link WorkflowStepProviderFactory}，工厂 ID 为 {@code revoke-role}。
 * <p>创建 {@link RevokeRoleStepProvider}，从用户移除领域或客户端角色；步骤通过 {@code role} 配置键指定角色名（客户端角色格式为 clientId/roleName）。</p>
 */
public class RevokeRoleStepProviderFactory implements WorkflowStepProviderFactory<RevokeRoleStepProvider> {

    /** 工厂标识 {@code revoke-role}。 */
    public static final String ID = "revoke-role";

    /** 创建 {@link RevokeRoleStepProvider} 实例。 */
    @Override
    public RevokeRoleStepProvider create(KeycloakSession session, ComponentModel model) {
        return new RevokeRoleStepProvider(session, model);
    }

    /** @return 工厂 ID {@link #ID} */
    @Override
    public String getId() {
        return ID;
    }

    /** @return 支持的用户资源类型集合 */
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return Set.of(ResourceType.USERS);
    }

    /** @return 管理控制台显示的步骤说明文本 */
    @Override
    public String getHelpText() {
        return "Revokes roles assigned to the user";
    }
}
