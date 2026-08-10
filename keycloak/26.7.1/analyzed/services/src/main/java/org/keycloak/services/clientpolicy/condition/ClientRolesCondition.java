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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.context.ClientModelContext;
import org.keycloak.services.clientpolicy.context.PreAuthorizationRequestContext;

import org.jboss.logging.Logger;

import static org.keycloak.services.clientpolicy.ClientPolicyEvent.PRE_AUTHORIZATION_REQUEST;

/**
 * 客户端策略条件：要求客户端至少拥有配置列表中的一个客户端角色才满足条件。
 * <p>在预授权请求或 {@link ClientModelContext} 事件中评估，便于通过角色标记需施加策略的客户端。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientRolesCondition extends AbstractClientPolicyConditionProvider<ClientRolesCondition.Configuration> {

    private static final Logger logger = Logger.getLogger(ClientRolesCondition.class);

    /** @param session Keycloak 会话 */
    public ClientRolesCondition(KeycloakSession session) {
        super(session);
    }

    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** 条件配置：需匹配的客户端角色名列表（任一命中即可） */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        /** 期望存在的客户端角色名称列表 */
        protected List<String> roles;

        /** @return 配置的角色列表 */
        public List<String> getRoles() {
            return roles;
        }

        /** @param roles 角色名称列表 */
        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }

    @Override
    public String getProviderId() {
        return ClientRolesConditionFactory.PROVIDER_ID;
    }

    /** 在预授权或客户端模型上下文中比对客户端角色 @param context 策略上下文 @return 投票结果 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        if (context.getEvent() == PRE_AUTHORIZATION_REQUEST) {
            PreAuthorizationRequestContext paContext = (PreAuthorizationRequestContext) context;
            ClientModel client = session.getContext().getRealm().getClientByClientId(paContext.getClientId());
            if (isRolesMatched(client)) return ClientPolicyVote.YES;
            return ClientPolicyVote.NO;
        } else if (context instanceof ClientModelContext) {
            ClientModel client = ((ClientModelContext) context).getClient();
            if (isRolesMatched(client)) return ClientPolicyVote.YES;
            return ClientPolicyVote.NO;
        } else {
            return ClientPolicyVote.ABSTAIN;
        }
    }

    /** 判断客户端是否拥有配置要求的至少一个角色 */
    private boolean isRolesMatched(ClientModel client) {
        if (client == null) return false;

        Set<String> rolesForMatching = getRolesForMatching();
        if (rolesForMatching == null) return false;

        // client.getRolesStream() 按 {@link RoleProvider#getClientRolesStream} 约定不会返回 null
        Set<String> clientRoles = client.getRolesStream().map(RoleModel::getName).collect(Collectors.toSet());

        if (logger.isTraceEnabled()) {
            clientRoles.forEach(i -> logger.tracev("client role assigned = {0}", i));
            rolesForMatching.forEach(i -> logger.tracev("client role for matching = {0}", i));
        }

        return rolesForMatching.removeAll(clientRoles);  // removeAll 会修改集合，故需独立实例化
    }

    /** 复制配置中的角色集合供匹配使用 */
    private Set<String> getRolesForMatching() {
        if (configuration.getRoles() == null) return null;
        return new HashSet<>(configuration.getRoles());
    }

}
