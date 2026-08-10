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

package org.keycloak.protocol.oidc.endpoints.request;

import java.util.Set;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;

/**
 * 从授权端点查询串解析 OAuth/OIDC 参数。
 * <p>检测重复参数，并在需要时强制要求 {@code response_type} 出现在查询串中（OIDC Core）。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzEndpointQueryStringParser extends AuthzEndpointRequestParser {

    private static final Logger logger = Logger.getLogger(AuthzEndpointRequestParser.class);

    /** 原始查询/表单参数映射。 */
    private final MultivaluedMap<String, String> requestParams;

    /** 是否强制查询串包含 response_type。 */
    private final boolean isResponseTypeParameterRequired;

    /** 解析过程中发现的无效请求消息。 */
    private String invalidRequestMessage = null;

    /**
     * @param keycloakSession Keycloak 会话
     * @param requestParams 请求参数
     * @param isResponseTypeParameterRequired 是否要求 response_type
     */
    public AuthzEndpointQueryStringParser(KeycloakSession keycloakSession, MultivaluedMap<String, String> requestParams, boolean isResponseTypeParameterRequired) {
        super(keycloakSession);
        this.requestParams = requestParams;
        this.isResponseTypeParameterRequired = isResponseTypeParameterRequired;
    }

    @Override
    protected void validateResponseTypeParameter(String responseTypeParameter, AuthorizationEndpointRequest request) {
        // OIDC Core：即使 request 对象含 response_type，查询串仍须携带该参数
        if (isResponseTypeParameterRequired && responseTypeParameter == null) {
            logger.warn("Missing parameter 'response_type' in the OAuth 2.0 request parameters");
            invalidRequestMessage = "Missing parameter: response_type";
        }

        super.validateResponseTypeParameter(responseTypeParameter, request);
    }

    @Override
    protected String getParameter(String paramName) {
        checkDuplicated(requestParams, paramName);
        return requestParams.getFirst(paramName);
    }

    @Override
    protected Integer getIntParameter(String paramName) {
        checkDuplicated(requestParams, paramName);
        String paramVal = requestParams.getFirst(paramName);
        return paramVal==null ? null : Integer.valueOf(paramVal);
    }

    /** @return 无效请求错误描述，无错误时 null */
    public String getInvalidRequestMessage() {
        return invalidRequestMessage;
    }

    @Override
    protected Set<String> keySet() {
        return requestParams.keySet();
    }

    private void checkDuplicated(MultivaluedMap<String, String> requestParams, String paramName) {
        if (invalidRequestMessage == null) {
            if (requestParams.get(paramName) != null && requestParams.get(paramName).size() != 1) {
                invalidRequestMessage = "duplicated parameter";
            }
        }
    }

}
