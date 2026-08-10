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
package org.keycloak.services.resources.account;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.enums.AccountRestApiVersion;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.AccessTokenIntrospectionProvider;
import org.keycloak.protocol.oidc.AccessTokenIntrospectionProviderFactory;
import org.keycloak.protocol.oidc.TokenIntrospectionProvider;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.Auth;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.AccountResourceProvider;
import org.keycloak.theme.Theme;

import org.jboss.logging.Logger;

/**
 * 账户服务入口加载器：根据 Accept/Content-Type 路由至 REST API 或控制台 HTML。
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AccountLoader {

    private final KeycloakSession session;
    private final EventBuilder event;

    private final HttpRequest request;
    private final HttpResponse response;

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(AccountLoader.class);

    /** 构造账户加载器 */
    public AccountLoader(KeycloakSession session, EventBuilder event) {
        this.session = session;
        this.event = event;
        this.request = session.getContext().getHttpRequest();
        this.response = session.getContext().getHttpResponse();
    }

    @Path("/")
    /**
     * 根路径分发：OPTIONS 预检、JSON 请求走 REST，否则走控制台提供者。
     * @return REST 服务、控制台资源或 CORS 预检服务
     */
    public Object getAccountService() {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = getAccountManagementClient(realm);

        HttpRequest request = session.getContext().getHttpRequest();
        HttpHeaders headers = session.getContext().getRequestHeaders();
        MediaType content = headers.getMediaType();
        List<MediaType> accepts = headers.getAcceptableMediaTypes();

        Theme theme = getTheme(session);
        UriInfo uriInfo = session.getContext().getUri();

        AccountResourceProvider accountResourceProvider = getAccountResourceProvider(theme);
        
        if (request.getHttpMethod().equals(HttpMethod.OPTIONS)) {
            return new CorsPreflightService();
        } else if ((accepts.contains(MediaType.APPLICATION_JSON_TYPE) || MediaType.APPLICATION_JSON_TYPE.equals(content)) && !uriInfo.getPath().endsWith("keycloak.json")) {
            return getAccountRestService(client, null);
        } else if (accountResourceProvider != null) {
            return accountResourceProvider.getResource();
        } else {
            throw new NotFoundException();
        }
    }

    @Path("{version : v\\d[0-9a-zA-Z_\\-]*}")
    @Produces(MediaType.APPLICATION_JSON)
    /** 版本化账户 REST API 入口（如 {@code v1}） */
    public Object getVersionedAccountRestService(final @PathParam("version") String version) {
        if (request.getHttpMethod().equals(HttpMethod.OPTIONS)) {
            return new CorsPreflightService();
        }
        return getAccountRestService(getAccountManagementClient(session.getContext().getRealm()), version);
    }

    /** 加载账户主题 */
    private Theme getTheme(KeycloakSession session) {
        try {
            return session.theme().getTheme(Theme.Type.ACCOUNT);
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
    }

    /** 校验 Bearer 令牌并构造 {@link AccountRestService} */
    private AccountRestService getAccountRestService(ClientModel client, String versionStr) {
        AccountRestService.checkAccountApiEnabled();

        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .authenticate();
        if (authResult == null) {
            throw new NotAuthorizedException("Bearer token required");
        }

        AccessToken accessToken = authResult.token();

        if (accessToken.getAudience() == null || accessToken.getResourceAccess(client.getClientId()) == null) {
            // 通过 introspection 转换令牌以获取 audience 等必需声明
            AccessTokenIntrospectionProvider provider = (AccessTokenIntrospectionProvider) session.getProvider(TokenIntrospectionProvider.class,
                    AccessTokenIntrospectionProviderFactory.ACCESS_TOKEN_TYPE);
            accessToken = provider.transformAccessToken(accessToken, authResult.session());
        }

        if (!accessToken.hasAudience(client.getClientId())) {
            throw new NotAuthorizedException("Invalid audience for client " + client.getClientId());
        }

        Auth auth = new Auth(session.getContext().getRealm(), accessToken, authResult.user(), client, authResult.session(), false);

        Cors.builder().checkAllowedOrigins(auth.getToken()).allowedMethods("GET", "PUT", "POST", "DELETE").auth().add();

        if (authResult.user().getServiceAccountClientLink() != null) {
            throw new NotAuthorizedException("Service accounts are not allowed to access this service");
        }

        AccountRestApiVersion version;
        if (versionStr == null) {
            version = AccountRestApiVersion.DEFAULT;
        }
        else {
            version = AccountRestApiVersion.get(versionStr);
            if (version == null) {
                throw new NotFoundException("API version not found");
            }
        }

        return new AccountRestService(session, auth, event, version);
    }

    /** 获取已启用的 account 管理客户端 */
    private ClientModel getAccountManagementClient(RealmModel realm) {
        ClientModel client = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
        if (client == null || !client.isEnabled()) {
            logger.debug("account management not enabled");
            throw new NotFoundException("account management not enabled");
        }
        return client;
    }

    /** 从主题配置或 SPI 解析 {@link AccountResourceProvider} */
    private AccountResourceProvider getAccountResourceProvider(Theme theme) {
        try {
            if (theme != null && theme.getProperties().containsKey(Theme.ACCOUNT_RESOURCE_PROVIDER_KEY)) {
                return session.getProvider(AccountResourceProvider.class, theme.getProperties().getProperty(Theme.ACCOUNT_RESOURCE_PROVIDER_KEY));
            }
        } catch (IOException ignore) {
        }
        return session.getProvider(AccountResourceProvider.class);
    }
}
