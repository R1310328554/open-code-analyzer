/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.policy.provider.user;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.evaluation.partial.PartialEvaluationPolicyProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.ResourceType;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;

import org.jboss.logging.Logger;

/**
 * 用户策略提供者：当请求身份 ID 包含在策略配置的用户列表中时授予访问。
 * <p>
 * 同时实现 {@link PartialEvaluationPolicyProvider}，支持细粒度权限的部分评估。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UserPolicyProvider implements PolicyProvider, PartialEvaluationPolicyProvider {

    private static final Logger logger = Logger.getLogger(UserPolicyProvider.class);

    /** 将 {@link Policy} 转为 {@link UserPolicyRepresentation} 的函数 */
    private final BiFunction<Policy, AuthorizationProvider, UserPolicyRepresentation> representationFunction;

    /**
     * @param representationFunction 策略表示转换函数
     */
    public UserPolicyProvider(BiFunction<Policy, AuthorizationProvider, UserPolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    /**
     * 检查当前身份 ID 是否在策略 {@code users} 配置中，匹配则 {@code grant()}。
     *
     * @param evaluation 当前授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        Policy policy = evaluation.getPolicy();

        if (policy.getConfig().getOrDefault("users", "").contains(evaluation.getContext().getIdentity().getId())) {
            evaluation.grant();
        }

        if (logger.isDebugEnabled()) {
            logger.debugf("User policy %s evaluated to status %s on identity %s with accepted users: %s", evaluation.getPolicy().getName(), evaluation.getEffect(), evaluation.getContext().getIdentity().getId(), policy.getConfig().getOrDefault("users", ""));
        }
    }

    /**
     * 根据用户 ID 查找依赖该用户的权限策略（FGAP 部分评估）。
     */
    @Override
    public Stream<Policy> getPermissions(KeycloakSession session, ResourceType resourceType, ResourceType groupResourceType, UserModel subject) {
        AuthorizationProvider provider = session.getProvider(AuthorizationProvider.class);
        RealmModel realm = session.getContext().getRealm();
        ClientModel adminPermissionsClient = realm.getAdminPermissionsClient();
        StoreFactory storeFactory = provider.getStoreFactory();
        ResourceServer resourceServer = storeFactory.getResourceServerStore().findByClient(adminPermissionsClient);
        PolicyStore policyStore = storeFactory.getPolicyStore();

        return policyStore.findDependentPolicies(resourceServer, resourceType.getType(), groupResourceType == null ? null : groupResourceType.getType(), UserPolicyProviderFactory.ID, "users", subject.getId());
    }

    /** 对管理用户直接评估用户策略是否满足。 */
    @Override
    public boolean evaluate(KeycloakSession session, Policy policy, UserModel adminUser) {
        return policy.getConfig().getOrDefault("users", "").contains(adminUser.getId());
    }

    @Override
    public boolean supports(Policy policy) {
        return UserPolicyProviderFactory.ID.equals(policy.getType());
    }

    /** 用户策略无额外资源需释放。 */
    @Override
    public void close() {

    }
}
