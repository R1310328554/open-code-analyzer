/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.TokenRefreshResponseContext;

import org.jboss.logging.Logger;

/**
 * 抑制刷新令牌轮换执行器。
 * <p>在令牌刷新响应阶段移除轮换后的新 refresh token，使客户端仅保留原 refresh token。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SuppressRefreshTokenRotationExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

    private static final Logger logger = Logger.getLogger(SuppressRefreshTokenRotationExecutor.class);

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public SuppressRefreshTokenRotationExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** @return 执行器 Provider 标识符 */
    @Override
    public String getProviderId() {
        return SuppressRefreshTokenRotationExecutorFactory.PROVIDER_ID;
    }

    /** 按客户端策略事件触发校验逻辑 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        ClientPolicyEvent event = context.getEvent();
        logger.tracev("Client Policy Trigger Event = {0}",  event);
        switch (event) {
            case TOKEN_REFRESH_RESPONSE:
                TokenRefreshResponseContext tokenRefreshResponseContext = (TokenRefreshResponseContext)context;
                TokenManager.AccessTokenResponseBuilder builder = tokenRefreshResponseContext.getAccessTokenResponseBuilder();
                builder.removeRefreshToken(); // 构建刷新响应前丢弃轮换后的 refresh token
                logger.trace("A rorated refresh token was suppressed.");
                break;
            default :
                return;
        }
    }

}
