/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.context.ClientCRUDContext;
import org.keycloak.services.clientpolicy.context.ClientModelContext;

import static org.keycloak.services.clientpolicy.ClientPolicyEvent.REGISTER;

/**
 * 客户端策略条件：按客户端登录协议（如 OpenID Connect、SAML）决定是否应用策略。
 * <p>在 {@link ClientPolicyEvent#REGISTER} 或 {@link ClientModelContext} 事件中评估协议字段。</p>
 *
 * @author rmartinc
 */
public class ClientProtocolCondition extends AbstractClientPolicyConditionProvider<ClientProtocolCondition.Configuration> {

    /** 条件配置：期望的客户端协议 ID */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        /** 配置的协议标识（与 {@link LoginProtocol} 提供方 ID 对应） */
        protected String protocol;

        /** 默认构造，protocol 为 null */
        public Configuration() {
            protocol = null;
        }

        /** @param protocol 期望协议 ID */
        public Configuration(String protocol) {
            this.protocol = protocol;
        }

        /** @return 配置的协议 ID */
        public String getProtocol() {
            return protocol;
        }

        /** @param protocol 协议 ID */
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
    }

    /** @param session Keycloak 会话 */
    public ClientProtocolCondition(KeycloakSession session) {
        super(session);
    }

    @Override
    public String getProviderId() {
        return ClientProtocolConditionFactory.PROVIDER_ID;
    }

    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** 比较客户端或待注册客户端的 protocol 与配置 @param context 策略上下文 @return 投票结果 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        if (context.getEvent() == REGISTER) {
            if (isCorrectProtocolFromRepresentation((ClientCRUDContext)context)) {
                return ClientPolicyVote.YES;
            }
            return ClientPolicyVote.NO;
        } else if (context instanceof ClientModelContext) {
            ClientModel client = ((ClientModelContext) context).getClient();
            if (isCorrectClientProtocol(client)) {
                return ClientPolicyVote.YES;
            } else {
                return ClientPolicyVote.NO;
            }
        } else {
            return ClientPolicyVote.ABSTAIN;
        }
    }

    /** 判断已持久化客户端的协议是否匹配配置 */
    private boolean isCorrectClientProtocol(ClientModel client) {
        if (client != null) {
            String protocol = client.getProtocol();
            if (protocol != null) {
                return protocol.equals(configuration.getProtocol());
            }
        }
        return false;
    }

    /** 判断注册提议表示中的协议是否匹配配置 */
    public boolean isCorrectProtocolFromRepresentation(ClientCRUDContext context) {
        ClientRepresentation clientRep = context.getProposedClientRepresentation();
        if (clientRep != null) {
            String protocol = clientRep.getProtocol();
            if (protocol != null) {
                return protocol.equals(configuration.getProtocol());
            }
        }
        return false;
    }
}
