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

package org.keycloak.protocol.oidc.grants.ciba.clientpolicy.executor;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.OAuthErrorException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.grants.ciba.clientpolicy.context.BackchannelAuthenticationRequestContext;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.request.BackchannelAuthenticationEndpointRequest;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;

import org.jboss.logging.Logger;

/**
 * CIBA 客户端策略 Executor：强制后台认证请求包含 {@code binding_message} 参数。
 * <p>用于区分并发 CIBA 流程中用户正在响应哪一次认证。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureCibaSessionEnforceExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(SecureCibaSessionEnforceExecutor.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public SecureCibaSessionEnforceExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** @return Executor Provider ID */
    @Override
    public String getProviderId() {
        return SecureCibaSessionEnforceExecutorFactory.PROVIDER_ID;
    }

    /** 在后台认证请求事件中校验 binding_message */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case BACKCHANNEL_AUTHENTICATION_REQUEST:
                BackchannelAuthenticationRequestContext backchannelAuthenticationRequestContext = (BackchannelAuthenticationRequestContext)context;
                executeOnBackchannelAuthenticationRequest(backchannelAuthenticationRequestContext.getRequest(),
                    backchannelAuthenticationRequestContext.getRequestParameters());
                return;
            default:
                return;
        }
    }

    /** 校验请求必须携带 binding_message 参数 */
    private void executeOnBackchannelAuthenticationRequest(
            BackchannelAuthenticationEndpointRequest request,
            MultivaluedMap<String, String> requestParameters) throws ClientPolicyException {
        logger.trace("Backchannel Authentication Endpoint - authn request");
        if (request.getBindingMessage() == null) {
            logger.trace("Missing parameter: binding_message");
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Missing parameter: binding_message");
        }
        logger.trace("Passed.");
    }
}
