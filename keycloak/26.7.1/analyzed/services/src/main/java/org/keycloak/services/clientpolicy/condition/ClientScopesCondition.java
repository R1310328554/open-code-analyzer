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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.context.ClientModelContext;
import org.keycloak.services.clientpolicy.context.ScopeParameterContext;

import org.jboss.logging.Logger;

/**
 * 客户端策略条件：按客户端 Scope 配置（默认/可选/任意）与请求 scope 参数决定是否应用策略。
 * <p>仅在同时实现 {@link ScopeParameterContext} 与 {@link ClientModelContext} 的上下文中评估。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientScopesCondition extends AbstractClientPolicyConditionProvider<ClientScopesCondition.Configuration> {

    private static final Logger logger = Logger.getLogger(ClientScopesCondition.class);

    /** @param session Keycloak 会话 */
    public ClientScopesCondition(KeycloakSession session) {
        super(session);
    }

    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** 条件配置：匹配模式（default/optional/any）与期望 scope 列表 */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        /** 匹配类型，见 {@link ClientScopesConditionFactory#DEFAULT} 等常量 */
        protected String type;
        /** 期望匹配的客户端 scope 名称列表 */
        protected List<String> scopes;

        /** @return 匹配类型 */
        public String getType() {
            return type;
        }

        /** @param type 匹配类型 */
        public void setType(String type) {
            this.type = type;
        }

        /** @return 期望 scope 列表 */
        public List<String> getScopes() {
            return scopes;
        }

        /** @param scope 期望 scope 列表 */
        public void setScopes(List<String> scope) {
            this.scopes = scope;
        }
    }

    @Override
    public String getProviderId() {
        return ClientScopesConditionFactory.PROVIDER_ID;
    }

    /** 根据 scope 参数与客户端默认/可选 scope 评估条件 @param context 策略上下文 @return 投票结果 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        if (context instanceof ScopeParameterContext && context instanceof ClientModelContext) {
            String scope = ((ScopeParameterContext) context).getScopeParameter();
            ClientModel client = ((ClientModelContext) context).getClient();

            if (isScopeMatched(scope, client)) return ClientPolicyVote.YES;
            return ClientPolicyVote.NO;
        } else {
            return ClientPolicyVote.ABSTAIN;
        }
    }

    /** 按配置的 type 模式匹配显式 scope 与客户端 scope 集合 */
    private boolean isScopeMatched(String explicitScopes, ClientModel client) {
        if (client == null) {
            return false;
        }

        if (explicitScopes == null) explicitScopes = "";
        Collection<String> explicitSpecifiedScopes = new HashSet<>(Arrays.asList(explicitScopes.split(" ")));
        Set<String> defaultScopes = client.getClientScopes(true).keySet();
        Set<String> optionalScopes = client.getClientScopes(false).keySet();
        Set<String> expectedScopes = getScopesForMatching();
        if (expectedScopes == null) return false;

        if (logger.isTraceEnabled()) {
            explicitSpecifiedScopes.forEach(i -> logger.tracev("explicit specified client scope = {0}", i));
            defaultScopes.forEach(i -> logger.tracev("default client scope = {0}", i));
            optionalScopes.forEach(i -> logger.tracev("optional client scope = {0}", i));
            expectedScopes.forEach(i -> logger.tracev("expected scope = {0}", i));
        }

        switch (configuration.getType()) {
            case ClientScopesConditionFactory.DEFAULT:
                expectedScopes.retainAll(defaultScopes);
                return !expectedScopes.isEmpty();

            case ClientScopesConditionFactory.OPTIONAL:
                explicitSpecifiedScopes.retainAll(expectedScopes);
                explicitSpecifiedScopes.retainAll(optionalScopes);
                if (logger.isTraceEnabled()) {
                    explicitSpecifiedScopes.forEach(i->logger.tracev("matched scope = {0}", i));
                }
                return !explicitSpecifiedScopes.isEmpty();

            case ClientScopesConditionFactory.ANY:
                explicitSpecifiedScopes.retainAll(expectedScopes);
                explicitSpecifiedScopes.retainAll(optionalScopes);
                expectedScopes.retainAll(defaultScopes);
                return !expectedScopes.isEmpty() || !explicitSpecifiedScopes.isEmpty();

            default:
                return false;
        }
    }

    /** 复制配置中的期望 scope 集合 */
    private Set<String> getScopesForMatching() {
        List<String> scopes = configuration.getScopes();
        if (scopes == null) return null;
        return new HashSet<>(scopes);
    }
}
