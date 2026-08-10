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

import org.keycloak.OAuthErrorException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * 机密客户端准入执行器。
 * <p>在授权端点、令牌端点等 OAuth 流程中拒绝 public 或 bearer-only 客户端的请求。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ConfidentialClientAcceptExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

    /** Keycloak 会话，用于读取当前客户端上下文 */
    protected final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public ConfidentialClientAcceptExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** @return 执行器标识 {@link ConfidentialClientAcceptExecutorFactory#PROVIDER_ID} */
    @Override
    public String getProviderId() {
        return ConfidentialClientAcceptExecutorFactory.PROVIDER_ID;
    }

    /** 在授权/令牌等事件中校验客户端必须为机密类型 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        // 仅在 OAuth 核心端点事件上执行校验
        switch (context.getEvent()) {
            case AUTHORIZATION_REQUEST:
            case TOKEN_REQUEST:
            case SERVICE_ACCOUNT_TOKEN_REQUEST:
            case BACKCHANNEL_AUTHENTICATION_REQUEST:
            case BACKCHANNEL_TOKEN_REQUEST:
                checkIsConfidentialClient();
                return;
            default:
                return;
        }
    }

    /** 校验上下文客户端存在且为 confidential（非 public、非 bearer-only） */
    private void checkIsConfidentialClient() throws ClientPolicyException {
        ClientModel client = session.getContext().getClient();
        if (client == null) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_CLIENT, "no client in context");
        }
        if (client.isPublicClient()) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_CLIENT, "invalid client access type: public");
        }
        if (client.isBearerOnly()) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_CLIENT, "invalid client access type: bearer only");
        }
    }
}
