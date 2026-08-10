/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.ldap.mappers.membership.role;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.LDAPUtils;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.membership.CommonLDAPGroupMapper;
import org.keycloak.storage.ldap.mappers.membership.CommonLDAPGroupMapperConfig;
import org.keycloak.storage.ldap.mappers.membership.LDAPGroupMapperMode;
import org.keycloak.storage.ldap.mappers.membership.UserRolesRetrieveStrategy;
import org.keycloak.storage.user.SynchronizationResult;

import org.jboss.logging.Logger;

/**
 * LDAP 角色映射器：将指定 LDAP DN 下的 LDAP 组映射为 Keycloak 领域角色或客户端角色。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RoleLDAPStorageMapper extends AbstractLDAPStorageMapper implements CommonLDAPGroupMapper {

    private static final Logger logger = Logger.getLogger(RoleLDAPStorageMapper.class);

    private final RoleMapperConfig config;
    private final RoleLDAPStorageMapperFactory factory;

    public RoleLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider, RoleLDAPStorageMapperFactory factory) {
        super(mapperModel, ldapProvider);
        this.config = new RoleMapperConfig(mapperModel);
        this.factory = factory;
    }


    @Override
    public LDAPQuery createLDAPGroupQuery() {
        return createRoleQuery(false);
    }

    @Override
    public CommonLDAPGroupMapperConfig getConfig() {
        return config;
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        LDAPGroupMapperMode mode = config.getMode();

        // 当前仅在用户首次从 LDAP 导入时同步角色映射
        if (mode == LDAPGroupMapperMode.IMPORT && isCreate) {

            List<LDAPObject> ldapRoles = getLDAPRoleMappings(ldapUser);

            // 将 LDAP 角色映射导入 Keycloak 数据库
            String roleNameAttr = config.getRoleNameLdapAttribute();

            RoleContainerModel roleContainer = getTargetRoleContainer(realm);
            if (roleContainer == null) {
                logger.warnf("Ignored client role grant for federation mapper '%s' as client not found: '%s'", mapperModel.getName(), config.getClientId());
                return;
            }

            for (LDAPObject ldapRole : ldapRoles) {
                String roleName = ldapRole.getAttributeAsString(roleNameAttr);
                RoleModel role = roleContainer.getRole(roleName);

                if (role == null) {
                    role = roleContainer.addRole(roleName);
                }

                logger.debugf("Granting role [%s] to user [%s] during import from LDAP", roleName, user.getUsername());
                user.grantRole(role);
            }
        }
    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {
    }


    /** 从 LDAP 同步角色到 Keycloak 数据库。 */
    @Override
    public SynchronizationResult syncDataFromFederationProviderToKeycloak(RealmModel realm) {
        SynchronizationResult syncResult = new SynchronizationResult() {

            @Override
            public String getStatus() {
                return String.format("%d imported roles, %d roles already exists in Keycloak", getAdded(), getUpdated());
            }

        };

        logger.debugf("Syncing roles from LDAP into Keycloak DB. Mapper is [%s], LDAP provider is [%s]", mapperModel.getName(), ldapProvider.getModel().getName());

        RoleContainerModel roleContainer = getTargetRoleContainer(realm);
        if (roleContainer == null) {
            logger.warnf("Ignored sync for federation mapper '%s' as client not found: '%s'", mapperModel.getName(), config.getClientId());
            return syncResult;
        }

        // 查询 LDAP 加载全部角色
        try (LDAPQuery ldapRoleQuery = createRoleQuery(false)) {
            List<LDAPObject> ldapRoles = LDAPUtils.loadAllLDAPObjects(ldapRoleQuery, ldapProvider);

            String rolesRdnAttr = config.getRoleNameLdapAttribute();
            for (LDAPObject ldapRole : ldapRoles) {
                String roleName = ldapRole.getAttributeAsString(rolesRdnAttr);

                if (roleContainer.getRole(roleName) == null) {
                    logger.debugf("Syncing role [%s] from LDAP to keycloak DB", roleName);
                    roleContainer.addRole(roleName);
                    syncResult.increaseAdded();
                } else {
                    syncResult.increaseUpdated();
                }
            }

            return syncResult;
        }
    }


    /** 从 Keycloak 同步角色到 LDAP。 */
    @Override
    public SynchronizationResult syncDataFromKeycloakToFederationProvider(RealmModel realm) {
        SynchronizationResult syncResult = new SynchronizationResult() {

            @Override
            public String getStatus() {
                return String.format("%d roles imported to LDAP, %d roles already existed in LDAP", getAdded(), getUpdated());
            }

        };

        if (config.getMode() != LDAPGroupMapperMode.LDAP_ONLY) {
            logger.warnf("Ignored sync for federation mapper '%s' as it's mode is '%s'", mapperModel.getName(), config.getMode().toString());
            return syncResult;
        }

        logger.debugf("Syncing roles from Keycloak into LDAP. Mapper is [%s], LDAP provider is [%s]", mapperModel.getName(), ldapProvider.getModel().getName());

        RoleContainerModel roleContainer = getTargetRoleContainer(realm);
        if (roleContainer == null) {
            logger.warnf("Ignored sync for federation mapper '%s' as client not found: '%s'", mapperModel.getName(), config.getClientId());
            return syncResult;
        }

        // 查询 LDAP 获取已存在的角色
        try (LDAPQuery ldapQuery = createRoleQuery(false)) {
            List<LDAPObject> ldapRoles = LDAPUtils.loadAllLDAPObjects(ldapQuery, ldapProvider);

            Set<String> ldapRoleNames = new HashSet<>();
            String rolesRdnAttr = config.getRoleNameLdapAttribute();
            for (LDAPObject ldapRole : ldapRoles) {
                String roleName = ldapRole.getAttributeAsString(rolesRdnAttr);
                ldapRoleNames.add(roleName);
            }


            Stream<RoleModel> keycloakRoles = roleContainer.getRolesStream();

            Consumer<String> syncRoleFromKCToLDAP = roleName -> {
                if (ldapRoleNames.contains(roleName)) {
                    syncResult.increaseUpdated();
                } else {
                    logger.debugf("Syncing role [%s] from Keycloak to LDAP", roleName);
                    createLDAPRole(roleName);
                    syncResult.increaseAdded();
                }
            };
            keycloakRoles.map(RoleModel::getName).forEach(syncRoleFromKCToLDAP);

            return syncResult;
        }
    }

    /** 构建 LDAP 角色查询；{@code includeMemberAttribute} 为 true 时一并返回成员属性以优化性能。 */
    // TODO: Possible to merge with GroupMapper and move to common class
    public LDAPQuery createRoleQuery(boolean includeMemberAttribute) {
        LDAPQuery ldapQuery = new LDAPQuery(ldapProvider);

        // 使用与用户搜索相同的全局搜索范围
        ldapQuery.setSearchScope(ldapProvider.getLdapIdentityStore().getConfig().getSearchScope());

        String rolesDn = config.getRolesDn();
        ldapQuery.setSearchDn(rolesDn);

        Collection<String> roleObjectClasses = config.getRoleObjectClasses(ldapProvider);
        ldapQuery.addObjectClasses(roleObjectClasses);

        String rolesRdnAttr = config.getRoleNameLdapAttribute();

        String customFilter = config.getCustomLdapFilter();
        if (customFilter != null && customFilter.trim().length() > 0) {
            Condition customFilterCondition = new LDAPQueryConditionsBuilder().addCustomLDAPFilter(customFilter);
            ldapQuery.addWhereCondition(customFilterCondition);
        }

        ldapQuery.addReturningLdapAttribute(rolesRdnAttr);

        // 性能优化：按需返回成员属性
        if (includeMemberAttribute) {
            String membershipAttr = config.getMembershipLdapAttribute();
            ldapQuery.addReturningLdapAttribute(membershipAttr);
        }

        return ldapQuery;
    }

    /** 返回角色映射的目标容器（领域或客户端）。 */
    protected RoleContainerModel getTargetRoleContainer(RealmModel realm) {
        boolean realmRolesMapping = config.isRealmRolesMapping();
        if (realmRolesMapping) {
            return realm;
        } else {
            String clientId = config.getClientId();
            if (clientId == null) {
                throw new ModelException("Using client roles mapping is requested, but parameter client.id not found!");
            }
            ClientModel client = realm.getClientByClientId(clientId);
            if (client == null) {
                logger.warnf("Cannot find requested client with clientId '%s' in federation mapper '%s'", clientId, mapperModel.getName());
            }
            return client;
        }
    }


    /** 在 LDAP 中创建角色条目。 */
    public LDAPObject createLDAPRole(String roleName) {
        LDAPObject ldapRole = LDAPUtils.createLDAPGroup(ldapProvider, roleName, config.getRoleNameLdapAttribute(), config.getRoleObjectClasses(ldapProvider),
                config.getRelativeCreateDn() + config.getRolesDn(), Collections.<String, Set<String>>emptyMap(), config.getMembershipLdapAttribute());

        logger.debugf("Creating role [%s] to LDAP with DN [%s]", roleName, ldapRole.getDn().toString());
        return ldapRole;
    }

    /** 在 LDAP 中为用户添加角色成员关系。 */
    public void addRoleMappingInLDAP(String roleName, LDAPObject ldapUser) {
        LDAPObject ldapRole = loadLDAPRoleByName(roleName);
        if (ldapRole == null) {
            ldapRole = createLDAPRole(roleName);
        }

        String membershipUserAttrName = getMembershipUserLdapAttribute();

        LDAPUtils.addMember(ldapProvider, config.getMembershipTypeLdapAttribute(), config.getMembershipLdapAttribute(), membershipUserAttrName, ldapRole, ldapUser);
    }

    /** 从 LDAP 角色中移除用户成员关系。 */
    public void deleteRoleMappingInLDAP(LDAPObject ldapUser, LDAPObject ldapRole) {
        String membershipUserAttrName = getMembershipUserLdapAttribute();
        LDAPUtils.deleteMember(ldapProvider, config.getMembershipTypeLdapAttribute(), config.getMembershipLdapAttribute(), membershipUserAttrName, ldapRole, ldapUser);
    }

    /** 按角色名加载 LDAP 角色对象。 */
    public LDAPObject loadLDAPRoleByName(String roleName) {
        try (LDAPQuery ldapQuery = createRoleQuery(true)) {
            Condition roleNameCondition = new LDAPQueryConditionsBuilder().equal(config.getRoleNameLdapAttribute(), roleName);
            ldapQuery.addWhereCondition(roleNameCondition);
            return ldapQuery.getFirstResult();
        }
    }

    /** 按配置策略获取用户的 LDAP 角色映射列表。 */
    protected List<LDAPObject> getLDAPRoleMappings(LDAPObject ldapUser) {
        String strategyKey = config.getUserRolesRetrieveStrategy();
        UserRolesRetrieveStrategy strategy = factory.getUserRolesRetrieveStrategy(strategyKey);

        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();
        return strategy.getLDAPRoleMappings(this, ldapUser, ldapConfig);
    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        final LDAPGroupMapperMode mode = config.getMode();

        // IMPORT 模式下所有操作针对本地数据库
        if (mode == LDAPGroupMapperMode.IMPORT) {
            return delegate;
        }
        final RoleContainerModel targetRoleContainer = getTargetRoleContainer(realm);
        if (targetRoleContainer == null) {
            return delegate;
        } else {
            return new LDAPRoleMappingsUserDelegate(realm, delegate, ldapUser, targetRoleContainer);
        }
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {
        String strategyKey = config.getUserRolesRetrieveStrategy();
        UserRolesRetrieveStrategy strategy = factory.getUserRolesRetrieveStrategy(strategyKey);
        strategy.beforeUserLDAPQuery(this, query);
    }


    protected String getMembershipUserLdapAttribute() {
        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();
        return config.getMembershipUserLdapAttribute(ldapConfig);
    }


    /** 用户代理：在 LDAP_ONLY/READ_ONLY 模式下合并或覆盖 LDAP 角色映射。 */
    public class LDAPRoleMappingsUserDelegate extends UserModelDelegate {

        private final RealmModel realm;
        private final LDAPObject ldapUser;
        private final RoleContainerModel roleContainer;

        // 避免同一请求内多次从 LDAP 加载角色映射
        private Set<RoleModel> cachedLDAPRoleMappings;

        public LDAPRoleMappingsUserDelegate(RealmModel realm, UserModel user, LDAPObject ldapUser, RoleContainerModel targetRoleContainer) {
            super(user);
            this.realm = realm;
            this.ldapUser = ldapUser;
            this.roleContainer = targetRoleContainer;
        }

        @Override
        public Stream<RoleModel> getRealmRoleMappingsStream() {
            if (roleContainer.equals(realm)) {
                Stream<RoleModel> ldapRoleMappings = getLDAPRoleMappingsConverted();

                if (config.getMode() == LDAPGroupMapperMode.LDAP_ONLY) {
                    // 仅使用 LDAP 中的角色映射
                    return ldapRoleMappings;
                } else {
                    // 合并数据库与 LDAP 的角色映射
                    return Stream.concat(ldapRoleMappings, super.getRealmRoleMappingsStream());
                }
            } else {
                return super.getRealmRoleMappingsStream();
            }
        }

        @Override
        public Stream<RoleModel> getClientRoleMappingsStream(ClientModel client) {
            if (roleContainer.equals(client)) {
                Stream<RoleModel> ldapRoleMappings = getLDAPRoleMappingsConverted();

                if (config.getMode() == LDAPGroupMapperMode.LDAP_ONLY) {
                    // 仅使用 LDAP 中的角色映射
                    return ldapRoleMappings;
                } else {
                    // 合并数据库与 LDAP 的角色映射
                    return Stream.concat(ldapRoleMappings, super.getClientRoleMappingsStream(client));
                }
            } else {
                return super.getClientRoleMappingsStream(client);
            }
        }

        @Override
        public boolean hasRole(RoleModel role) {
            return RoleUtils.hasRole(getRoleMappingsStream(), role)
              || RoleUtils.hasRoleFromGroup(getGroupsStream(), role, true);
        }

        @Override
        public void grantRole(RoleModel role) {
            if (config.getMode() == LDAPGroupMapperMode.LDAP_ONLY) {

                if (role.getContainer().equals(roleContainer)) {

                    // 需在 LDAP 中创建新的角色映射
                    cachedLDAPRoleMappings = null;
                    addRoleMappingInLDAP(role.getName(), ldapUser);
                } else {
                    super.grantRole(role);
                }
            } else {
                super.grantRole(role);
            }
        }

        @Override
        public Stream<RoleModel> getRoleMappingsStream() {
            Stream<RoleModel> modelRoleMappings = super.getRoleMappingsStream();

            Stream<RoleModel> ldapRoleMappings = getLDAPRoleMappingsConverted();

            if (config.getMode() == LDAPGroupMapperMode.LDAP_ONLY) {
                // LDAP_ONLY 模式下目标容器的角色映射仅来自 LDAP
                modelRoleMappings = modelRoleMappings.filter(role -> !Objects.equals(role.getContainer(), roleContainer));
            }

            return Stream.concat(modelRoleMappings, ldapRoleMappings);
        }

        /** 将 LDAP 角色对象转换为 Keycloak {@link RoleModel} 流。 */
        protected Stream<RoleModel> getLDAPRoleMappingsConverted() {
            if (cachedLDAPRoleMappings != null) {
                return cachedLDAPRoleMappings.stream();
            }

            List<LDAPObject> ldapRoles = getLDAPRoleMappings(ldapUser);
            String roleNameLdapAttr = config.getRoleNameLdapAttribute();
            cachedLDAPRoleMappings = ldapRoles.stream()
                    .map(role -> {
                        String roleName = role.getAttributeAsString(roleNameLdapAttr);
                        RoleModel modelRole = roleContainer.getRole(roleName);
                        if (modelRole == null) {
                            // 在本地数据库中创建角色
                            modelRole = roleContainer.addRole(roleName);
                        }
                        return modelRole;
                    }).collect(Collectors.toSet());

            return cachedLDAPRoleMappings.stream();
        }

        @Override
        public void deleteRoleMapping(RoleModel role) {
            if (role.getContainer().equals(roleContainer)) {

                try (LDAPQuery ldapQuery = createRoleQuery(true)) {
                    LDAPQueryConditionsBuilder conditionsBuilder = new LDAPQueryConditionsBuilder();
                    Condition roleNameCondition = conditionsBuilder.equal(config.getRoleNameLdapAttribute(), role.getName());

                    String membershipUserAttrName = getMembershipUserLdapAttribute();
                    String membershipUserAttr = LDAPUtils.getMemberValueOfChildObject(ldapUser, config.getMembershipTypeLdapAttribute(), membershipUserAttrName);

                    Condition membershipCondition = conditionsBuilder.equal(config.getMembershipLdapAttribute(), membershipUserAttr);

                    ldapQuery.addWhereCondition(roleNameCondition).addWhereCondition(membershipCondition);
                    LDAPObject ldapRole = ldapQuery.getFirstResult();

                    if (ldapRole == null) {
                        // LDAP 中无此映射：LDAP_ONLY 无需操作；READ_ONLY 则删除本地映射
                        if (config.getMode() == LDAPGroupMapperMode.READ_ONLY) {
                            super.deleteRoleMapping(role);
                        }
                    } else {
                        // LDAP 中存在映射：LDAP_ONLY 可删除；READ_ONLY 不可删除
                        if (config.getMode() == LDAPGroupMapperMode.READ_ONLY) {
                            throw new ModelException("Not possible to delete LDAP role mappings as mapper mode is READ_ONLY");
                        } else {
                            // 删除 LDAP 角色映射
                            cachedLDAPRoleMappings = null;
                            deleteRoleMappingInLDAP(ldapUser, ldapRole);
                        }
                    }
                }
            } else {
                super.deleteRoleMapping(role);
            }
        }
    }

    public LDAPObject loadRoleGroupByName(String roleName) {
        try (LDAPQuery ldapQuery = createRoleQuery(true)) {
            Condition roleNameCondition = new LDAPQueryConditionsBuilder().equal(config.getRoleNameLdapAttribute(), roleName);
            ldapQuery.addWhereCondition(roleNameCondition);
            return ldapQuery.getFirstResult();
        }
    }

    @Override
    public List<UserModel> getRoleMembers(RealmModel realm, RoleModel role, int firstResult, int maxResults) {
        if (config.getMode() == LDAPGroupMapperMode.IMPORT) {
            // IMPORT 模式仅返回 Keycloak 侧结果，避免与 LDAP 成员重复
            return Collections.emptyList();
        }

        LDAPObject ldapGroup = loadRoleGroupByName(role.getName());
        if (ldapGroup == null) {
            return Collections.emptyList();
        }

        String strategyKey = config.getUserRolesRetrieveStrategy();
        UserRolesRetrieveStrategy strategy = factory.getUserRolesRetrieveStrategy(strategyKey);
        return strategy.getLDAPRoleMembers(realm, this, ldapGroup, firstResult, maxResults);
    }
}
