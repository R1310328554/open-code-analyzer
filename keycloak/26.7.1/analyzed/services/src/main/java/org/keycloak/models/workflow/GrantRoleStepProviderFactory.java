package org.keycloak.models.workflow;

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 授予角色工作流步骤工厂，ID 为 {@code grant-role}。
 * <p>创建 {@link GrantRoleStepProvider}，支持为一个或多个 Realm/客户端角色批量授权。</p>
 */
public class GrantRoleStepProviderFactory implements WorkflowStepProviderFactory<GrantRoleStepProvider> {

    /** 步骤工厂标识 {@code grant-role}。 */
    public static final String ID = "grant-role";

    /** 创建绑定会话与组件模型的 {@link GrantRoleStepProvider}。 */
    @Override
    public GrantRoleStepProvider create(KeycloakSession session, ComponentModel model) {
        return new GrantRoleStepProvider(session, model);
    }

    @Override
    public String getId() {
        return ID;
    }

    /** @return 仅支持用户资源类型 */
    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return Set.of(ResourceType.USERS);
    }

    /** @return 管理控制台步骤说明文本 */
    @Override
    public String getHelpText() {
        return "Grants one or more roles to a user";
    }
}
