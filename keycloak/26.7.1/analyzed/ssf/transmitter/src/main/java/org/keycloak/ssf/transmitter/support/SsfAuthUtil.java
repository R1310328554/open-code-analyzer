package org.keycloak.ssf.transmitter.support;

import java.util.List;
import java.util.regex.Pattern;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.ssf.Ssf;
import org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore;
import org.keycloak.utils.KeycloakSessionUtil;

import org.jboss.logging.Logger;

/**
 * SSF 发送方 REST 端点的 Bearer 令牌认证与权限检查工具。
 */
public class SsfAuthUtil {

    private static final Logger log = Logger.getLogger(SsfAuthUtil.class);

    /** 会话中缓存认证结果的属性键。 */
    public static final String AUTH_KEY = "auth";

    private static final Pattern SCOPE_DELIMITER = Pattern.compile(" ");

    /** 执行 Bearer 认证并将结果存入会话；失败抛出 401。 */
    public static AuthenticationManager.AuthResult authenticate() {
        KeycloakSession session = KeycloakSessionUtil.getKeycloakSession();
        var authenticator = new AppAuthManager.BearerTokenAuthenticator(session);
        var auth = authenticator.authenticate();
        if (auth == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        SsfAuthUtil.setAuth(session, auth);
        return auth;
    }

    private static void setAuth(KeycloakSession session, AuthenticationManager.AuthResult auth) {
        session.setAttribute(AUTH_KEY, auth);
    }

    private static AuthenticationManager.AuthResult getAuthResult() {
        return (AuthenticationManager.AuthResult) KeycloakSessionUtil.getKeycloakSession().getAttribute(AUTH_KEY);
    }

    /** 是否具备 SSF 管理 scope（{@link Ssf#SCOPE_SSF_MANAGE}）。 */
    public static boolean canManage() {
        return checkScopePermission(Ssf.SCOPE_SSF_MANAGE);
    }

    /** 是否具备 SSF 读 scope（{@link Ssf#SCOPE_SSF_READ}）。 */
    public static boolean canRead() {
        return checkScopePermission(Ssf.SCOPE_SSF_READ);
    }

    /**
     * 检查当前会话中的 Bearer 令牌是否具备指定 scope，并验证 SSF 接收方配置。
     *
     * @param scope 所需 OAuth scope
     * @return 通过全部检查时 {@code true}
     */
    public static boolean checkScopePermission(String scope) {

        // 0. 令牌必须有效
        var authResult = getAuthResult();
        if (authResult == null) {
            log.trace("SSF auth denied: no authentication result available");
            return false;
        }

        ClientModel client = authResult.client();
        if (client == null) {
            log.trace("SSF auth denied: authentication result carries no client");
            return false;
        }

        // 1. 客户端须配置为 SSF 接收方。
        if (!SsfUtil.isReceiverClient(client)) {
            log.tracef("SSF auth denied: client %s is not configured as an SSF receiver", client.getClientId());
            return false;
        }

        // …且客户端本身须启用。
        if (!client.isEnabled()) {
            log.tracef("SSF auth denied: SSF receiver client %s is disabled", client.getClientId());
            return false;
        }

        // 2. 服务账户检查（默认：属性缺失或非 "false" 时要求服务账户）
        String requireSaValue = client.getAttribute(ClientStreamStore.SSF_REQUIRE_SERVICE_ACCOUNT_KEY);
        boolean requireServiceAccount = !"false".equalsIgnoreCase(requireSaValue);
        if (requireServiceAccount) {
            if (!client.isServiceAccountsEnabled()) {
                log.tracef("SSF auth denied: service account required but not enabled for client %s", client.getClientId());
                return false;
            }
            // getServiceAccountClientLink() 返回内部客户端 UUID（见 UserModel.setServiceAccountClientLink
            // 与 ClientManager.enableServiceAccount）——非公开 clientId。与 client.getId() 比较以正确接受
            // 接收方自身服务账户 Bearer（link == client.getId()），并拒绝其他：普通用户（link == null）
            // 及其他客户端的服务账户（link == 其他 UUID）。
            if (!client.getId().equals(authResult.user().getServiceAccountClientLink())) {
                log.tracef("SSF auth denied: token user is not the service account for client %s", client.getClientId());
                return false;
            }
        }

        // 3. 角色检查（仅当已配置时）
        String requiredRole = client.getAttribute(ClientStreamStore.SSF_REQUIRED_ROLE_KEY);
        if (requiredRole != null && !requiredRole.isBlank()) {
            if (!hasRole(authResult, requiredRole)) {
                log.tracef("SSF auth denied: token missing required role '%s' for client %s", requiredRole, client.getClientId());
                return false;
            }
        }

        // 4. Scope 检查
        String tokenScope = authResult.token().getScope();
        if (tokenScope == null) {
            log.tracef("SSF auth denied: token has no scope claim for client %s", client.getClientId());
            return false;
        }

        boolean containsScope = List.of(SCOPE_DELIMITER.split(tokenScope)).contains(scope);
        if (!containsScope) {
            log.tracef("SSF auth denied: token missing required scope '%s' for client %s", scope, client.getClientId());
            return false;
        }

        // SSF 1.0 §8.1.1 inactivity_timeout：流管理或 poll 端点上的任意已认证访问
        // 均计为合格接收方活动，须重启不活动时钟。在此统一 stamp，各资源处理器无需重复。
        SsfActivityTracker.stamp(client);

        return true;
    }

    /**
     * 检查令牌是否携带给定角色。角色值格式与管理 UI 角色选择器一致：
     * <ul>
     *     <li>{@code roleName} — 作为领域角色检查。</li>
     *     <li>{@code clientId.roleName} — 作为指定客户端上的客户端角色检查。</li>
     * </ul>
     */
    public static boolean hasRole(AuthenticationManager.AuthResult authResult, String roleValue) {
        return hasRole(authResult.token(), roleValue);
    }

    /**
     * {@link #hasRole(AuthenticationManager.AuthResult, String)} 的令牌级重载。
     * 仅有解码 {@link AccessToken} 的调用方（例如管理 emit 端点走 {@code AdminAuth} 而非 SSF 接收方认证管道）
     * 可在无完整 {@code AuthResult} 时检查角色。
     */
    public static boolean hasRole(AccessToken token, String roleValue) {
        if (token == null || roleValue == null || roleValue.isBlank()) {
            return false;
        }

        int dot = roleValue.indexOf('.');
        if (dot > 0 && dot < roleValue.length() - 1) {
            // 客户端角色："clientId.roleName"
            String clientId = roleValue.substring(0, dot);
            String roleName = roleValue.substring(dot + 1);
            AccessToken.Access clientAccess = token.getResourceAccess(clientId);
            return clientAccess != null
                    && clientAccess.getRoles() != null
                    && clientAccess.getRoles().contains(roleName);
        }

        // 领域角色：纯 "roleName"
        AccessToken.Access realmAccess = token.getRealmAccess();
        return realmAccess != null
                && realmAccess.getRoles() != null
                && realmAccess.getRoles().contains(roleValue);
    }
}
