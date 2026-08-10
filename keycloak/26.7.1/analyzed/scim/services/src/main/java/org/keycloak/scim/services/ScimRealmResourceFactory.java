package org.keycloak.scim.services;

import java.net.URI;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.Config.Scope;
import org.keycloak.Token;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.representations.AccessToken;
import org.keycloak.scim.protocol.response.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AppAuthManager.BearerTokenAuthenticator;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.urls.UrlType;

import org.jboss.logging.Logger;

/**
 * SCIM 领域资源提供者工厂：校验 Bearer 令牌、受众与客户端类型后暴露 {@link ScimRealmResource}。
 * <p>仅在领域启用 SCIM API 且 {@link Feature#SCIM_API} 特性开启时生效。</p>
 */
public class ScimRealmResourceFactory implements RealmResourceProviderFactory, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(ScimRealmResourceFactory.class);

    /** 创建 SCIM 提供者；未启用 SCIM 时返回 null 并记录警告。 */
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();

        if (realm.isScimApiEnabled()) {
            return new RealmResourceProvider() {

                /** 认证并校验令牌受众后返回 SCIM 根资源。 */
                @Override
                public Object getResource() {
                    AuthResult authResult = new BearerTokenAuthenticator(session).authenticate();

                    if (authResult == null) {
                        logger.debug("SCIM request rejected: no valid bearer token provided");
                        throw new ErrorResponseException(Response.status(Status.UNAUTHORIZED)
                                .type(MediaType.APPLICATION_JSON)
                                .entity(new ErrorResponse("Bearer token required", Status.UNAUTHORIZED.getStatusCode()))
                                .build());
                    }

                    Token bearerToken = session.getContext().getBearerToken();

                    if (bearerToken == null) {
                        logger.debug("SCIM request rejected: bearer token could not be resolved");
                        throw new ErrorResponseException(Response.status(Status.UNAUTHORIZED)
                                .type(MediaType.APPLICATION_JSON)
                                .entity(new ErrorResponse("Bearer token required", Status.UNAUTHORIZED.getStatusCode()))
                                .build());
                    }

                    ClientModel client = authResult.client();

                    if (client.isPublicClient()) {
                        logger.debug("SCIM request rejected: public clients not allowed");
                        throw new ErrorResponseException(Response.status(Status.FORBIDDEN)
                                .type(MediaType.APPLICATION_JSON)
                                .entity(new ErrorResponse("Public client not allowed", Status.FORBIDDEN.getStatusCode()))
                                .build());
                    }

                    AccessToken accessToken = authResult.token();
                    URI frontendBaseUri = session.getContext().getUri(UrlType.FRONTEND).getBaseUri();
                    String scimAudience = Urls.realmBase(frontendBaseUri)
                            .path("{realm}/scim/v2")
                            .build(realm.getName())
                            .toString();

                    if (!accessToken.hasAudience(scimAudience)) {
                        logger.debug("SCIM request rejected: token does not contain the required audience");
                        throw new ErrorResponseException(Response.status(Status.UNAUTHORIZED)
                                .type(MediaType.APPLICATION_JSON)
                                .entity(new ErrorResponse("Invalid token audience", Status.UNAUTHORIZED.getStatusCode()))
                                .build());
                    }

                    return new ScimRealmResource(session);
                }

                @Override
                public void close() {

                }
            };
        }

        logger.warnf("SCIM API is not enabled for realm '%s'", realm.getName());
        return null;
    }

    /** 工厂初始化（无额外配置）。 */
    @Override
    public void init(Scope config) {
    }

    /** 启动后回调（占位）。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.toString();
    }

    /** 关闭工厂（无资源释放）。 */
    @Override
    public void close() {

    }

    /** 提供者 ID，固定为 "scim"。 */
    @Override
    public String getId() {
        return "scim";
    }

    /** 是否支持：取决于 SCIM_API 特性是否启用。 */
    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.SCIM_API);
    }
}
