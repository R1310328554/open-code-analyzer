/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
import jakarta.ws.rs.ext.Provider;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.util.WellKnownProviderUtil;
import org.keycloak.wellknown.WellKnownProviderFactory;

import org.jboss.logging.Logger;

/**
 * 服务端 Well-Known 元数据资源。
 * <p>暴露 {@code /.well-known/{provider}/realms/{realm}} 路径，供 OIDC/OAuth 等协议发现端点使用。</p>
 */
@Provider
@Path("/.well-known")
public class ServerMetadataResource {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ServerMetadataResource.class);

    /** 注入的 Keycloak 会话 */
    @Context
    protected KeycloakSession session;

    @OPTIONS
    @Path("{provider}/realms/{realm}")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * Well-Known 端点 CORS 预检。
     * @param alias Well-Known 提供方别名
     * @param realm 领域名称
     * @return 预检响应
     */
    public Response getWellKnownPreflight(final @PathParam("provider") String alias,
                                          final @PathParam("realm") String realm) {
        if (!isValidProvider(alias)) {
            throw new NotFoundException();
        }
        return Cors.builder().allowedMethods("GET").preflight().auth().add(Response.ok());
    }

    @GET
    @Path("{provider}/realms/{realm}")
    @Produces({MediaType.APPLICATION_JSON, org.keycloak.utils.MediaType.APPLICATION_JWT})
    /**
     * 获取指定提供方与领域的 Well-Known 配置。
     * @param alias Well-Known 提供方别名
     * @param realm 领域名称
     * @return JSON 或 JWT 格式的配置
     */
    public Response getWellKnown(final @PathParam("provider") String alias,
                                 final @PathParam("realm") String realm) {
        if (!isValidProvider(alias)) {
            throw new NotFoundException();
        }
        return RealmsResource.getWellKnownResponse(session, realm, alias, logger);
    }

    /**
     * @Deprecated 请改用 {@link #wellKnownProviderUrl(UriBuilder)}。
     * @return the updated UriBuilder instance.
     */
    @Deprecated
    public static UriBuilder wellKnownOAuthProviderUrl(UriBuilder builder) {
        return wellKnownProviderUrl(builder);
    }

    /**
     * 基于给定 UriBuilder 构造 Well-Known 提供方 URL 路径。
     *
     * @param builder 基础 UriBuilder，将追加 Well-Known 路径
     *                It must not be null.
     * @return 追加路径后的 UriBuilder
     */
    public static UriBuilder wellKnownProviderUrl(UriBuilder builder) {
        return builder.path(ServerMetadataResource.class).path("{provider}/realms/{realm}");
    }

    /** 校验别名对应的 Well-Known 提供方是否可通过服务端元数据暴露 */
    private boolean isValidProvider(String alias) {
        return WellKnownProviderUtil.resolveFromAlias(session.getKeycloakSessionFactory(), alias)
                .map(WellKnownProviderFactory::isAvailableViaServerMetadata)
                .orElse(false);
    }
}
