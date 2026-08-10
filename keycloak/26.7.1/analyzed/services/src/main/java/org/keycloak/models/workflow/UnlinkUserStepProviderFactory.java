package org.keycloak.models.workflow;

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 解除用户 IdP 联合绑定的工作流步骤工厂，ID 为 {@code unlink-user}。
 * <p>创建 {@link UnlinkUserStepProvider}，仅支持 {@link ResourceType#USERS} 资源类型；可配置一个或多个 IdP 别名或 {@code *} 解除全部联合。</p>
 */
public class UnlinkUserStepProviderFactory implements WorkflowStepProviderFactory<UnlinkUserStepProvider> {

    /** 步骤工厂标识 {@code unlink-user}。 */
    public static final String ID = "unlink-user";

    /** 创建绑定会话与组件模型的 {@link UnlinkUserStepProvider}。 */
    @Override
    public UnlinkUserStepProvider create(KeycloakSession session, ComponentModel model) {
        return new UnlinkUserStepProvider(session, model);
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
        return "Unlink a user from a configured Identity Provider or from all Identity Providers.";
    }
}
