package org.keycloak.authentication.authenticators;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 透传认证器：不判定成功或失败，仅将流程上下文标记为 attempted（已尝试）。
 *
 * Pass-thru atheneticator that just sets the context to attempted.
 */
public class AttemptedAuthenticator implements Authenticator {

    /** 单例实例。 */
    public static final AttemptedAuthenticator SINGLETON = new AttemptedAuthenticator();
    @Override
    /** 将当前执行标记为 attempted 并继续流程。 */
    public void authenticate(AuthenticationFlowContext context) {
        context.attempted();

    }

    @Override
    /** 不应被调用。 */
    public void action(AuthenticationFlowContext context) {
        throw new RuntimeException("Unreachable!");

    }

    @Override
    /** 不依赖已认证用户。 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
