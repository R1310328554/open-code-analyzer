package org.keycloak.protocol.docker;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.saml.profile.ecp.authenticator.HttpBasicAuthenticator;
import org.keycloak.representations.docker.DockerAccess;
import org.keycloak.representations.docker.DockerError;
import org.keycloak.representations.docker.DockerErrorResponseToken;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.jaxrs.ResponseBuilderImpl;

/**
 * Docker 协议 HTTP Basic 认证器：校验用户名密码，失败时返回 Docker 规范 JSON 错误令牌。
 * <p>无效凭据与未知用户返回相同错误消息，符合 Docker Registry 认证行为。</p>
 */
public class DockerAuthenticator extends HttpBasicAuthenticator {
    private static final Logger logger = Logger.getLogger(DockerAuthenticator.class);

    /** 认证器提供方 ID。 */
    public static final String ID = "docker-http-basic-authenticator";

    @Override
    /** 凭据无效时委托 {@link #invalidUserAction} 返回 Docker 错误响应。 */
    protected void notValidCredentialsAction(final AuthenticationFlowContext context, final RealmModel realm, final UserModel user) {
        invalidUserAction(context, realm, user.getUsername(), context.getSession().getContext().resolveLocale(user));
    }

    @Override
    /** 用户不存在时使用 Realm 默认语言返回与无效凭据相同的 Docker 错误。 */
    protected void nullUserAction(final AuthenticationFlowContext context, final RealmModel realm, final String userId) {
        final String localeString = Optional.ofNullable(realm.getDefaultLocale()).orElse(Locale.ENGLISH.toString());
        invalidUserAction(context, realm, userId, new Locale(localeString));
    }

    @Override
    /** 用户被禁用时返回 401 及 {@link DockerErrorResponseToken}。 */
    protected void userDisabledAction(AuthenticationFlowContext context, RealmModel realm, UserModel user, String eventError) {
        context.getEvent().user(user);
        context.getEvent().error(eventError);
        final DockerError error = new DockerError("UNAUTHORIZED","Invalid username or password.",
                Collections.singletonList(new DockerAccess(context.getAuthenticationSession().getClientNote(DockerAuthV2Protocol.SCOPE_PARAM))));
        context.failure(AuthenticationFlowError.USER_DISABLED, new ResponseBuilderImpl()
                .status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .entity(new DockerErrorResponseToken(Collections.singletonList(error)))
                .build());
    }

    /**
     * Docker 协议对无效凭据与错误用户名返回相同消息；SAML ECP 则区分二者。
     */
    /** 构造 UNAUTHORIZED Docker 错误并以 {@link AuthenticationFlowError#INVALID_USER} 失败认证流。 */
    private void invalidUserAction(final AuthenticationFlowContext context, final RealmModel realm, final String userId, final Locale locale) {
        context.getEvent().user(userId);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);

        final DockerError error = new DockerError("UNAUTHORIZED","Invalid username or password.",
                Collections.singletonList(new DockerAccess(context.getAuthenticationSession().getClientNote(DockerAuthV2Protocol.SCOPE_PARAM))));

        context.failure(AuthenticationFlowError.INVALID_USER, new ResponseBuilderImpl()
                .status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .entity(new DockerErrorResponseToken(Collections.singletonList(error)))
                .build());
    }

    @Override
    /** Docker Basic 认证对所有用户均视为已配置。 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }
}
