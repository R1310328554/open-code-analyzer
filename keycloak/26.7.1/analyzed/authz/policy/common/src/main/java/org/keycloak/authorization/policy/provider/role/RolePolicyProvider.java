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
package org.keycloak.authorization.policy.provider.role;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.attribute.Attributes.Entry;
import org.keycloak.authorization.fgap.evaluation.partial.PartialEvaluationPolicyProvider;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.identity.UserModelIdentity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.authorization.ResourceType;
import org.keycloak.representations.idm.authorization.RolePolicyRepresentation;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.RoleUtils.getDeepUserRoleMappings;

/**
 * 角色策略提供者：根据身份是否持有策略配置的角色（realm 或 client 角色）判定是否授予访问。
 * <p>
 * 同时实现 {@link PartialEvaluationPolicyProvider}，支持细粒度权限的部分评估。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class RolePolicyProvider implements PolicyProvider, PartialEvaluationPolicyProvider {

    /** 将 {@link Policy} 转为 {@link RolePolicyRepresentation} 的函数 */
    private final BiFunction<Policy, AuthorizationProvider, RolePolicyRepresentation> representationFunction;

    private static final Logger logger = Logger.getLogger(RolePolicyProvider.class);

    /**
     * @param representationFunction 策略表示转换函数
     */
    public RolePolicyProvider(BiFunction<Policy, AuthorizationProvider, RolePolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    /**
     * 遍历策略角色定义，满足必需角色且至少匹配一个则 {@code grant()}。
     *
     * @param evaluation 当前授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        Policy policy = evaluation.getPolicy();
        RolePolicyRepresentation policyRep = representationFunction.apply(policy, evaluation.getAuthorizationProvider());
        AuthorizationProvider authorizationProvider = evaluation.getAuthorizationProvider();
        RealmModel realm = authorizationProvider.getKeycloakSession().getContext().getRealm();
        Identity identity = evaluation.getContext().getIdentity();

        if (isGranted(realm, authorizationProvider, policyRep, identity)) {
            evaluation.grant();
        }

        logger.debugf("policy %s evaluated with status %s on identity %s", policy.getName(), evaluation.getEffect(), identity.getId());
    }

    /**
     * 检查身份是否满足策略中所有角色定义（含必需角色与可选角色逻辑）。
     */
    private boolean isGranted(RealmModel realm, AuthorizationProvider authorizationProvider, RolePolicyRepresentation policyRep, Identity identity) {
        Set<RolePolicyRepresentation.RoleDefinition> roleIds = policyRep.getRoles();
        boolean granted = false;

        for (RolePolicyRepresentation.RoleDefinition roleDefinition : roleIds) {
            RoleModel role = realm.getRoleById(roleDefinition.getId());

            if (role != null) {
                boolean isFetchRoles = policyRep.isFetchRoles() != null && policyRep.isFetchRoles();
                boolean hasRole = hasRole(identity, role, realm, authorizationProvider, isFetchRoles);

                if (!hasRole && roleDefinition.isRequired() != null && roleDefinition.isRequired()) {
                    return false;
                } else if (hasRole) {
                    granted = true;
                }
            }
        }

        return granted;
    }

    /**
     * 判断身份是否持有指定角色；{@code fetchRoles} 为 true 时从数据库加载用户角色映射。
     */
    private boolean hasRole(Identity identity, RoleModel role, RealmModel realm, AuthorizationProvider authorizationProvider, boolean fetchRoles) {
        if (fetchRoles) {
            UserModel subject = getSubject(identity, realm, authorizationProvider);
            return subject != null && subject.hasRole(role);
        }
        String roleName = role.getName();
        if (role.isClientRole()) {
            ClientModel clientModel = realm.getClientById(role.getContainerId());
            return identity.hasClientRole(clientModel.getClientId(), roleName);
        }
        return identity.hasRealmRole(roleName);
    }

    /** 解析评估主体用户：优先按身份 ID，否则回退到 JWT {@code sub} claim。 */
    private UserModel getSubject(Identity identity, RealmModel realm, AuthorizationProvider authorizationProvider) {
        KeycloakSession session = authorizationProvider.getKeycloakSession();
        UserProvider users = session.users();
        UserModel user = users.getUserById(realm, identity.getId());

        if (user == null) {
            Entry sub = identity.getAttributes().getValue(JsonWebToken.SUBJECT);

            if (sub == null || sub.isEmpty()) {
                return null;
            }

            return users.getUserById(realm, sub.asString(0));
        }

        return user;
    }

    /** 角色策略无额外资源需释放。 */
    @Override
    public void close() {

    }

    /**
     * 根据用户深度角色映射查找依赖该角色的权限策略（FGAP 部分评估）。
     */
    @Override
    public Stream<Policy> getPermissions(KeycloakSession session, ResourceType resourceType, ResourceType groupResourceType, UserModel subject) {
        AuthorizationProvider provider = session.getProvider(AuthorizationProvider.class);
        RealmModel realm = session.getContext().getRealm();
        ClientModel adminPermissionsClient = realm.getAdminPermissionsClient();
        StoreFactory storeFactory = provider.getStoreFactory();
        ResourceServer resourceServer = storeFactory.getResourceServerStore().findByClient(adminPermissionsClient);
        PolicyStore policyStore = storeFactory.getPolicyStore();
        List<String> roleIds = getDeepUserRoleMappings(subject).stream().map(RoleModel::getId).toList();
        Stream<Policy> policies = Stream.of();

        return Stream.concat(policies, policyStore.findDependentPolicies(resourceServer, resourceType.getType(), groupResourceType == null ? null : groupResourceType.getType(), RolePolicyProviderFactory.ID, "roles", roleIds));
    }

    /** 对管理用户直接评估角色策略是否满足。 */
    @Override
    public boolean evaluate(KeycloakSession session, Policy policy, UserModel adminUser) {
        RealmModel realm = session.getContext().getRealm();
        AuthorizationProvider authorizationProvider = session.getProvider(AuthorizationProvider.class);
        return isGranted(realm, authorizationProvider, representationFunction.apply(policy, authorizationProvider), new UserModelIdentity(realm, adminUser));
    }

    @Override
    public boolean supports(Policy policy) {
        return RolePolicyProviderFactory.ID.equals(policy.getType());
    }
}
