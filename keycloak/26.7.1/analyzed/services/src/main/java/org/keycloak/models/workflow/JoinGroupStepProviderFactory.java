package org.keycloak.models.workflow;

import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * 加入组步骤的 {@link WorkflowStepProviderFactory}，工厂 ID 为 {@code join-group}。
 * <p>创建 {@link JoinGroupStepProvider} 实例，仅支持 {@link ResourceType#USERS} 资源；步骤通过 {@code group} 配置键指定一个或多个组路径。</p>
 */
public class JoinGroupStepProviderFactory implements WorkflowStepProviderFactory<JoinGroupStepProvider> {

    /** 工厂标识 {@code join-group}。 */
    public static final String ID = "join-group";

    /** 创建 {@link JoinGroupStepProvider} 实例。 */
    @Override
    public JoinGroupStepProvider create(KeycloakSession session, ComponentModel model) {
        return new JoinGroupStepProvider(session, model);
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
        return "Adds user to one or more groups";
    }
}
