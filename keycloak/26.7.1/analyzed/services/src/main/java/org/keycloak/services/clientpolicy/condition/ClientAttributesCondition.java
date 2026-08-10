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

import java.util.List;
import java.util.Map;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.MapperTypeSerializer;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.context.ClientModelContext;
import org.keycloak.services.clientpolicy.context.PreAuthorizationRequestContext;

import org.jboss.logging.Logger;

import static org.keycloak.services.clientpolicy.ClientPolicyEvent.PRE_AUTHORIZATION_REQUEST;

/**
 * 客户端策略条件：要求客户端携带配置的全部自定义属性键值对才满足条件。
 * <p>在预授权请求或 {@link ClientModelContext} 事件中评估，便于管理员通过客户端属性标记需施加策略的客户端。</p>
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public class ClientAttributesCondition extends AbstractClientPolicyConditionProvider<ClientAttributesCondition.Configuration> {

    private static final Logger logger = Logger.getLogger(ClientAttributesCondition.class);

    /** @param session Keycloak 会话 */
    public ClientAttributesCondition(KeycloakSession session) {
        super(session);
    }

    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** 条件配置：序列化的客户端属性映射（键→单值列表） */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        /** 属性映射 JSON/序列化字符串 */
        private String attributes;

        /** @return 属性配置字符串 */
        public String getAttributes() {
            return attributes;
        }

        /** @param attributes 属性配置字符串 */
        public void setAttributes(String attributes) {
            this.attributes = attributes;
        }
    }

    @Override
    public String getProviderId() {
        return ClientAttributesConditionFactory.PROVIDER_ID;
    }

    /** 在预授权或客户端模型上下文中比对客户端属性 @param context 策略上下文 @return 投票结果 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        if (context.getEvent() == PRE_AUTHORIZATION_REQUEST) {
            PreAuthorizationRequestContext parc = (PreAuthorizationRequestContext) context;
            ClientModel client = session.getContext().getRealm().getClientByClientId(parc.getClientId());
            if (isAttributesMatched(client)) return ClientPolicyVote.YES;
            return ClientPolicyVote.NO;
        } else if (context instanceof ClientModelContext clientModelContext) {
            ClientModel client = clientModelContext.getClient();
            if (isAttributesMatched(client)) return ClientPolicyVote.YES;
            return ClientPolicyVote.NO;
        } else {
            return ClientPolicyVote.ABSTAIN;
        }
    }

    /** 判断客户端是否包含配置要求的全部属性键值 */
    private boolean isAttributesMatched(ClientModel client) {
        if (client == null) return false;

        Map<String, List<String>> attributesForMatching = getAttributesForMatching();
        if (attributesForMatching == null) return false;

        Map<String, String> clientAttributes = client.getAttributes();

        if (logger.isTraceEnabled()) {
            clientAttributes.forEach((i, j) -> logger.tracev("client attribute assigned = {0}: {1}", i, j));
            attributesForMatching.forEach((i, j) -> logger.tracev("client attribute for matching = {0}: {1}", i, j));
        }

        return attributesForMatching.entrySet().stream()
                .allMatch(entry -> {
                    String key = entry.getKey();
                    if (key == null) {
                        logger.warnf("Empty key in configuration of client-attributes condition");
                        return false;
                    }
                    if (entry.getValue() == null || entry.getValue().isEmpty()) {
                        logger.warnf("Empty value in the configuration of client-attributes condition for the attribute %s. This cannot match any client", key);
                        return false;
                    }
                    if (entry.getValue().size() > 1) {
                        logger.warnf("More values in the configuration of client-attributes condition for the attribute %s. This cannot match any client", key);
                        return false;
                    }
                    String value = entry.getValue().get(0);
                    return clientAttributes.containsKey(key) && clientAttributes.get(key).equals(value);
                });
    }

    /** 反序列化配置中的属性映射 */
    private Map<String, List<String>> getAttributesForMatching() {
        if (configuration.getAttributes() == null) return null;
        return MapperTypeSerializer.deserialize(configuration.getAttributes());
    }

}
