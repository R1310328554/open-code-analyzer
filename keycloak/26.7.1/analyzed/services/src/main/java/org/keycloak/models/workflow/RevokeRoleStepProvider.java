package org.keycloak.models.workflow;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

/**
 * 撤销角色工作流步骤：从用户移除配置的一个或多个角色映射。
 * <p>继承 {@link RoleBasedStepProvider}，在 {@link #run(UserModel, RoleModel)} 中调用 {@link UserModel#deleteRoleMapping(RoleModel)}。</p>
 */
public class RevokeRoleStepProvider extends RoleBasedStepProvider {

    private final Logger log = Logger.getLogger(RevokeRoleStepProvider.class);

    /** @param session Keycloak 会话 @param model 步骤组件配置 */
    protected RevokeRoleStepProvider(KeycloakSession session, ComponentModel model) {
        super(session, model);
    }

    /** 撤销指定用户的角色映射并记录调试日志。 */
    @Override
    protected void run(UserModel user, RoleModel role) {
        log.debugv("Revoking role {0} from user {1}", role.getName(), user.getId());
        user.deleteRoleMapping(role);
    }
}
