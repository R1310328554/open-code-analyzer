package org.keycloak.protocol.saml.profile.ecp.authenticator;

import java.util.Base64;
import java.util.List;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

/**
 * HTTP Basic 认证器：从 {@code Authorization: Basic} 请求头解析用户名密码并完成 SAML ECP 认证。
 * <p>用于 ECP profile 的非交互式客户端；失败时返回 401 及 WWW-Authenticate 头。</p>
 */
public class HttpBasicAuthenticator implements Authenticator {

    /** HTTP Basic 认证方案名 */
    private static final String BASIC = "Basic";
    private static final String BASIC_PREFIX = BASIC + " ";

    /** 解析 Basic 凭据、校验用户并设置认证结果或失败响应 */
    @Override
    public void authenticate(final AuthenticationFlowContext context) {
        final HttpRequest httpRequest = context.getHttpRequest();
        final HttpHeaders httpHeaders = httpRequest.getHttpHeaders();
        final String[] usernameAndPassword = getUsernameAndPassword(httpHeaders);

        context.attempted();

        if (usernameAndPassword != null) {
            final RealmModel realm = context.getRealm();
            final String username = usernameAndPassword[0];
            final UserModel user = context.getSession().users().getUserByUsername(realm, username);

            // 记录用户名以便暴力破解防护统计成功/失败
            context.getEvent().detail(Details.USERNAME, username);
            context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

            if (user != null) {
                final String password = usernameAndPassword[1];
                final boolean valid = user.credentialManager().isValid(UserCredentialModel.password(password));

                if (valid) {
                    if (isTemporarilyDisabledByBruteForce(context, user)) {
                        userDisabledAction(context, realm, user, Errors.USER_TEMPORARILY_DISABLED);
                    } else if (user.isEnabled()) {
                        userSuccessAction(context, user);
                    } else {
                        userDisabledAction(context, realm, user, Errors.USER_DISABLED);
                    }
                } else {
                    notValidCredentialsAction(context, realm, user);
                }
            } else {
                nullUserAction(context, realm, username);
            }
        }
    }

    /** 认证成功：绑定用户并标记 flow 成功 */
    protected void userSuccessAction(AuthenticationFlowContext context, UserModel user) {
        context.getAuthenticationSession().setAuthenticatedUser(user);
        context.success();
    }

    /** 用户禁用或临时锁定：记录事件并返回 401 */
    protected void userDisabledAction(AuthenticationFlowContext context, RealmModel realm, UserModel user, String eventError) {
        context.getEvent().user(user);
        context.getEvent().error(eventError);
        context.failure(AuthenticationFlowError.INVALID_USER, Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, BASIC_PREFIX + "realm=\"" + realm.getName() + "\"")
                .build());
    }

    /** 用户不存在时的默认处理（空实现，子类可覆盖） */
    protected void nullUserAction(final AuthenticationFlowContext context, final RealmModel realm, final String user) {
        // 默认无操作
    }

    /** 密码无效：记录事件并返回 401 */
    protected void notValidCredentialsAction(final AuthenticationFlowContext context, final RealmModel realm, final UserModel user) {
        context.getEvent().user(user);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
        context.failure(AuthenticationFlowError.INVALID_USER, Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, BASIC_PREFIX + "realm=\"" + realm.getName() + "\"")
                .build());
    }

    private boolean isTemporarilyDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {
        return (context.getRealm().isBruteForceProtected())
           && (context.getProtector().isTemporarilyDisabled(context.getSession(), context.getRealm(), user));
    }

    private String[] getUsernameAndPassword(final HttpHeaders httpHeaders) {
        final List<String> authHeaders = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION);

        if (authHeaders == null || authHeaders.size() == 0) {
            return null;
        }

        String credentials = null;

        for (final String authHeader : authHeaders) {
            if (authHeader.startsWith(BASIC_PREFIX)) {
                final String[] split = authHeader.trim().split("\\s+");

                if (split.length != 2) return null;

                credentials = split[1];
            }
        }

        try {
            String val = new String(Base64.getMimeDecoder().decode(credentials));
            int seperatorIndex = val.indexOf(":");
            if(seperatorIndex == -1) return new String[]{val};
            String user = val.substring(0, seperatorIndex);
            String pw = val.substring(seperatorIndex + 1);
            return new String[]{user,pw};
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("Failed to parse credentials.", e);
        }
    }

    /** ECP Basic 认证无表单提交步骤 */
    @Override
    public void action(final AuthenticationFlowContext context) {

    }

    /** {@inheritDoc} 由 Basic 头自行解析用户，不要求预先绑定 */
    @Override
    public boolean requiresUser() {
        return false;
    }

    /** {@inheritDoc} 不依赖 per-user 配置 */
    @Override
    public boolean configuredFor(final KeycloakSession session, final RealmModel realm, final UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(final KeycloakSession session, final RealmModel realm, final UserModel user) {

    }

    @Override
    public void close() {

    }
}
