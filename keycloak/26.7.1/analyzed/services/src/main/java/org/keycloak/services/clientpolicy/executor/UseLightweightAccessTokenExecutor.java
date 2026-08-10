/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * 轻量级访问令牌执行器。
 * <p>在各类令牌请求事件中，于会话上设置 {@link Constants#USE_LIGHTWEIGHT_ACCESS_TOKEN_ENABLED}，使后续令牌签发使用轻量级访问令牌格式。</p>
 */
public class UseLightweightAccessTokenExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {
    /** Keycloak 会话，用于设置轻量级令牌属性 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public UseLightweightAccessTokenExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** {@inheritDoc} 返回执行器 Provider ID */
    @Override
    public String getProviderId() {
        return UseLightweightAccessTokenExecutorFactory.PROVIDER_ID;
    }

    /** 在令牌请求/刷新等事件中启用轻量级访问令牌 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case TOKEN_REQUEST:
            case TOKEN_REFRESH:
            case RESOURCE_OWNER_PASSWORD_CREDENTIALS_REQUEST:
            case SERVICE_ACCOUNT_TOKEN_REQUEST:
            case BACKCHANNEL_TOKEN_REQUEST:
            case DEVICE_TOKEN_REQUEST:
                session.setAttribute(Constants.USE_LIGHTWEIGHT_ACCESS_TOKEN_ENABLED, true);
                break;
        }
    }
}
