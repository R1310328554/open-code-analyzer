/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.protocol.oidc;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.OAuthErrorException;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint;
import org.keycloak.protocol.oidc.endpoints.LoginStatusIframeEndpoint;
import org.keycloak.protocol.oidc.endpoints.LogoutEndpoint;
import org.keycloak.protocol.oidc.endpoints.ThirdPartyCookiesIframeEndpoint;
import org.keycloak.protocol.oidc.endpoints.TokenEndpoint;
import org.keycloak.protocol.oidc.endpoints.TokenRevocationEndpoint;
import org.keycloak.protocol.oidc.endpoints.UserInfoEndpoint;
import org.keycloak.protocol.oidc.ext.OIDCExtProvider;
import org.keycloak.protocol.oidc.utils.JWKSServerUtils;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.services.util.CacheControlUtil;

import org.jboss.resteasy.reactive.NoCache;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

/**
 * OIDC 协议 REST 资源根：挂载授权、令牌、UserInfo、登出、JWKS 等端点。
 * <p>路径前缀：{@code /realms/{realm}/protocol/openid-connect}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OIDCLoginProtocolService {

    /** 当前 Realm。 */
    private final RealmModel realm;
    /** 令牌管理器。 */
    private final TokenManager tokenManager;
    /** 事件构建器。 */
    private final EventBuilder event;

    private final KeycloakSession session;

    private final HttpHeaders headers;

    private final HttpRequest request;

    private final ClientConnection clientConnection;

    /** @param session Keycloak 会话
     * @param event 事件构建器 */
    public OIDCLoginProtocolService(KeycloakSession session, EventBuilder event) {
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.realm = session.getContext().getRealm();
        this.tokenManager = new TokenManager();
        this.event = event;
        this.request = session.getContext().getHttpRequest();
        this.headers = session.getContext().getRequestHeaders();
    }

    /** 构建 OIDC 协议服务基础 URI（含 realm 占位符）。 */
    public static UriBuilder tokenServiceBaseUrl(UriInfo uriInfo) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return tokenServiceBaseUrl(baseUriBuilder);
    }

    /** @param baseUriBuilder 服务器基础 URI 构建器 */
    public static UriBuilder tokenServiceBaseUrl(UriBuilder baseUriBuilder) {
        return baseUriBuilder.path(RealmsResource.class).path("{realm}/protocol/" + OIDCLoginProtocol.LOGIN_PROTOCOL);
    }

    /** 授权端点 URI 构建器。 */
    public static UriBuilder authUrl(UriInfo uriInfo) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return authUrl(baseUriBuilder);
    }

    /** @param baseUriBuilder 基础 URI */
    public static UriBuilder authUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "auth");
    }

    /** 注册端点 URI 构建器。 */
    public static UriBuilder registrationsUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "registrations");
    }

    /** 令牌端点 URI 构建器。 */
    public static UriBuilder tokenUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "token");
    }

    /** JWKS/certs 端点 URI 构建器。 */
    public static UriBuilder certsUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "certs");
    }

    /** UserInfo 端点 URI 构建器。 */
    public static UriBuilder userInfoUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "issueUserInfo");
    }

    /** 令牌自省端点 URI 构建器。 */
    public static UriBuilder tokenIntrospectionUrl(UriBuilder baseUriBuilder) {
        return tokenUrl(baseUriBuilder).path(TokenEndpoint.class, "introspect");
    }

    /** 登出端点 URI 构建器。 */
    public static UriBuilder logoutUrl(UriInfo uriInfo) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return logoutUrl(baseUriBuilder);
    }

    /** @param baseUriBuilder 基础 URI */
    public static UriBuilder logoutUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "logout");
    }

    /** 令牌吊销端点 URI 构建器。 */
    public static UriBuilder tokenRevocationUrl(UriBuilder baseUriBuilder) {
        UriBuilder uriBuilder = tokenServiceBaseUrl(baseUriBuilder);
        return uriBuilder.path(OIDCLoginProtocolService.class, "revoke");
    }

    /** OIDC 授权端点（{@link AuthorizationEndpoint}）。 */
    @Path("auth")
    public Object auth() {
        return new AuthorizationEndpoint(session, event);
    }

    /** 用户注册端点。 */
    @Path("registrations")
    public Object registrations(@QueryParam(Constants.TOKEN) String tokenString) {
        AuthorizationEndpoint endpoint = new AuthorizationEndpoint(session, event);
        return endpoint.register(tokenString);
    }

    /** 忘记凭据/重置密码端点。 */
    @Path("forgot-credentials")
    public Object forgotCredentialsPage() {
        AuthorizationEndpoint endpoint = new AuthorizationEndpoint(session, event);
        return endpoint.forgotCredentials();
    }

    /** OAuth2/OIDC 令牌端点。 */
    @Path("token")
    public Object token() {
        return new TokenEndpoint(session, tokenManager, event);
    }

    /** 登录状态 iframe（第三方 Cookie 检测辅助）。 */
    @Path("login-status-iframe.html")
    public Object getLoginStatusIframe() {
        return new LoginStatusIframeEndpoint(session);
    }

    /** 第三方 Cookie 检测 iframe 端点。 */
    @Path("3p-cookies")
    public Object thirdPartyCookiesCheck() {
        return new ThirdPartyCookiesIframeEndpoint(session);
    }

    @OPTIONS
    @Path("certs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVersionPreflight() {
        return Cors.builder().allowedMethods("GET").preflight().auth().add(Response.ok());
    }

    /** 返回 Realm JWKS（支持 application/jwk-set+json）。 */
    @GET
    @Path("certs")
    @Produces({MediaType.APPLICATION_JSON, org.keycloak.utils.MediaType.APPLICATION_JWKS})
    @NoCache
    public Response certs() {
        checkSsl();

        JSONWebKeySet keySet = JWKSServerUtils.getRealmJwks(session, realm);

        Response.ResponseBuilder responseBuilder = Response.ok(keySet).cacheControl(CacheControlUtil.getDefaultCacheControl());

        boolean isJwksRequest = org.keycloak.utils.MediaType.APPLICATION_JWKS.equals(this.headers.getHeaderString(HttpHeaders.ACCEPT));
        if (isJwksRequest) {
            responseBuilder.header(CONTENT_TYPE, org.keycloak.utils.MediaType.APPLICATION_JWKS);
        } else {
            responseBuilder.header(CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        return Cors.builder().allowAllOrigins().auth().add(responseBuilder);
    }

    /** UserInfo 端点。 */
    @Path("userinfo")
    public Object issueUserInfo() {
        return new UserInfoEndpoint(session, tokenManager);
    }

    /** RP 发起登出端点。 */
    @Path("logout")
    public Object logout() {
        return new LogoutEndpoint(session, tokenManager, event);
    }

    /** RFC 7009 令牌吊销端点。 */
    @Path("revoke")
    public Object revoke() {
        return new TokenRevocationEndpoint(session, event);
    }

    /** 原生/已安装应用 OOB 授权码回调页。 */
    @Path("oauth/oob")
    @GET
    public Response installedAppUrnCallback(final @QueryParam("code") String code, final @QueryParam("error") String error, final @QueryParam("error_description") String errorDescription) {
        LoginFormsProvider forms = session.getProvider(LoginFormsProvider.class);
        if (code != null) {
            return forms.setClientSessionCode(code).createCode();
        } else {
            return forms.setError(error).createCode();
        }
    }

    /** 解析 OIDC 扩展 Provider 子资源。 */
    @Path("ext/{extension}")
    public Object resolveExtension(@PathParam("extension") String extension) {
        OIDCExtProvider provider = session.getProvider(OIDCExtProvider.class, extension);
        if (provider != null) {
            provider.setEvent(event);
            return provider;
        }
        throw new NotFoundException();
    }

    /** 非 HTTPS 且 Realm 要求 SSL 时拒绝请求。 */
    private void checkSsl() {
        if (!session.getContext().getUri().getBaseUri().getScheme().equals("https")
                && realm.getSslRequired().isRequired(clientConnection)) {
            Cors cors = Cors.builder().auth().allowedMethods(request.getHttpMethod()).auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);
            throw new CorsErrorResponseException(cors.allowAllOrigins(), OAuthErrorException.INVALID_REQUEST, "HTTPS required",
                    Response.Status.FORBIDDEN);
        }
    }

}
