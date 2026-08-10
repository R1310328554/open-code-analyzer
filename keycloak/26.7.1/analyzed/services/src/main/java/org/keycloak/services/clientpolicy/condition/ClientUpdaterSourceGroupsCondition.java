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

import org.keycloak.OAuthErrorException;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.context.AdminClientRegisterContext;
import org.keycloak.services.clientpolicy.context.AdminClientRegisteredContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdateContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdatedContext;
import org.keycloak.services.clientpolicy.context.ClientCRUDContext;
import org.keycloak.services.clientpolicy.context.DynamicClientRegisterContext;
import org.keycloak.services.clientpolicy.context.DynamicClientRegisteredContext;
import org.keycloak.services.clientpolicy.context.DynamicClientUpdateContext;
import org.keycloak.services.clientpolicy.context.DynamicClientUpdatedContext;

import org.jboss.logging.Logger;

/**
 * 客户端更新者组条件：按执行客户端创建/更新的用户所属组匹配策略。
 * <p>Admin API 路径使用 {@link UserModel}；动态注册路径从 JWT subject 解析用户。</p>
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientUpdaterSourceGroupsCondition extends AbstractClientPolicyConditionProvider<ClientUpdaterSourceGroupsCondition.Configuration> {

    private static final Logger logger = Logger.getLogger(ClientUpdaterSourceGroupsCondition.class);

    /** @param session Keycloak 会话 */
    public ClientUpdaterSourceGroupsCondition(KeycloakSession session) {
        super(session);
    }

    /** {@inheritDoc} @return 条件配置类型 */
    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** 条件配置：期望匹配的用户组名称列表。 */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        protected List<String> groups;

        /** @return 期望的用户组名称列表 */
        public List<String> getGroups() {
            return groups;
        }

        /** @param groups 用户组名称列表 */
        public void setGroups(List<String> groups) {
            this.groups = groups;
        }
    }

    /** {@inheritDoc} @return {@link ClientUpdaterSourceGroupsConditionFactory#PROVIDER_ID} */
    @Override
    public String getProviderId() {
        return ClientUpdaterSourceGroupsConditionFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} 在客户端注册/更新事件上按更新者组投票 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
        case REGISTER:
        case REGISTERED:
            if (context instanceof AdminClientRegisterContext || context instanceof AdminClientRegisteredContext) {
                return getVoteForGroupsMatched(((ClientCRUDContext)context).getAuthenticatedUser());
            } else if (context instanceof DynamicClientRegisterContext || context instanceof DynamicClientRegisteredContext) {
                return getVoteForGroupsMatched(((ClientCRUDContext)context).getToken());
            } else {
                throw new ClientPolicyException(OAuthErrorException.SERVER_ERROR, "unexpected context type.");
            }
        case UPDATE:
        case UPDATED:
            if (context instanceof AdminClientUpdateContext || context instanceof AdminClientUpdatedContext) {
                return getVoteForGroupsMatched(((ClientCRUDContext)context).getAuthenticatedUser());
            } else if (context instanceof DynamicClientUpdateContext || context instanceof DynamicClientUpdatedContext) {
                return getVoteForGroupsMatched(((ClientCRUDContext)context).getToken());
            } else {
                throw new ClientPolicyException(OAuthErrorException.SERVER_ERROR, "unexpected context type.");
            }
        default:
            return ClientPolicyVote.ABSTAIN;
        }
    }

    /** 按用户组成员关系投票。 @param user 已认证用户 */
    private ClientPolicyVote getVoteForGroupsMatched(UserModel user) {
        if (isGroupsMatched(user)) return ClientPolicyVote.YES;
        return ClientPolicyVote.NO;
    }

    /** 从 JWT subject 解析用户并按组投票。 @param token 注册/更新令牌 */
    private ClientPolicyVote getVoteForGroupsMatched(JsonWebToken token) {
        if (token == null) return ClientPolicyVote.NO;
        if(isGroupMatched(token.getSubject())) return ClientPolicyVote.YES;
        return ClientPolicyVote.NO;
    }

    /** 按用户 ID 加载用户并检查组匹配。 @param subjectId JWT subject */
    private boolean isGroupMatched(String subjectId) {
        if (subjectId == null) return false;
        return isGroupsMatched(session.users().getUserById(session.getContext().getRealm(), subjectId));
    }

    /** 判断用户是否属于配置的任一期望组。 @param user 待检查用户 */
    private boolean isGroupsMatched(UserModel user) {
        if (user == null) return false;

        Set<String> expectedGroups = instantiateGroupsForMatching();
        if (expectedGroups == null) return false;

        // user.getGroupsStream() 按 {@link UserModel#getGroupsStream} 约定不会返回 null
        Set<String> groups = user.getGroupsStream().map(GroupModel::getName).collect(Collectors.toSet());

        if (logger.isTraceEnabled()) {
            groups.forEach(i -> logger.tracev("user group = {0}", i));
            expectedGroups.forEach(i -> logger.tracev("expected user group = {0}", i));
        }

        return expectedGroups.removeAll(groups); // removeAll 会修改 expectedGroups 副本，故须每次实例化
    }

    /** 从配置实例化期望组集合（可变的副本）。 */
    private Set<String> instantiateGroupsForMatching() {
        List<String> groups = configuration.getGroups();
        if (groups == null) return null;
        return new HashSet<>(groups);
    }

}
