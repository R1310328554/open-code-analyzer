/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.url;

import java.net.URI;
import java.util.Optional;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.enums.SslRequired;
import org.keycloak.models.KeycloakSession;
import org.keycloak.urls.HostnameProvider;
import org.keycloak.urls.UrlType;

import org.jboss.logging.Logger;

import static org.keycloak.common.util.UriUtils.checkUrl;
import static org.keycloak.urls.UrlType.FRONTEND;
import static org.keycloak.utils.StringUtil.isNotBlank;

/**
 * Hostname V2 {@link HostnameProvider} 实现，支持静态主机名、完整 URL、管理端 URL 与动态 backchannel。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class HostnameV2Provider implements HostnameProvider {
    private final KeycloakSession session;
    /** 纯主机名字符串（非完整 URL 时使用）。 */
    private final String hostname;
    /** 完整前端 URL（含 scheme）。 */
    private final URI hostnameUrl;
    /** 管理控制台专用 URL。 */
    private final URI adminUrl;
    /** 是否对 backchannel 请求动态使用原始请求 URI。 */
    private final Boolean backchannelDynamic;
    private static final UrlType defaultUrlType = FRONTEND;

    private final Logger logger = Logger.getLogger(HostnameV2Provider.class);

    public HostnameV2Provider(KeycloakSession session, String hostname, URI hostnameUrl, URI adminUrl, Boolean backchannelDynamic) {
        this.session = session;
        this.hostname = hostname;
        this.hostnameUrl = hostnameUrl;
        this.adminUrl = adminUrl;
        this.backchannelDynamic = backchannelDynamic;
    }

    /** 按 {@link UrlType} 构建基础 URI（前端、管理端、后端或本地管理）。 */
    @Override
    public URI getBaseUri(UriInfo originalUriInfo, UrlType type) {
        UriBuilder builder;

        switch (type) {
            case ADMIN:
                builder = getAdminUriBuilder(originalUriInfo);
                break;
            case LOCAL_ADMIN:
                builder = originalUriInfo.getBaseUriBuilder();
                // 反向代理场景下本地端口可能不准确，此处无法感知实际服务端口
                builder.host("localhost");
                break;
            case BACKEND:
                builder = backchannelDynamic && !isFrontendRequest(originalUriInfo) ? originalUriInfo.getBaseUriBuilder() : getFrontUriBuilder(originalUriInfo);
                break;
            case FRONTEND:
                builder = getFrontUriBuilder(originalUriInfo);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL type");
        }

        URI uri = builder.build();
        // 规范化默认端口（80/443 映射为 -1）
        int normalizedPort = normalizedPort(uri);
        if (normalizedPort != uri.getPort()) {
            builder.port(normalizedPort);
            uri = builder.build();
        }

        return uri;
    }

    /** 将 HTTP 80 / HTTPS 443 映射为标准“无端口”表示。 */
    private int normalizedPort(URI uri) {
        if ((uri.getScheme().equals("http") && uri.getPort() == 80) || (uri.getScheme().equals("https") && uri.getPort() == 443)) {
            return -1;
        }
        return uri.getPort();
    }

    /** 判断当前请求是否来自前端（scheme/host/port 与配置的前端 URL 一致）。 */
    private boolean isFrontendRequest(UriInfo originalUriInfo) {
        URI frontend = getFrontUriBuilder(originalUriInfo).build();
        return frontend.getScheme().equals(originalUriInfo.getBaseUri().getScheme()) &&
                frontend.getHost().equals(originalUriInfo.getBaseUri().getHost()) &&
                frontend.getPort() == normalizedPort(originalUriInfo.getBaseUri());
    }

    /** 构建前端 URI，优先使用 realm {@code frontendUrl} 属性，其次全局 hostname/hostnameUrl。 */
    private UriBuilder getFrontUriBuilder(UriInfo originalUriInfo) {
        UriBuilder builder = getRealmFrontUriBuilder();

        if (builder != null) {
            return builder;
        }

        if (hostnameUrl != null) {
            builder = UriBuilder.fromUri(hostnameUrl);
        }
        else {
            builder = originalUriInfo.getBaseUriBuilder();
            if (hostname != null) {
                builder.host(hostname);
            }
        }
        return builder;
    }

    private UriBuilder getRealmFrontUriBuilder() {
        return Optional.ofNullable(session)
                .map(s -> s.getContext())
                .map(c -> c.getRealm())
                .map(r -> r.getAttribute("frontendUrl"))
                .filter(url -> isNotBlank(url))
                .filter(url -> {
                    try {
                        // 与其他 Hostname 提供者保持一致，避免破坏性变更；此 URL 校验方式被认为不够充分
                        checkUrl(SslRequired.NONE, url, "Realm frontendUrl");
                    }
                    catch (IllegalArgumentException e) {
                        logger.errorf(e, "Failed to parse realm frontendUrl '%s'. Falling back to global value.", url);
                        return false;
                    }
                    return true;
                })
                .map(UriBuilder::fromUri)
                .orElse(null);
    }

    /** 构建管理端 URI，未配置 {@code hostname-admin} 时回退到前端 URL。 */
    private UriBuilder getAdminUriBuilder(UriInfo originalUriInfo) {
        return adminUrl != null ? UriBuilder.fromUri(adminUrl) : getFrontUriBuilder(originalUriInfo);
    }

    @Override
    public String getScheme(UriInfo originalUriInfo, UrlType type) {
        return getBaseUri(originalUriInfo, type).getScheme();
    }

    @Override
    public String getScheme(UriInfo originalUriInfo) {
        return getScheme(originalUriInfo, defaultUrlType);
    }

    @Override
    public String getHostname(UriInfo originalUriInfo, UrlType type) {
        return getBaseUri(originalUriInfo, type).getHost();
    }

    @Override
    public String getHostname(UriInfo originalUriInfo) {
        return getHostname(originalUriInfo, defaultUrlType);
    }

    @Override
    public int getPort(UriInfo originalUriInfo, UrlType type) {
        return getBaseUri(originalUriInfo, type).getPort();
    }

    @Override
    public int getPort(UriInfo originalUriInfo) {
        return getPort(originalUriInfo, defaultUrlType);
    }

    @Override
    public String getContextPath(UriInfo originalUriInfo, UrlType type) {
        return getBaseUri(originalUriInfo, type).getPath();
    }

    @Override
    public String getContextPath(UriInfo originalUriInfo) {
        return getContextPath(originalUriInfo, defaultUrlType);
    }

}
