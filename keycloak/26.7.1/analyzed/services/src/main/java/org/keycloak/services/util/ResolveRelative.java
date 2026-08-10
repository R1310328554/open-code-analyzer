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

package org.keycloak.services.util;

import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.common.util.StringPropertyReplacer;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.urls.UrlType;

/**
 * 相对 URI 解析工具类。
 * <p>将以 {@code /} 开头的路径解析为基于 frontend/admin 根 URL 的绝对 URI，
 * 并支持 {@link Constants#AUTH_BASE_URL_PROP} 等占位符替换。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResolveRelative {

    /**
     * 从当前 session 上下文获取 frontend/admin URL 后解析相对 URI。
     *
     * @param rootUrl 根 URL 或占位符（如 {@link Constants#AUTH_BASE_URL_PROP}）
     * @param url 待解析的路径
     */
    public static String resolveRelativeUri(KeycloakSession session, String rootUrl, String url) {
        String frontendUrl = session.getContext().getUri(UrlType.FRONTEND).getBaseUri().toString();
        String adminUrl = session.getContext().getUri(UrlType.ADMIN).getBaseUri().toString();
        return resolveRelativeUri(frontendUrl, adminUrl, rootUrl, url);
    }

    /**
     * 将相对路径解析为绝对 URI。
     * <p>非 {@code /} 开头则原样返回；有 rootUrl 则拼接；否则基于 frontendUrl 替换路径。</p>
     */
    public static String resolveRelativeUri(String frontendUrl, String adminUrl, String rootUrl, String url) {
        String finalUrl;

        if (url == null || !url.startsWith("/")) {
            finalUrl = url;
        } else if (rootUrl != null && !rootUrl.isEmpty()) {
            finalUrl = resolveRootUrl(frontendUrl, adminUrl, rootUrl) + url;
        } else {
            finalUrl = UriBuilder.fromUri(frontendUrl).replacePath(url).build().toString();
        }

        return StringPropertyReplacer.replaceProperties(finalUrl);
    }

    /** 从 session 上下文解析 rootUrl 占位符为实际 URL。 */
    public static String resolveRootUrl(KeycloakSession session, String rootUrl) {
        String frontendUrl = session.getContext().getUri(UrlType.FRONTEND).getBaseUri().toString();
        String adminUrl = session.getContext().getUri(UrlType.ADMIN).getBaseUri().toString();
        return resolveRootUrl(frontendUrl, adminUrl, rootUrl);
    }

    /**
     * 将 rootUrl 占位符替换为 frontend 或 admin 基础 URL。
     * <p>{@link Constants#AUTH_BASE_URL_PROP} → frontendUrl；
     * {@link Constants#AUTH_ADMIN_URL_PROP} → adminUrl。</p>
     */
    public static String resolveRootUrl(String frontendUrl, String adminUrl, String rootUrl) {
        if (rootUrl != null) {
            if (rootUrl.equals(Constants.AUTH_BASE_URL_PROP)) {
                rootUrl = frontendUrl;
                if (rootUrl.endsWith("/")) {
                    rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
                }
            } else if (rootUrl.equals(Constants.AUTH_ADMIN_URL_PROP)) {
                rootUrl = adminUrl;
                if (rootUrl.endsWith("/")) {
                    rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
                }
            }
        }
        return rootUrl;
    }
}
