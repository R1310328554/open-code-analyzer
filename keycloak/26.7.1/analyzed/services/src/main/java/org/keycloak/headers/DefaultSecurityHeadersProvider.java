/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.headers;

import java.util.Collections;
import java.util.Map;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.BrowserSecurityHeaders;
import org.keycloak.models.ContentSecurityPolicyBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import org.jboss.logging.Logger;

import static jakarta.ws.rs.HttpMethod.HEAD;
import static jakarta.ws.rs.HttpMethod.OPTIONS;

import static org.keycloak.models.BrowserSecurityHeaders.CONTENT_SECURITY_POLICY;

/**
 * 默认 {@link SecurityHeadersProvider}：按请求/响应 MediaType 写入领域配置的浏览器安全头。
 * <p>REST（JSON/XML）与 HTML 页面采用不同头集合；HTML 响应支持 CSP frame-ancestors 动态调整。</p>
 */
public class DefaultSecurityHeadersProvider implements SecurityHeadersProvider {

    private static final Logger LOGGER = Logger.getLogger(DefaultSecurityHeadersProvider.class);

    /** 领域级浏览器安全头键值（来自 {@link RealmModel#getBrowserSecurityHeaders()}）。 */
    private final Map<String, String> headerValues;
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** 本次响应的可选头配置；首次调用 {@link #options()} 时懒创建。 */
    private DefaultSecurityHeadersOptions options;

    /** 从当前领域加载浏览器安全头配置。 */
    public DefaultSecurityHeadersProvider(KeycloakSession session) {
        this.session = session;

        RealmModel realm = session.getContext().getRealm();
        if (realm != null) {
            headerValues = realm.getBrowserSecurityHeaders();
        } else {
            headerValues = Collections.emptyMap();
        }
    }

    @Override
    public SecurityHeadersOptions options() {
        if (options == null) {
            options = new DefaultSecurityHeadersOptions();
        }
        return options;
    }

    @Override
    /** 根据 MediaType 与 options 向响应写入安全头。 */
    public void addHeaders(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (options != null && options.isSkipHeaders()) {
            return;
        }

        MediaType requestType = requestContext.getMediaType();
        MediaType responseType = responseContext.getMediaType();
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        if (responseType == null && !isEmptyMediaTypeAllowed(requestContext, responseContext)) {
            LOGGER.errorv("MediaType not set on path {0}, with response status {1}", session.getContext().getUri().getRequestUri().getPath(), responseContext.getStatus());
            throw new InternalServerErrorException();
        }

        if (isRest(requestType, responseType)) {
            addRestHeaders(headers);
        } else if (isHtml(requestType, responseType)) {
            addHtmlHeaders(headers);
        } else {
            addGenericHeaders(headers);
        }
    }

    /** 写入通用安全头（HSTS、X-Content-Type-Options 等，不含 X-Frame-Options/CSP）。 */
    private void addGenericHeaders(MultivaluedMap<String, Object> headers) {
        addHeader(BrowserSecurityHeaders.STRICT_TRANSPORT_SECURITY, headers);
        addHeader(BrowserSecurityHeaders.X_CONTENT_TYPE_OPTIONS, headers);
        addHeader(BrowserSecurityHeaders.REFERRER_POLICY, headers);
        addHeader(BrowserSecurityHeaders.X_ROBOTS_TAG, headers);
    }

    /** 写入 REST API 响应安全头（含 X-Frame-Options，不含 CSP）。 */
    private void addRestHeaders(MultivaluedMap<String, Object> headers) {
        addHeader(BrowserSecurityHeaders.STRICT_TRANSPORT_SECURITY, headers);
        addHeader(BrowserSecurityHeaders.X_FRAME_OPTIONS, headers);
        addHeader(BrowserSecurityHeaders.X_CONTENT_TYPE_OPTIONS, headers);
        addHeader(BrowserSecurityHeaders.REFERRER_POLICY, headers);
        addHeader(BrowserSecurityHeaders.X_ROBOTS_TAG, headers);
    }

    /** 写入 HTML 页面全部安全头，并按 options 调整 CSP/X-Frame-Options。 */
    private void addHtmlHeaders(MultivaluedMap<String, Object> headers) {
        for (BrowserSecurityHeaders header : BrowserSecurityHeaders.values()) {
            addHeader(header, headers);
        }

        // TODO：引入更严格 CSP 后将重构此逻辑
        if (options != null) {
            if (options.isAllowAnyFrameAncestor()) {
                headers.remove(BrowserSecurityHeaders.X_FRAME_OPTIONS.getHeaderName());
            }

            Object cspVal = headers.getFirst(CONTENT_SECURITY_POLICY.getHeaderName());
            if (cspVal != null) {
                ContentSecurityPolicyBuilder csp = ContentSecurityPolicyBuilder.create(cspVal.toString());
                if (options.isAllowAnyFrameAncestor() && csp.isDefaultFrameAncestors()) {
                    // 仅当 frame-ancestors 为默认 'self' 时才移除
                    csp.frameAncestors(null);
                }

                String allowedFrameSrc = options.getAllowedFrameSrc();
                if (allowedFrameSrc != null) {
                    csp.addFrameSrc(allowedFrameSrc);
                }

                headers.putSingle(CONTENT_SECURITY_POLICY.getHeaderName(), csp.build());
            }
        }
    }

    /** 写入单个安全头，优先使用领域配置值。 */
    private void addHeader(BrowserSecurityHeaders header, MultivaluedMap<String, Object> headers) {
        String value = headerValues.getOrDefault(header.getKey(), header.getDefaultValue());
        if (value != null && !value.isEmpty()) {
            headers.putSingle(header.getHeaderName(), value);
        }
    }

    /**
     * 判断无 Content-Type 的响应是否安全可接受。
     */
    private boolean isEmptyMediaTypeAllowed(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (!responseContext.hasEntity()) {
            if (options != null && options.isAllowEmptyContentType()) {
                return true;
            }
            int status = responseContext.getStatus();
            if (status == 201 || status == 204 ||
                status == 301 || status == 302 || status == 303 || status == 304 || status == 307 || status == 308 ||
                status == 400 || status == 401 || status == 403 || status == 404) {
                return true;
            }

            String method = requestContext.getMethod().toUpperCase();

            switch (method) {
                case OPTIONS:
                    return true;
                case HEAD:
                    return status == 200;
            }
        }

        return false;
    }

    /** 判断是否为 REST（JSON/XML）响应。 */
    private boolean isRest(MediaType requestType, MediaType responseType) {
        MediaType mediaType = responseType != null ? responseType : requestType;
        return matches(mediaType, MediaType.APPLICATION_JSON_TYPE) || matches(mediaType, MediaType.APPLICATION_XML_TYPE);
    }

    /** 判断是否为 HTML 或表单 URL 编码响应。 */
    private boolean isHtml(MediaType requestType, MediaType responseType) {
        if (matches(responseType, MediaType.TEXT_HTML_TYPE)) {
            return true;
        } else if (matches(requestType, MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
            return true;
        }
        return false;
    }

    /** 比较两个 MediaType 的 type/subtype（忽略大小写）。 */
    private boolean matches(MediaType a, MediaType b) {
        if (a == null) {
            return b == null;
        }
        return a.getType().equalsIgnoreCase(b.getType()) && a.getSubtype().equalsIgnoreCase(b.getSubtype());
    }

}
