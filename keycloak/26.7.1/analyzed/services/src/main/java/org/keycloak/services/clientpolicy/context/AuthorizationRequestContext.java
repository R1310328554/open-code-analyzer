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

package org.keycloak.services.clientpolicy.context;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.ClientModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.utils.OIDCResponseType;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * OIDC 授权请求上下文：在 {@link ClientPolicyEvent#AUTHORIZATION_REQUEST} 事件上暴露授权端点请求详情。
 * <p>包含解析后的 response_type、redirect_uri、请求参数、认证会话及 scope，供条件/Executor 在授权阶段评估策略。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class AuthorizationRequestContext implements ClientPolicyContext, ClientModelContext, ScopeParameterContext {

    /** 解析后的 OIDC response_type。 */
    private final OIDCResponseType parsedResponseType;
    /** 授权端点请求对象。 */
    private final AuthorizationEndpointRequest request;
    /** 请求中的 redirect_uri。 */
    private final String redirectUri;
    /** 原始请求参数映射。 */
    private final MultivaluedMap<String, String> requestParameters;

    /** 关联的认证会话。 */
    private final AuthenticationSessionModel authenticationSession;

    /**
     * @param parsedResponseType 解析后的 response_type
     * @param request 授权端点请求
     * @param redirectUri 重定向 URI
     * @param requestParameters 请求参数
     * @param authenticationSession 认证会话
     */
    public AuthorizationRequestContext(OIDCResponseType parsedResponseType,
        AuthorizationEndpointRequest request,
        String redirectUri,
        MultivaluedMap<String, String> requestParameters,
        AuthenticationSessionModel authenticationSession) {
        this.parsedResponseType = parsedResponseType;
        this.request = request;
        this.redirectUri = redirectUri;
        this.requestParameters = requestParameters;
        this.authenticationSession = authenticationSession;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#AUTHORIZATION_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.AUTHORIZATION_REQUEST;
    }

    /** @return 解析后的 response_type */
    public OIDCResponseType getParsedResponseType() {
        return parsedResponseType;
    }

    /** @return 授权端点请求对象 */
    public AuthorizationEndpointRequest getAuthorizationEndpointRequest() {
        return request;
    }

    /** @return 重定向 URI */
    public String getRedirectUri() {
        return redirectUri;
    }
 
    /** @return 原始请求参数 */
    public MultivaluedMap<String, String> getRequestParameters() {
        return requestParameters;
    }

    /** @return 是否为 Pushed Authorization Request（含 request_uri 参数） */
    public boolean isParRequest() {
        return requestParameters.containsKey(OIDCLoginProtocol.REQUEST_URI_PARAM);
    }

    /** @return 关联认证会话 */
    public AuthenticationSessionModel getAuthenticationSession() {
        return authenticationSession;
    }

    /** {@inheritDoc} @return 认证会话中的客户端 */
    @Override
    public ClientModel getClient() {
        return authenticationSession.getClient();
    }

    /** {@inheritDoc} @return 请求中的 scope 参数 */
    @Override
    public String getScopeParameter() {
        return request.getScope();
    }
}
