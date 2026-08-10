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

import org.keycloak.OAuthErrorException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * 拒绝全部请求执行器。
 * <p>在任意客户端策略事件触发时直接抛出 {@link ClientPolicyException}，用于测试或强制阻断客户端访问。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class RejectRequestExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

    /** @param session Keycloak 会话（本执行器不使用） */
    public RejectRequestExecutor(KeycloakSession session) {
    }

    @Override
    public String getProviderId() {
        return RejectRequestExecutorFactory.PROVIDER_ID;
    }

    /** 无条件拒绝当前客户端策略事件对应的请求 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Request not allowed");
    }
}
