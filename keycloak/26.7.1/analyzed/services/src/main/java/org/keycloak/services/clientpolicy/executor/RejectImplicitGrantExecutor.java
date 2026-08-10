/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.executor;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.OAuthErrorException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.par.clientpolicy.context.PushedAuthorizationRequestContext;
import org.keycloak.protocol.oidc.utils.OIDCResponseType;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AuthorizationRequestContext;
import org.keycloak.services.clientpolicy.context.ClientCRUDContext;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.keycloak.OAuth2Constants.CODE;

/**
 * 拒绝隐式授权（Implicit/Hybrid）执行器。
 * <p>在客户端注册/更新、PAR 与授权请求中禁止 implicit/hybrid 流程，无论客户端是否显式启用 implicit flow。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class RejectImplicitGrantExecutor implements ClientPolicyExecutorProvider<RejectImplicitGrantExecutor.Configuration> {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 执行器运行时配置 */
    private Configuration configuration;

    public RejectImplicitGrantExecutor(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void setupConfiguration(Configuration config) {
        this.configuration = config;
    }

    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** 隐式授权拒绝执行器配置项 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {
        /** 为 true 时在注册/更新时自动关闭 implicit flow */
        @JsonProperty("auto-configure")
        protected Boolean autoConfigure;

        public Boolean isAutoConfigure() {
            return autoConfigure;
        }

        public void setAutoConfigure(Boolean autoConfigure) {
            this.autoConfigure = autoConfigure;
        }
    }

    @Override
    public String getProviderId() {
        return RejectImplicitGrantExecutorFactory.PROVIDER_ID;
    }

    /** 按事件类型自动配置、校验或拒绝 implicit/hybrid 请求 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case REGISTER:
            case UPDATE:
                ClientCRUDContext clientUpdateContext = (ClientCRUDContext)context;
                autoConfigure(clientUpdateContext.getProposedClientRepresentation());
                validate(clientUpdateContext.getProposedClientRepresentation());
                break;
            case PUSHED_AUTHORIZATION_REQUEST:
                PushedAuthorizationRequestContext pushedAuthorizationRequestContext = (PushedAuthorizationRequestContext)context;
                executeOnPushedAuthorizationRequest(pushedAuthorizationRequestContext.getRequest(),
                        pushedAuthorizationRequestContext.getRequestParameters());
                break;
            case AUTHORIZATION_REQUEST:
                AuthorizationRequestContext authorizationRequestContext = (AuthorizationRequestContext)context;
                executeOnAuthorizationRequest(authorizationRequestContext.getParsedResponseType(),
                    authorizationRequestContext.getAuthorizationEndpointRequest(),
                    authorizationRequestContext.getRedirectUri());
                return;
            default:
                return;
        }
    }

    /** 自动将 implicitFlowEnabled 设为 false */
    private void autoConfigure(ClientRepresentation rep) {
        if (configuration.isAutoConfigure())
            rep.setImplicitFlowEnabled(Boolean.FALSE);
    }

    /** 校验客户端元数据不得启用 implicit flow */
    private void validate(ClientRepresentation rep) throws ClientPolicyException {
        boolean isImplicitFlowEnabled = rep.isImplicitFlowEnabled().booleanValue();
        if (!isImplicitFlowEnabled) return;
        throw new ClientPolicyException(OAuthErrorException.INVALID_CLIENT_METADATA, "Invalid client metadata: implicit flow enabled");
    }

    /** 在授权请求中拒绝 implicit/hybrid response_type */
    private void executeOnAuthorizationRequest(
            OIDCResponseType parsedResponseType,
            AuthorizationEndpointRequest request,
            String redirectUri) throws ClientPolicyException {
        // 授权端点已检查客户端 implicit/hybrid 开关；本方法仍一律拒绝 implicit/hybrid
        // 无论客户端配置如何，均禁止 implicit grant
        if (parsedResponseType.isImplicitOrHybridFlow()) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Implicit/Hybrid flow is prohibited.");
        }
    }

    /** 在 PAR 请求中仅允许 authorization code（response_type=code） */
    private void executeOnPushedAuthorizationRequest(
            AuthorizationEndpointRequest request,
            MultivaluedMap<String, String> requestParameters) throws ClientPolicyException {
        if (request.getResponseType() == null || request.getResponseType().isEmpty()) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "No response type.");
        }
        if (!CODE.equals(request.getResponseType())) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Implicit/Hybrid flow is prohibited.");
        }
    }

}
