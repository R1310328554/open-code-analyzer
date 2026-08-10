package org.keycloak.models.workflow;

import java.util.List;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 移除用户属性步骤的 {@link WorkflowStepProviderFactory}，工厂 ID 为 {@code remove-user-attribute}。
 * <p>创建 {@link RemoveUserAttributeStepProvider}，通过 {@code attribute} 配置键指定要删除的属性名；当前未在 UI 暴露额外配置项。</p>
 */
public class RemoveUserAttributeStepProviderFactory implements WorkflowStepProviderFactory<RemoveUserAttributeStepProvider> {

    /** 工厂标识 {@code remove-user-attribute}。 */
    public static final String ID = "remove-user-attribute";

    /** 创建 {@link RemoveUserAttributeStepProvider} 实例。 */
    @Override
    public RemoveUserAttributeStepProvider create(KeycloakSession session, ComponentModel model) {
        return new RemoveUserAttributeStepProvider(session, model);
    }

    /** SPI 初始化（当前无操作）。 */
    @Override
    public void init(Config.Scope config) {
        // 无操作
    }

    /** 会话工厂后置初始化（当前无操作）。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // 无操作
    }

    /** 关闭工厂（当前无操作）。 */
    @Override
    public void close() {
        // 无操作
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
        return "Removes attributes from a user. Configure attributes to remove using the 'attribute' configuration key with the attribute names.";
    }

    /** @return 配置属性列表（当前为空，属性通过 {@code attribute} 键读取） */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        // 当前 UI 未暴露专用配置项，属性名从 attribute 配置键读取
        return List.of();
    }
}
