package org.keycloak.models.workflow;

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 离开组步骤的 {@link WorkflowStepProviderFactory}，工厂 ID 为 {@code leave-group}。
 * <p>创建 {@link LeaveGroupStepProvider} 实例，仅支持用户资源；步骤通过 {@code group} 配置键指定要退出的组路径列表。</p>
 */
public class LeaveGroupStepProviderFactory implements WorkflowStepProviderFactory<LeaveGroupStepProvider> {

    /** 工厂标识 {@code leave-group}。 */
    public static final String ID = "leave-group";

    /** 创建 {@link LeaveGroupStepProvider} 实例。 */
    @Override
    public LeaveGroupStepProvider create(KeycloakSession session, ComponentModel model) {
        return new LeaveGroupStepProvider(session, model);
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
        return "Removes a user from one or more groups";
    }
}
