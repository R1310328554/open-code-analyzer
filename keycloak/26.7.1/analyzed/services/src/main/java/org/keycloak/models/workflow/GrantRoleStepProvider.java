package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

/**
 * 工作流步骤：向目标用户授予配置中指定的 Realm 或客户端角色。
 * <p>继承 {@link RoleBasedStepProvider}，在 {@link #run(UserModel, RoleModel)} 中调用 {@link UserModel#grantRole}。</p>
 */
public class GrantRoleStepProvider extends RoleBasedStepProvider {

    private final Logger log = Logger.getLogger(GrantRoleStepProvider.class);

    /** @param session Keycloak 会话 @param model 含角色列表的工作流步骤配置 */
    protected GrantRoleStepProvider(KeycloakSession session, ComponentModel model) {
        super(session, model);
    }

    /** 将指定 {@link RoleModel} 授予用户。 */
    @Override
    protected void run(UserModel user, RoleModel role) {
        log.debugv("Granting role {0} to user {1}", role.getName(), user.getId());
        user.grantRole(role);
    }
}
