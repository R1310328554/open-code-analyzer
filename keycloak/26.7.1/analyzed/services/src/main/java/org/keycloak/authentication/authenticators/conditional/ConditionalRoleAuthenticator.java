package org.keycloak.authentication.authenticators.conditional;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

/**
 * 条件认证器：根据当前用户是否拥有指定角色决定子流程是否执行。
 * 支持取反配置，使「无该角色」时条件为真。
 */
public class ConditionalRoleAuthenticator implements ConditionalAuthenticator {
    /** 单例实例。 */
    public static final ConditionalRoleAuthenticator SINGLETON = new ConditionalRoleAuthenticator();
    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(ConditionalRoleAuthenticator.class);

    @Override
    /** 校验用户是否拥有配置的角色；取反时结果反转。 */
    public boolean matchCondition(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        RealmModel realm = context.getRealm();
        AuthenticatorConfigModel authConfig = context.getAuthenticatorConfig();
        if (user != null && authConfig!=null && authConfig.getConfig()!=null) {
            String requiredRole = authConfig.getConfig().get(ConditionalRoleAuthenticatorFactory.CONDITIONAL_USER_ROLE);
            boolean negateOutput = Boolean.parseBoolean(authConfig.getConfig().get(ConditionalRoleAuthenticatorFactory.CONF_NEGATE));
            RoleModel role = KeycloakModelUtils.getRoleFromString(context.getSession(), realm, requiredRole);
            if (role == null) {
                logger.errorv("Invalid role name submitted: {0}", requiredRole);
                return false;
            }

            return negateOutput != user.hasRole(role);
        }
        return false;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // 未使用
    }

    @Override
    /** @return 条件评估需要已识别用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Not used
    }

    @Override
    public void close() {
        // 无资源需释放
    }
}
