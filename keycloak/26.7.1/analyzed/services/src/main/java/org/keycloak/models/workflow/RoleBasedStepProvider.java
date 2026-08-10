package org.keycloak.models.workflow;

import java.util.List;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

/**
 * 基于角色的工作流步骤抽象基类：解析配置中的角色列表并对用户逐一执行子类逻辑。
 * <p>支持领域角色与 {@code clientId/roleName} 格式的客户端角色；子类实现 {@link #run(UserModel, RoleModel)} 定义授予或撤销等具体操作。</p>
 */
public abstract class RoleBasedStepProvider implements WorkflowStepProvider {

    private final Logger log = Logger.getLogger(RoleBasedStepProvider.class);
    /** 步骤配置键：角色名称列表（客户端角色用 clientId/roleName 格式）。 */
    public static final String CONFIG_ROLE = "role";

    private final KeycloakSession session;
    private final ComponentModel model;

    /** @param session Keycloak 会话 @param model 步骤组件配置 */
    public RoleBasedStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    /** 加载目标用户并按配置角色列表调用 {@link #run(UserModel, RoleModel)}。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        UserModel user = session.users().getUserById(getRealm(), context.getResourceId());

        if (user != null) {
            try {
                getRoles().forEach(role -> run(user, role));
            } catch (Exception e) {
                log.errorf(e, "Failed to grant role to user %s", user.getId());
            }
        }
    }

    /** 子类实现：对单个用户-角色对执行具体操作（授予或撤销等）。 */
    protected abstract void run(UserModel user, RoleModel role);

    /** 无资源需释放。 */
    @Override
    public void close() {
    }

    /** 从步骤配置解析并返回角色模型流。 */
    private Stream<RoleModel> getRoles() {
        return model.getConfig().getOrDefault(CONFIG_ROLE, List.of()).stream().map(this::getRole);
    }

    /** 按名称解析领域或客户端角色；不存在时抛出 {@link IllegalStateException}。 */
    private RoleModel getRole(String name) {
        RoleModel role;
        String[] parts = name.split("/");

        if (parts.length > 1) {
            ClientModel client = getRealm().getClientByClientId(parts[0]);

            if (client == null) {
                throw new IllegalStateException("Client with clientId " + parts[0] + " not found");
            }

            role = client.getRole(parts[1]);
        } else {
            role = getRealm().getRole(name);
        }

        if (role == null) {
            throw new IllegalStateException("Role " + name + " not found");
        }

        return role;
    }

    /** @return 当前会话上下文中的领域模型 */
    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }
}
