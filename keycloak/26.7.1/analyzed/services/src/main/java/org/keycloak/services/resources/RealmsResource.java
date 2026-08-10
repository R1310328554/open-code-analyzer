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
package org.keycloak.services.resources;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.OAuthErrorException;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.AuthorizationService;
import org.keycloak.common.Profile;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.clientregistration.ClientRegistrationService;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.account.AccountLoader;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.services.util.ResolveRelative;
import org.keycloak.services.util.WellKnownProviderUtil;
import org.keycloak.utils.ProfileHelper;
import org.keycloak.wellknown.WellKnownProvider;
import org.keycloak.wellknown.WellKnownProviderFactory;

import org.jboss.logging.Logger;

import static org.keycloak.utils.MediaType.APPLICATION_JWT;

/**
 * 领域根 REST 资源（{@code /realms}）。
 * <p>路由到协议端点、登录操作、账户服务、身份代理、Well-Known 及 SPI 扩展子资源。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Provider
@Path("/realms")
public class RealmsResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(RealmsResource.class);

    /** 注入的 Keycloak 会话 */
    @Context
    protected KeycloakSession session;

    /** 构建领域基础 URL（基于请求 UriInfo） */
    public static UriBuilder realmBaseUrl(UriInfo uriInfo) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return realmBaseUrl(baseUriBuilder);
    }

    /** 构建领域基础 URL（基于已有 UriBuilder） */
    public static UriBuilder realmBaseUrl(UriBuilder baseUriBuilder) {
        return baseUriBuilder.path(RealmsResource.class).path(RealmsResource.class, "getRealmResource");
    }

    /** 构建账户服务 URL */
    public static UriBuilder accountUrl(UriBuilder base) {
        return base.path(RealmsResource.class).path(RealmsResource.class, "getAccountService");
    }

    /** 构建协议端点 URL（UriInfo 版本） */
    public static UriBuilder protocolUrl(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(RealmsResource.class).path(RealmsResource.class, "getProtocol");
    }

    /** 构建协议端点 URL（UriBuilder 版本） */
    public static UriBuilder protocolUrl(UriBuilder builder) {
        return builder.path(RealmsResource.class).path(RealmsResource.class, "getProtocol");
    }

    /** 构建客户端动态注册 URL */
    public static UriBuilder clientRegistrationUrl(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(RealmsResource.class).path(RealmsResource.class, "getClientsService");
    }

    /** 构建身份代理服务 URL */
    public static UriBuilder brokerUrl(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(RealmsResource.class).path(RealmsResource.class, "getBrokerService");
    }

    /** 构建领域级 Well-Known URL */
    public static UriBuilder wellKnownProviderUrl(UriBuilder builder) {
        return builder.path(RealmsResource.class).path(RealmsResource.class, "getWellKnown");
    }

    @Path("{realm}/protocol/{protocol}")
    /**
     * 获取指定协议端点（OIDC/SAML 等）。
     * @param name 领域名称
     * @param protocol 协议 ID
     * @return 协议端点实例
     */
    public Object getProtocol(final @PathParam("realm") String name,
                              final @PathParam("protocol") String protocol) {
        resolveRealmAndUpdateSession(name);

        LoginProtocolFactory factory = (LoginProtocolFactory)session.getKeycloakSessionFactory().getProviderFactory(LoginProtocol.class, protocol);
        if(factory == null){
            logger.debugf("protocol %s not found", protocol);
            throw new NotFoundException("Protocol not found");
        }

        EventBuilder event = new EventBuilder(session.getContext().getRealm(), session, session.getContext().getConnection());

        return factory.createProtocolEndpoint(session, event);
    }

    /**
     * 根据 clientId 解析客户端 URL 并返回 302 重定向。
     * <p>
     * This allows a client to refer to other clients just by their client id in URLs, will then redirect users to the actual client url.
     * The client url is derived according to the rules of the base url in the client configuration.
     * </p>
     *
     * @param realmName
     * @param clientId
     * @return
     * @since 2.0
     */
    @GET
    @Path("{realm}/clients/{client_id}/redirect")
    /** {@inheritDoc} */
    public Response getRedirect(final @PathParam("realm") String realmName, final @PathParam("client_id") String clientId) {
        resolveRealmAndUpdateSession(realmName);

        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(clientId);

        if (client == null) {
            return null;
        }

        if (client.getRootUrl() == null && client.getBaseUrl() == null) {
            return null;
        }


        URI targetUri;
        if (client.getRootUrl() != null && (client.getBaseUrl() == null || client.getBaseUrl().isEmpty())) {
            targetUri = KeycloakUriBuilder.fromUri(client.getRootUrl()).build();
        } else {
            targetUri = KeycloakUriBuilder.fromUri(ResolveRelative.resolveRelativeUri(session, client.getRootUrl(), client.getBaseUrl())).build();
        }

        return Response.seeOther(targetUri).build();
    }

    @Path("{realm}/login-actions")
    /** 获取领域登录操作子资源 */
    public LoginActionsService getLoginActionsService(final @PathParam("realm") String name) {
        resolveRealmAndUpdateSession(name);
        EventBuilder event = new EventBuilder(session.getContext().getRealm(), session, session.getContext().getConnection());
        return new LoginActionsService(session, event);
    }

    @Path("{realm}/clients-registrations")
    /** 获取客户端动态注册子资源 */
    public ClientRegistrationService getClientsService(final @PathParam("realm") String name) {
        resolveRealmAndUpdateSession(name);
        EventBuilder event = new EventBuilder(session.getContext().getRealm(), session, session.getContext().getConnection());
        return new ClientRegistrationService(session, event);
    }

    @Path("{realm}/clients-managements")
    /** 获取客户端管理子资源 */
    public ClientsManagementService getClientsManagementService(final @PathParam("realm") String name) {
        resolveRealmAndUpdateSession(name);
        EventBuilder event = new EventBuilder(session.getContext().getRealm(), session, session.getContext().getConnection());
        return new ClientsManagementService(session, event);
    }

    /** 解析领域并写入会话上下文（实例方法） */
    private void resolveRealmAndUpdateSession(String realmName) {
        resolveRealmAndUpdateSession(session, realmName);
    }

    /** 解析领域并写入会话上下文（静态方法） */
    private static void resolveRealmAndUpdateSession(KeycloakSession session, String realmName) {
        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        if (realm == null) {
            throw new NotFoundException("Realm does not exist");
        }
        session.getContext().setRealm(realm);
    }

    @Path("{realm}/account")
    /** 获取账户控制台子资源 */
    public Object getAccountService(final @PathParam("realm") String name) {
        resolveRealmAndUpdateSession(name);
        EventBuilder event = new EventBuilder(session.getContext().getRealm(), session, session.getContext().getConnection());
        return new AccountLoader(session, event);
    }

    @Path("{realm}")
    /** 获取领域公开信息子资源 */
    public PublicRealmResource getRealmResource(final @PathParam("realm") String name) {
        resolveRealmAndUpdateSession(name);
        return new PublicRealmResource(session);
    }

    @Path("{realm}/broker")
    /** 获取身份代理（IdP 联邦）子资源并初始化事件 */
    public IdentityBrokerService getBrokerService(final @PathParam("realm") String name) {
        resolveRealmAndUpdateSession(name);

        IdentityBrokerService brokerService = new IdentityBrokerService(session);

        brokerService.init();

        return brokerService;
    }

    @OPTIONS
    @Path("{realm}/.well-known/{provider}")
    @Produces(MediaType.APPLICATION_JSON)
    /** Well-Known 端点 CORS 预检 */
    public Response getVersionPreflight(final @PathParam("realm") String name,
                                        final @PathParam("provider") String providerName) {
        return Cors.builder().allowedMethods("GET").preflight().auth().add(Response.ok());
    }

    @GET
    @Path("{realm}/.well-known/{alias}")
    @Produces({MediaType.APPLICATION_JSON, APPLICATION_JWT})
    /** 获取领域 Well-Known 配置（JSON 或 JWT） */
    public Response getWellKnown(final @PathParam("realm") String realm,
                                 final @PathParam("alias") String alias) {
        return getWellKnownResponse(session, realm, alias, logger);
    }

    /**
     * 解析并返回 Well-Known 响应（静态入口，供 {@link ServerMetadataResource} 复用）。
     * @param session Keycloak 会话
     * @param realm 领域名称
     * @param alias Well-Known 别名
     * @param logger 日志记录器
     * @return Well-Known 配置响应
     */
    public static Response getWellKnownResponse(KeycloakSession session, String realm, String alias, Logger logger) throws NotFoundException {
        resolveRealmAndUpdateSession(session, realm);
        checkSsl(session, session.getContext().getRealm());

        WellKnownProviderFactory wellKnownProviderFactoryFound = WellKnownProviderUtil.resolveFromAlias(session.getKeycloakSessionFactory(), alias)
                .orElseThrow(NotFoundException::new);

        logger.tracef("Use provider with ID '%s' for well-known alias '%s'", wellKnownProviderFactoryFound.getId(), alias);

        WellKnownProvider wellKnown = session.getProvider(WellKnownProvider.class, wellKnownProviderFactoryFound.getId());

        if (wellKnown != null) {
            Object config = wellKnown.getConfig();
            Response.ResponseBuilder responseBuilder;

            // 根据返回类型选择 JWT 或 JSON 媒体类型
            responseBuilder = Response.ok(config).type(config instanceof String ? APPLICATION_JWT : MediaType.APPLICATION_JSON);

            return Cors.builder().allowAllOrigins().auth().add(responseBuilder.cacheControl(CacheControlUtil.noCache()));
        }

        throw new NotFoundException();
    }

    @Path("{realm}/authz")
    /** 获取授权服务子资源（需启用 AUTHORIZATION 特性） */
    public Object getAuthorizationService(@PathParam("realm") String name) {
        ProfileHelper.requireFeature(Profile.Feature.AUTHORIZATION);

        resolveRealmAndUpdateSession(name);
        AuthorizationProvider authorization = this.session.getProvider(AuthorizationProvider.class);
        return new AuthorizationService(authorization);
    }

    /**
     * 通过 {@link org.keycloak.services.resource.RealmResourceSPI} 解析 REST 扩展子资源。 to resolve sub-resources instances given an <code>unknownPath</code>.
     *
     * @param extension a path that could be to a REST extension
     * @return a JAX-RS sub-resource instance for the REST extension if found. Otherwise null is returned.
     */
    @Path("{realm}/{extension}")
    /** {@inheritDoc} */
    public Object resolveRealmExtension(@PathParam("realm") String realmName, @PathParam("extension") String extension) {
        resolveRealmAndUpdateSession(realmName);

        RealmResourceProvider provider = session.getProvider(RealmResourceProvider.class, extension);

        if (provider != null) {
            Object resource = provider.getResource();

            if (resource != null) {
                return resource;
            }
        }

        throw new NotFoundException();
    }

    /** 校验当前请求是否满足领域 SSL 要求（实例方法） */
    private void checkSsl(RealmModel realm) {
        checkSsl(session, realm);
    }

    /** 校验 HTTPS 要求，不满足时抛出 CORS 错误 */
    private static void checkSsl(KeycloakSession session, RealmModel realm) {
        if (!"https".equals(session.getContext().getUri().getBaseUri().getScheme())
                && realm.getSslRequired().isRequired(session.getContext().getConnection())) {
            HttpRequest request = session.getContext().getHttpRequest();
            Cors cors = Cors.builder().auth().allowedMethods(request.getHttpMethod()).auth().exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS);
            throw new CorsErrorResponseException(cors.allowAllOrigins(), OAuthErrorException.INVALID_REQUEST, "HTTPS required",
                    Response.Status.FORBIDDEN);
        }
    }
}
