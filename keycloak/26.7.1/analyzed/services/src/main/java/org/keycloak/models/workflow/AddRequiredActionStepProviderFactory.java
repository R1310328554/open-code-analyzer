package org.keycloak.models.workflow;

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 添加必需操作工作流步骤工厂，ID 为 {@code add-required-action}。
 * <p>创建 {@link AddRequiredActionStepProvider}，仅支持 {@link ResourceType#USERS} 资源类型。</p>
 */
public class AddRequiredActionStepProviderFactory implements WorkflowStepProviderFactory<AddRequiredActionStepProvider> {

    /** 步骤工厂标识 {@code add-required-action}。 */
    public static final String ID = "add-required-action";

    /** 创建绑定会话与组件模型的 {@link AddRequiredActionStepProvider}。 */
    @Override
    public AddRequiredActionStepProvider create(KeycloakSession session, ComponentModel model) {
        return new AddRequiredActionStepProvider(session, model);
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
        return "Adds a required action to the user";
    }
}
