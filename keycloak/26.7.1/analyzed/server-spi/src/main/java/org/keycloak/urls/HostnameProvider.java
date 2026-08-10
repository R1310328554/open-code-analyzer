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
package org.keycloak.urls;

import java.net.URI;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.models.KeycloakContext;
import org.keycloak.provider.Provider;

/**
 * 主机名提供者：决定 Keycloak 前端与后端请求的 URL（scheme、host、port、context-path）。
 * 可基于请求头（如 Host）或固定配置；前端与后端可使用不同 URL。
 * <p>
 * 注意：勿在实现中调用 {@link KeycloakContext#getUri()}，否则会无限递归。
 * <p>
 * 注意：传入的 {@link UriInfo} 在无活动上下文时会抛出 {@link ContextNotActiveException}。
 * <p>
 * The Hostname provider is used by Keycloak to decide URLs for frontend and backend requests. A provider can either
 * base the URL on the request (Host header for example) or based on hard-coded URLs. Further, it is possible to have
 * different URLs on frontend requests and backend requests.
 * <p>
 * Note: Do NOT use {@link KeycloakContext#getUri()} within a Hostname provider. It will result in an infinite loop.
 * <p>
 * Note: the {@link UriInfo} provided to these methods will throw {@link ContextNotActiveException} rather than {@link IllegalStateException} as described in the javadoc.
 */
public interface HostnameProvider extends Provider {

    /**
     * 返回 URL scheme；未实现时委托 {@link #getScheme(UriInfo)}。
     * Returns the URL scheme. If not implemented will delegate to {@link #getScheme(UriInfo)}.
     *
     * @param originalUriInfo the original URI
     * @param type type of the request
     * @return the schema
     */
    default String getScheme(UriInfo originalUriInfo, UrlType type) {
        return getScheme(originalUriInfo);
    }

    /**
     * 返回 URL scheme；未实现时从请求读取。
     * Returns the URL scheme. If not implemented will get the scheme from the request.
     *
     * @param originalUriInfo the original URI
     * @return the schema
     */
    default String getScheme(UriInfo originalUriInfo) {
        return originalUriInfo.getBaseUri().getScheme();
    }

    /**
     * 返回主机名；未实现时委托 {@link #getHostname(UriInfo)}。
     * Returns the host. If not implemented will delegate to {@link #getHostname(UriInfo)}.
     *
     * @param originalUriInfo the original URI
     * @param type type of the request
     * @return the host
     */
    default String getHostname(UriInfo originalUriInfo, UrlType type) {
        return getHostname(originalUriInfo);
    }

    /**
     * 返回主机名；未实现时从请求读取。
     *  Returns the host. If not implemented will get the host from the request.
     * @param originalUriInfo
     * @return the host
     */
    default String getHostname(UriInfo originalUriInfo) {
        return originalUriInfo.getBaseUri().getHost();
    }

    /**
     * 返回端口（默认端口为 -1）；未实现时委托 {@link #getPort(UriInfo)}。
     * Returns the port (or -1 for default port). If not implemented will delegate to {@link #getPort(UriInfo)}
     *
     * @param originalUriInfo the original URI
     * @param type type of the request
     * @return the port
     */
    default int getPort(UriInfo originalUriInfo, UrlType type) {
        return getPort(originalUriInfo);
    }

    /**
     * 返回端口；未实现时从请求读取。
     * Returns the port (or -1 for default port). If not implemented will get the port from the request.
     *
     * @param originalUriInfo the original URI
     * @return the port
     */
    default int getPort(UriInfo originalUriInfo) {
        return originalUriInfo.getBaseUri().getPort();
    }

    /**
     * 返回 Keycloak 上下文路径（反向代理场景）；未实现时委托 {@link #getContextPath(UriInfo)}。
     * Returns the context-path for Keycloak. This is useful when Keycloak is exposed on a different context-path on
     * a reverse proxy. If not implemented will delegate to {@link #getContextPath(UriInfo)}
     *
     * @param originalUriInfo the original URI
     * @param type type of the request
     * @return the context-path
     */
    default String getContextPath(UriInfo originalUriInfo, UrlType type) {
        return getContextPath(originalUriInfo);
    }

    /**
     * 返回上下文路径；未实现时使用请求中的路径（默认 /auth）。
     * Returns the context-path for Keycloak This is useful when Keycloak is exposed on a different context-path on
     * a reverse proxy. If not implemented will use the context-path from the request, which by default is /auth
     *
     * @param originalUriInfo the original URI
     * @return the context-path
     */
    default String getContextPath(UriInfo originalUriInfo) {
        return originalUriInfo.getBaseUri().getPath();
    }

    @Override
    default void close() {
    }

    /**
     * 按 {@link UrlType} 组装 Keycloak 基础 URI。
     * Returns the base URI for Keycloak with the scheme, host, port, and context-path set for the given UrlType
     *
     * @param originalUriInfo the original URI
     * @param type type of the request
     * @return the base URI
     */
    default URI getBaseUri(UriInfo originalUriInfo, UrlType type) {
        String scheme = getScheme(originalUriInfo, type);
        String hostname = getHostname(originalUriInfo, type);
        int port = getPort(originalUriInfo, type);
        String contextPath = getContextPath(originalUriInfo, type);
        return originalUriInfo.getBaseUriBuilder().scheme(scheme).host(hostname).port(port).replacePath(contextPath).build();
    }

}
