/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.transaction;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.OAuth2Constants;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;

/**
 * 请求上下文辅助类：提供当前 HTTP 请求的摘要信息，便于日志与诊断。
 * Provides some info about current HTTP request. Useful for example for logging
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RequestContextHelper {

    /** 在 {@link KeycloakSession} 中缓存本辅助实例的属性键。 */
    public static final String SESSION_ATTRIBUTE = "REQ_CONTEXT_HELPER";

    private static final Set<String> ALLOWED_ATTRIBUTES = Set.of(OAuth2Constants.GRANT_TYPE, OAuth2Constants.CIBA_GRANT_TYPE, OAuth2Constants.SCOPE, OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE, OAuth2Constants.ACCESS_TOKEN_TYPE,
                                                                 OAuth2Constants.DEVICE_CODE_GRANT_TYPE, OAuth2Constants.RESPONSE_TYPE);

    private final KeycloakSession session;

    // 显式设置的上下文描述（非 HTTP 场景，如定时清理任务）
    // Explicitly set information about context. This is useful when the request is executed outside of HTTP (for example during periodic cleaner tasks)
    private String contextMessage;

    /** 私有构造，通过 {@link #getContext} 获取实例。 */
    private RequestContextHelper(KeycloakSession session) {
        this.session = session;
    }

    /** 设置非 HTTP 场景的上下文描述信息。 */
    public void setContextMessage(String message) {
        this.contextMessage = message;
    }

    /** 从会话获取或创建请求上下文辅助实例。 */
    public static RequestContextHelper getContext(KeycloakSession session) {
        RequestContextHelper ctxHelper = (RequestContextHelper) session.getAttribute(SESSION_ATTRIBUTE);
        if (ctxHelper != null) {
            return ctxHelper;
        } else {
            ctxHelper = new RequestContextHelper(session);
            session.setAttribute(SESSION_ATTRIBUTE, ctxHelper);
            return ctxHelper;
        }
    }

    /**
     * 返回当前请求的简短描述，例如 {@code HTTP GET /realms/test/account}。
     * Providing short information about current request. For example just something "HTTP GET /realms/test/account"
     *
     * @return
     */
    public String getContextInfo() {
        if (contextMessage != null) return contextMessage;

        try {
            HttpRequest httpRequest = session.getContext().getHttpRequest();
            if (httpRequest != null && httpRequest.getUri() != null) {

                return new StringBuilder("HTTP ")
                        .append(httpRequest.getHttpMethod())
                        .append(" ")
                        .append(httpRequest.getUri().getPath())
                        .toString();
            }
        } catch (ContextNotActiveException e) {
            // 非 HTTP 上下文
            // non-http context
        } catch (Exception e) {
            return "Unknown context";
        }
        return "Non-HTTP context";
    }

    /**
     * 返回当前请求的详细描述，含表单参数（敏感参数已脱敏）。
     * Providing longer information about current request. For example something like "HTTP GET /realms/test/protocol/openid-connect/token, form parameters [ grant_type=code, redirect_uri=https://... ]"
     *
     * @return
     */
    public String getDetailedContextInfo() {
        try {
            HttpRequest httpRequest = session.getContext().getHttpRequest();
            if (httpRequest != null && httpRequest.getUri() != null) {
                StringBuilder builder = new StringBuilder("HTTP ")
                        .append(httpRequest.getHttpMethod())
                        .append(" ")
                        .append(httpRequest.getUri().getRequestUri());

                MultivaluedMap<String, String> formParams = httpRequest.getDecodedFormParameters();
                if (formParams != null && !formParams.isEmpty()) {

                    builder.append(", Form parameters [ ");
                    formParams.entrySet().forEach(entry -> {
                        String key = entry.getKey();
                        List<String> values = entry.getValue();
                        values.forEach(value -> {

                            if (!ALLOWED_ATTRIBUTES.contains(key)) value = "***";
                            builder.append(key + "=" + value + ", ");
                        });
                    });
                    builder.append(" ]");
                }

                return builder.toString();
            }
        } catch (Exception e) {
            // 异常时回退到简短上下文信息
            // Fallback to getContextInfo if this happens
        }
        return getContextInfo();
    }
}
