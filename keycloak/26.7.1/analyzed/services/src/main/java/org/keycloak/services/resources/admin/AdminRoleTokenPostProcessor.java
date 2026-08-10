package org.keycloak.services.resources.admin;

import java.util.Map;
import java.util.Map.Entry;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.util.TokenUtil;

import static org.keycloak.models.utils.KeycloakModelUtils.removeTransientAdminRoles;

/**
 * 管理角色令牌后处理器。
 * <p>从访问令牌中移除用户未显式授予的临时管理角色（realm 与 resource access）。</p>
 */
public class AdminRoleTokenPostProcessor implements TokenPostProcessor {

    /** Keycloak 会话 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public AdminRoleTokenPostProcessor(KeycloakSession session) {
        this.session = session;
    }

    /** {@inheritDoc} 清理 realmAccess 与各 resourceAccess 中的临时管理角色。 */
    @Override
    public void process(TokenPostProcessorContext context) {
        ClientSessionContext clientSessionCtx = context.clientSessionCtx();
        AuthenticatedClientSessionModel clientSession = clientSessionCtx.getClientSession();
        UserSessionModel userSession = clientSession.getUserSession();
        UserModel user = userSession.getUser();
        RealmModel realm = session.getContext().getRealm();
        AccessToken accessToken = context.accessToken();

        TokenUtil.convertTokenRolesFromOtherClaims(accessToken);

        removeTransientAdminRoles(realm, null, user, accessToken.getRealmAccess());

        Map<String, Access> resourceAccess = accessToken.getResourceAccess();

        for (Entry<String, Access> access : resourceAccess.entrySet()) {
            removeTransientAdminRoles(realm, access.getKey(), user, access.getValue());
        }
    }
}
