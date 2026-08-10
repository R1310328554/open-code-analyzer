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

package org.keycloak.services.clientpolicy.condition;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;

/**
 * 客户端策略条件：对任意客户端、任意事件恒返回 {@link ClientPolicyVote#YES}。
 * <p>常用于需要无条件应用某 Profile 的策略组合。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class AnyClientCondition extends AbstractClientPolicyConditionProvider<ClientPolicyConditionConfigurationRepresentation> {

    /** @param session Keycloak 会话 */
    public AnyClientCondition(KeycloakSession session) {
        super(session);
    }

    /** @return 默认条件配置类型（无额外字段） */
    @Override
    public Class<ClientPolicyConditionConfigurationRepresentation> getConditionConfigurationClass() {
        return ClientPolicyConditionConfigurationRepresentation.class;
    }

    /** @return 条件提供方 ID */
    @Override
    public String getProviderId() {
        return AnyClientConditionFactory.PROVIDER_ID;
    }

    /** 恒为 YES，表示条件始终满足 @param context 策略上下文 @return YES */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        return ClientPolicyVote.YES;
    }

}
