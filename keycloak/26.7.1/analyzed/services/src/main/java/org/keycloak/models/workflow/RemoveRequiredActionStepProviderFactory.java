package org.keycloak.models.workflow;

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 移除必需操作步骤的 {@link WorkflowStepProviderFactory}，工厂 ID 为 {@code remove-required-action}。
 * <p>创建 {@link RemoveRequiredActionStepProvider}，从用户账户清除登录后必须完成的操作（如更新密码、验证邮箱等）。</p>
 */
public class RemoveRequiredActionStepProviderFactory implements WorkflowStepProviderFactory<RemoveRequiredActionStepProvider> {

    /** 工厂标识 {@code remove-required-action}。 */
    public static final String ID = "remove-required-action";

    /** 创建 {@link RemoveRequiredActionStepProvider} 实例。 */
    @Override
    public RemoveRequiredActionStepProvider create(KeycloakSession session, ComponentModel model) {
        return new RemoveRequiredActionStepProvider(session, model);
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
        return "Removes a required action from a user";
    }
}
