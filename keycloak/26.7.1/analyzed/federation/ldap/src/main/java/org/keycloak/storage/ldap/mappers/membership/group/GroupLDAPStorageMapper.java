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

package org.keycloak.storage.ldap.mappers.membership.group;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.LDAPUtils;
import org.keycloak.storage.ldap.idm.model.LDAPDn;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPUtil;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.membership.CommonLDAPGroupMapper;
import org.keycloak.storage.ldap.mappers.membership.CommonLDAPGroupMapperConfig;
import org.keycloak.storage.ldap.mappers.membership.LDAPGroupMapperMode;
import org.keycloak.storage.ldap.mappers.membership.MembershipType;
import org.keycloak.storage.ldap.mappers.membership.UserRolesRetrieveStrategy;
import org.keycloak.storage.user.SynchronizationResult;

import org.jboss.logging.Logger;

/**
 * 组 LDAP 存储映射器：将 LDAP 组 DN 下的组结构同步到 Keycloak 组，并管理用户-组成员关系。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GroupLDAPStorageMapper extends AbstractLDAPStorageMapper implements CommonLDAPGroupMapper {

    private static final Logger logger = Logger.getLogger(GroupLDAPStorageMapper.class);

    private final GroupMapperConfig config;
    private final GroupLDAPStorageMapperFactory factory;

    // 避免同一事务内重复执行 LDAP→Keycloak 全量同步
    private boolean syncFromLDAPPerformedInThisTransaction = false;

    /** 构造映射器并绑定配置与工厂。 */
    public GroupLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider, GroupLDAPStorageMapperFactory factory) {
        super(mapperModel, ldapProvider);
        this.config = new GroupMapperConfig(mapperModel);
        this.factory = factory;
    }


    // CommonLDAPGroupMapper 接口实现

    /** {@inheritDoc} 创建不含 member 属性的组查询。 */
    @Override
    public LDAPQuery createLDAPGroupQuery() {
        return createGroupQuery(false);
    }

    @Override
    public CommonLDAPGroupMapperConfig getConfig() {
        return config;
    }



    // LDAP 组 CRUD 操作
    // !! 必须在 try-with-resources 中调用，否则 vault 密钥可能泄漏 !!
    public LDAPQuery createGroupQuery(boolean includeMemberAttribute) {
        LDAPQuery ldapQuery = new LDAPQuery(ldapProvider);

        // 暂与全局用户搜索范围一致
        ldapQuery.setSearchScope(ldapProvider.getLdapIdentityStore().getConfig().getSearchScope());

        String groupsDn = config.getGroupsDn();
        ldapQuery.setSearchDn(groupsDn);

        Collection<String> groupObjectClasses = config.getGroupObjectClasses(ldapProvider);
        ldapQuery.addObjectClasses(groupObjectClasses);

        String customFilter = config.getCustomLdapFilter();
        if (customFilter != null && customFilter.trim().length() > 0) {
            Condition customFilterCondition = new LDAPQueryConditionsBuilder().addCustomLDAPFilter(customFilter);
            ldapQuery.addWhereCondition(customFilterCondition);
        }

        ldapQuery.addReturningLdapAttribute(config.getGroupNameLdapAttribute());

        // 性能优化：按需返回 member 属性
        if (includeMemberAttribute) {
            ldapQuery.addReturningLdapAttribute(config.getMembershipLdapAttribute());
        }

        for (String groupAttr : config.getGroupAttributes()) {
            ldapQuery.addReturningLdapAttribute(groupAttr);
        }

        return ldapQuery;
    }

    /** 在 LDAP 中创建组对象。 */
    public LDAPObject createLDAPGroup(String groupName, Map<String, Set<String>> additionalAttributes) {
        LDAPObject ldapGroup = LDAPUtils.createLDAPGroup(ldapProvider, groupName, config.getGroupNameLdapAttribute(), config.getGroupObjectClasses(ldapProvider),
                config.getRelativeCreateDn() + config.getGroupsDn(), additionalAttributes, config.getMembershipLdapAttribute());

        logger.debugf("Creating group [%s] to LDAP with DN [%s]", groupName, ldapGroup.getDn().toString());
        return ldapGroup;
    }

    /** 按组名加载 LDAP 组。 */
    public LDAPObject loadLDAPGroupByName(String groupName) {
        try (LDAPQuery ldapQuery = createGroupQuery(true)) {
            Condition roleNameCondition = new LDAPQueryConditionsBuilder().equal(config.getGroupNameLdapAttribute(), groupName);
            ldapQuery.addWhereCondition(roleNameCondition);
            return ldapQuery.getFirstResult();
        }
    }

    public LDAPObject updateLDAPGroup(LDAPObject ldapObject) {
        LDAPObject ldapGroup = LDAPUtils.updateLDAPGroup(ldapProvider, ldapObject);

        return ldapGroup;
    }

    protected Set<LDAPDn> getLDAPSubgroups(LDAPObject ldapGroup) {
        MembershipType membershipType = config.getMembershipTypeLdapAttribute();
        return membershipType.getLDAPSubgroups(this, ldapGroup);
    }


    // 从 LDAP 同步到 Keycloak

    /** {@inheritDoc} 将 LDAP 组树同步到 Keycloak 组结构。 */
    @Override
    public SynchronizationResult syncDataFromFederationProviderToKeycloak(RealmModel realm) {
        SynchronizationResult syncResult = new SynchronizationResult() {

            @Override
            public String getStatus() {
                return String.format("%d imported groups, %d updated groups, %d removed groups", getAdded(), getUpdated(), getRemoved());
            }

        };

        logger.debugf("Syncing groups from LDAP into Keycloak DB. Mapper is [%s], LDAP provider is [%s]", mapperModel.getName(), ldapProvider.getModel().getName());

        // 获取全部 LDAP 组
        List<LDAPObject> ldapGroups = getAllLDAPGroups(config.isPreserveGroupsInheritance());

        // 转换为内部表示
        Map<String, LDAPObject> ldapGroupsMap = new HashMap<>();
        List<GroupTreeResolver.Group> ldapGroupsRep = new LinkedList<>();
        convertGroupsToInternalRep(ldapGroups, ldapGroupsMap, ldapGroupsRep);

        // 构建组树（若启用继承保留）
        if (config.isPreserveGroupsInheritance()) {
            try {
                List<GroupTreeResolver.GroupTreeEntry> groupTrees = new GroupTreeResolver().resolveGroupTree(ldapGroupsRep, config.isIgnoreMissingGroups());

                updateKeycloakGroupTree(realm, groupTrees, ldapGroupsMap, syncResult);
            } catch (GroupTreeResolver.GroupTreeResolveException gre) {
                throw new ModelException("Couldn't resolve groups from LDAP. Fix LDAP or skip preserve inheritance. Details: " + gre.getMessage(), gre);
            }
        } else {
            syncFlatGroupStructure(realm, syncResult, ldapGroupsMap);
        }

        syncFromLDAPPerformedInThisTransaction = true;

        return syncResult;
    }

    private void syncExistingGroup(RealmModel realm, GroupModel kcExistingGroup, Map.Entry<String, LDAPObject> groupEntry,
                                   SynchronizationResult syncResult, Set<String> visitedGroupIds, String groupName) {
        try {
            // 在独立内层事务中更新，避免并发删除导致竞态
            KeycloakModelUtils.runJobInTransaction(ldapProvider.getSession().getKeycloakSessionFactory(), session -> {
                RealmModel innerTransactionRealm = session.realms().getRealm(realm.getId());
                GroupModel innerTransactionGroup = session.groups().getGroupById(innerTransactionRealm, kcExistingGroup.getId());
                updateAttributesOfKCGroup(innerTransactionGroup, groupEntry.getValue());
                syncResult.increaseUpdated();
                visitedGroupIds.add(kcExistingGroup.getId());
            });

        } catch (ModelException me) {
            logger.error(String.format("Failed to update attributes of LDAP group %s: ", groupName), me);
            syncResult.increaseFailed();
        }
    }

    private void syncNonExistingGroup(RealmModel realm, Map.Entry<String, LDAPObject> groupEntry,
                                      SynchronizationResult syncResult, Set<String> visitedGroupIds, String groupName) {
        try {
            // 在独立内层事务中创建，避免并发创建导致竞态
            KeycloakModelUtils.runJobInTransaction(ldapProvider.getSession().getKeycloakSessionFactory(), session -> {
                RealmModel innerTransactionRealm = session.realms().getRealm(realm.getId());
                GroupModel kcGroup = createKcGroup(innerTransactionRealm, groupName, null);
                updateAttributesOfKCGroup(kcGroup, groupEntry.getValue());
                syncResult.increaseAdded();
                visitedGroupIds.add(kcGroup.getId());
            });
        } catch (ModelException me) {
            logger.error(String.format("Failed to sync group %s from LDAP: ", groupName), me);
            syncResult.increaseFailed();
        }
    }

    private void convertGroupsToInternalRep(List<LDAPObject> ldapGroups, Map<String, LDAPObject> ldapGroupsMap,
                                            List<GroupTreeResolver.Group> ldapGroupsRep) {
        String groupsRdnAttr = config.getGroupNameLdapAttribute();
        for (LDAPObject ldapGroup : ldapGroups) {
            String groupName = ldapGroup.getAttributeAsString(groupsRdnAttr);
            //String groupName = ldapGroup.getUuid();

            if (config.isPreserveGroupsInheritance()) {
                Set<String> subgroupNames = new HashSet<>();
                for (LDAPDn groupDn : getLDAPSubgroups(ldapGroup)) {
                    String subGroupName = groupDn.getFirstRdn().getAttrValue(groupsRdnAttr);
                    subgroupNames.add(subGroupName);
                }

                ldapGroupsRep.add(new GroupTreeResolver.Group(groupName, subgroupNames));
            }

            ldapGroupsMap.put(groupName, ldapGroup);
        }
    }

    private void syncFlatGroupStructure(RealmModel realm, SynchronizationResult syncResult, Map<String, LDAPObject> ldapGroupsMap) {
        Set<String> visitedGroupIds = new HashSet<>();

        // 扁平结构：所有组挂在 groups path 下
        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();
        final int groupsPerTransaction = ldapConfig.getBatchSizeForSync();
        Set<Map.Entry<String, LDAPObject>> entries = ldapGroupsMap.entrySet();
        for (Iterator<Map.Entry<String, LDAPObject>> it = entries.iterator(); it.hasNext(); ) {

            KeycloakModelUtils.runJobInTransaction(ldapProvider.getSession().getKeycloakSessionFactory(), session -> {

                // KEYCLOAK-8253：realm 获取 intentionally 放在 for 循环外，避免同步大量组时 realm 缓存膨胀
                RealmModel currentRealm = session.realms().getRealm(realm.getId());

                // 本事务内已知的组路径子组
                Map<String, GroupModel> transactionGroupPathGroups = getKcSubGroups(currentRealm, null)
                        .collect(Collectors.toMap(GroupModel::getName, Function.identity()));

                for (int i = 0; i < groupsPerTransaction && it.hasNext(); i++) {
                    Map.Entry<String, LDAPObject> groupEntry = it.next();

                    String groupName = groupEntry.getKey();
                    GroupModel kcExistingGroup = transactionGroupPathGroups.get(groupName);

                    if (kcExistingGroup != null) {
                        syncExistingGroup(currentRealm, kcExistingGroup, groupEntry, syncResult, visitedGroupIds, groupName);
                    } else {
                        syncNonExistingGroup(currentRealm, groupEntry, syncResult, visitedGroupIds, groupName);
                    }
                }
            });
        }

        // 可选：删除 LDAP 中已不存在的 Keycloak 组
        if (config.isDropNonExistingGroupsDuringSync()) {
            dropNonExistingKcGroups(realm, syncResult, visitedGroupIds);
        }
    }

    private void updateKeycloakGroupTree(RealmModel realm, List<GroupTreeResolver.GroupTreeEntry> groupTrees, Map<String, LDAPObject> ldapGroups, SynchronizationResult syncResult) {
        Set<String> visitedGroupIds = new HashSet<>();

        for (GroupTreeResolver.GroupTreeEntry groupEntry : groupTrees) {
            updateKeycloakGroupTreeEntry(realm, groupEntry, ldapGroups, null, syncResult, visitedGroupIds);
        }

        // 可选：删除 LDAP 中已不存在的 Keycloak 组
        if (config.isDropNonExistingGroupsDuringSync()) {
            dropNonExistingKcGroups(realm, syncResult, visitedGroupIds);
        }
    }

    private void updateKeycloakGroupTreeEntry(RealmModel realm, GroupTreeResolver.GroupTreeEntry groupTreeEntry, Map<String, LDAPObject> ldapGroups, GroupModel kcParent, SynchronizationResult syncResult, Set<String> visitedGroupIds) {
        String groupName = groupTreeEntry.getGroupName();

        // Check if group already exists
        GroupModel kcGroup = getKcSubGroups(realm, kcParent)
                .filter(g -> Objects.equals(g.getName(), groupName)).findFirst().orElse(null);

        if (kcGroup != null) {
            logger.debugf("Updated Keycloak group '%s' from LDAP", kcGroup.getName());
            updateAttributesOfKCGroup(kcGroup, ldapGroups.get(kcGroup.getName()));
            syncResult.increaseUpdated();
        } else {
            kcGroup = createKcGroup(realm, groupTreeEntry.getGroupName(), kcParent);
            if (kcGroup.getParent() == null) {
                logger.debugf("Imported top-level group '%s' from LDAP", kcGroup.getName());
            } else {
                logger.debugf("Imported group '%s' from LDAP as child of group '%s'", kcGroup.getName(), kcGroup.getParent().getName());
            }

            updateAttributesOfKCGroup(kcGroup, ldapGroups.get(kcGroup.getName()));
            syncResult.increaseAdded();
        }

        visitedGroupIds.add(kcGroup.getId());

        for (GroupTreeResolver.GroupTreeEntry childEntry : groupTreeEntry.getChildren()) {
            updateKeycloakGroupTreeEntry(realm, childEntry, ldapGroups, kcGroup, syncResult, visitedGroupIds);
        }
    }

    private void dropNonExistingKcGroups(RealmModel realm, SynchronizationResult syncResult, Set<String> visitedGroupIds) {
        // Remove keycloak groups, which don't exist in LDAP
        GroupModel parent = getKcGroupsPathGroup(realm);

        getAllKcGroups(realm, parent)
                .filter(kcGroup -> !visitedGroupIds.contains(kcGroup.getId()))
                .forEach(kcGroup -> {
                    logger.debugf("Removing Keycloak group '%s', which doesn't exist in LDAP", kcGroup.getName());
                    realm.removeGroup(kcGroup);
                    syncResult.increaseRemoved();
                });
    }

    /** 将 LDAP 组属性同步到 Keycloak 组。 */
    private void updateAttributesOfKCGroup(GroupModel kcGroup, LDAPObject ldapGroup) {
        Collection<String> groupAttributes = config.getGroupAttributes();
        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();

        for (String attrName : groupAttributes) {
            Set<String> attrValues = ldapGroup.getAttributeAsSet(attrName);
            if (attrValues == null) {
                kcGroup.removeAttribute(attrName);
            } else {
                if (config.isDecodeGroupUuidAttribute()
                        && attrName.equalsIgnoreCase(ldapConfig.getUuidLDAPAttributeName())) {
                    attrValues = attrValues.stream()
                            .map(v -> LDAPUtil.decodeBase64ToUuid(v, ldapConfig))
                            .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
                }
                kcGroup.setAttribute(attrName, new LinkedList<>(attrValues));
            }
        }
    }


    /** 按 LDAP 组查找对应的 Keycloak 组。 */
    protected GroupModel findKcGroupByLDAPGroup(RealmModel realm, GroupModel parent, LDAPObject ldapGroup) {
        String groupNameAttr = config.getGroupNameLdapAttribute();
        String groupName = ldapGroup.getAttributeAsString(groupNameAttr);

        if (config.isPreserveGroupsInheritance()) {
            // Override if better effectivity or different algorithm is needed
            return getAllKcGroups(realm, parent)
                    .filter(group -> Objects.equals(group.getName(), groupName)).findFirst().orElse(null);
        } else {
            // 未保留继承时，组始终在 groups path 下
            return getSession().groups().getGroupByName(realm, parent, groupName);
        }
    }

    /** 查找 Keycloak 组，不存在时按需从 LDAP 同步创建。 */
    protected GroupModel findKcGroupOrSyncFromLDAP(RealmModel realm, GroupModel parent, LDAPObject ldapGroup, UserModel user) {
        GroupModel kcGroup = findKcGroupByLDAPGroup(realm, parent, ldapGroup);

        if (kcGroup == null) {

            if (config.isPreserveGroupsInheritance()) {

                // 保留继承时，全量同步 LDAP 组树更可靠
                if (!syncFromLDAPPerformedInThisTransaction) {
                    syncDataFromFederationProviderToKeycloak(realm);
                    kcGroup = findKcGroupByLDAPGroup(realm, parent, ldapGroup);
                }
            } else {
                String groupNameAttr = config.getGroupNameLdapAttribute();
                String groupName = ldapGroup.getAttributeAsString(groupNameAttr);

                kcGroup = createKcGroup(realm, groupName, null);
                updateAttributesOfKCGroup(kcGroup, ldapGroup);
            }

            // memberOf 策略下，用户可能引用 LDAP 中已不存在的组
            if (kcGroup == null) {
                String groupName = ldapGroup.getAttributeAsString(config.getGroupNameLdapAttribute());
                logger.warnf("User '%s' is member of group '%s', which doesn't exist in LDAP", user.getUsername(), groupName);
            }
        }

        return kcGroup;
    }

    /** 发送 LDAP 查询获取全部组。 */
    protected List<LDAPObject> getAllLDAPGroups(boolean includeMemberAttribute) {
        try (LDAPQuery ldapGroupQuery = createGroupQuery(includeMemberAttribute)) {
            return LDAPUtils.loadAllLDAPObjects(ldapGroupQuery, ldapProvider);
        }
    }


    // 从 Keycloak 同步到 LDAP

    /** {@inheritDoc} 将 Keycloak 组结构同步到 LDAP（仅 LDAP_ONLY 模式）。 */
    @Override
    public SynchronizationResult syncDataFromKeycloakToFederationProvider(RealmModel realm) {
        SynchronizationResult syncResult = new SynchronizationResult() {

            @Override
            public String getStatus() {
                return String.format("%d groups imported to LDAP, %d groups updated to LDAP, %d groups removed from LDAP", getAdded(), getUpdated(), getRemoved());
            }

        };

        if (config.getMode() != LDAPGroupMapperMode.LDAP_ONLY) {
            logger.warnf("Ignored sync for federation mapper '%s' as it's mode is '%s'", mapperModel.getName(), config.getMode().toString());
            return syncResult;
        }

        logger.debugf("Syncing groups from Keycloak into LDAP. Mapper is [%s], LDAP provider is [%s]", mapperModel.getName(), ldapProvider.getModel().getName());

        // 查询现有 LDAP 组
        List<LDAPObject> ldapGroups = getAllLDAPGroups(config.isPreserveGroupsInheritance());

        // Convert them to Map<String, LDAPObject>
        Map<String, LDAPObject> ldapGroupsMap = new HashMap<>();
        String groupsRdnAttr = config.getGroupNameLdapAttribute();
        for (LDAPObject ldapGroup : ldapGroups) {
            String groupName = ldapGroup.getAttributeAsString(groupsRdnAttr);
            ldapGroupsMap.put(groupName, ldapGroup);
        }


        // 跟踪 LDAP 中仍存在于 Keycloak 的组名
        Set<String> ldapGroupNames = new HashSet<>();

        // 创建或更新 Keycloak 组到 LDAP（含属性）
        getKcSubGroups(realm, null)
                .forEach(kcGroup -> processKeycloakGroupSyncToLDAP(kcGroup, ldapGroupsMap, ldapGroupNames, syncResult));

        // dropNonExisting 时，删除 LDAP 中 Keycloak 已不存在的组
        if (config.isDropNonExistingGroupsDuringSync()) {
            Set<String> copy = new HashSet<>(ldapGroupsMap.keySet());
            for (String groupName : copy) {
                if (!ldapGroupNames.contains(groupName)) {
                    LDAPObject ldapGroup = ldapGroupsMap.remove(groupName);
                    ldapProvider.getLdapIdentityStore().remove(ldapGroup);
                    syncResult.increaseRemoved();
                }
            }
        }

        // 最后处理组成员关系（保留继承时）
        if (config.isPreserveGroupsInheritance()) {
            getKcSubGroups(realm, null)
                    .forEach(kcGroup -> processKeycloakGroupMembershipsSyncToLDAP(kcGroup, ldapGroupsMap));
        }

        return syncResult;
    }

    /** 递归同步 Keycloak 组到 LDAP（按名称匹配，创建或更新属性）。 */
    private void processKeycloakGroupSyncToLDAP(GroupModel kcGroup, Map<String, LDAPObject> ldapGroupsMap, Set<String> ldapGroupNames, SynchronizationResult syncResult) {
        String groupName = kcGroup.getName();

        // 提取待写入 LDAP 的组属性
        Map<String, Set<String>> supportedLdapAttributes = new HashMap<>();
        for (String attrName : config.getGroupAttributes()) {
            Set<String> valueSet = kcGroup.getAttributeStream(attrName).collect(Collectors.toSet());
            supportedLdapAttributes.put(attrName, valueSet.isEmpty() ? null : valueSet);
        }

        LDAPObject ldapGroup = ldapGroupsMap.get(groupName);

        if (ldapGroup == null) {
            ldapGroup = createLDAPGroup(groupName, supportedLdapAttributes);
            syncResult.increaseAdded();
        } else {
            for (Map.Entry<String, Set<String>> attrEntry : supportedLdapAttributes.entrySet()) {
                ldapGroup.setAttribute(attrEntry.getKey(), attrEntry.getValue());
            }

            ldapProvider.getLdapIdentityStore().update(ldapGroup);
            syncResult.increaseUpdated();
        }

        ldapGroupsMap.put(groupName, ldapGroup);
        ldapGroupNames.add(groupName);

        // process KC subgroups
        kcGroup.getSubGroupsStream()
                .forEach(kcSubgroup -> processKeycloakGroupSyncToLDAP(kcSubgroup, ldapGroupsMap, ldapGroupNames, syncResult));
    }

    /** 递归将 Keycloak 子组成员关系同步到 LDAP 父组的 member 属性。 */
    private void processKeycloakGroupMembershipsSyncToLDAP(GroupModel kcGroup, Map<String, LDAPObject> ldapGroupsMap) {
        LDAPObject ldapGroup = ldapGroupsMap.get(kcGroup.getName());
        Set<LDAPDn> toRemoveSubgroupsDNs = getLDAPSubgroups(ldapGroup);

        String membershipUserLdapAttrName = getMembershipUserLdapAttribute(); // 对组继承不适用，但接口需要

        // 将 Keycloak 子组添加为 LDAP 子组成员
        Set<GroupModel> kcSubgroups = kcGroup.getSubGroupsStream().collect(Collectors.toSet());
        for (GroupModel kcSubgroup : kcSubgroups) {
            LDAPObject ldapSubgroup = ldapGroupsMap.get(kcSubgroup.getName());
            if (!toRemoveSubgroupsDNs.remove(ldapSubgroup.getDn())) {
                // LDAP 组中尚无该子组 => 添加成员
                LDAPUtils.addMember(ldapProvider, MembershipType.DN, config.getMembershipLdapAttribute(), membershipUserLdapAttrName, ldapGroup, ldapSubgroup);
            }
        }

        // 移除 Keycloak 中已不再是子组的 LDAP 成员
        for (LDAPDn toRemoveDN : toRemoveSubgroupsDNs) {
            LDAPObject fakeGroup = new LDAPObject();
            fakeGroup.setDn(toRemoveDN);
            LDAPUtils.deleteMember(ldapProvider, MembershipType.DN, config.getMembershipLdapAttribute(), membershipUserLdapAttrName, ldapGroup, fakeGroup);
        }

        for (GroupModel kcSubgroup : kcSubgroups) {
            processKeycloakGroupMembershipsSyncToLDAP(kcSubgroup, ldapGroupsMap);
        }
    }

    /** 递归查找 LDAP 中尚不存在的最高祖先组，以便批量同步到 LDAP。 */
    private GroupModel getHighestPredecessorNotExistentInLdap(GroupModel groupsPathGroup, GroupModel group) {
        GroupModel parentGroup = group.getParent();
        if (parentGroup == groupsPathGroup) {
            return group;
        }

        LDAPObject ldapGroup = loadLDAPGroupByName(parentGroup.getName());
        if (ldapGroup != null) {
            // 父组已存在于 LDAP，返回当前组
            return group;
        } else {
            // 父组不存在，继续向上递归
            return getHighestPredecessorNotExistentInLdap(groupsPathGroup, parentGroup);
        }
    }


    // 组-用户成员关系操作


    /** {@inheritDoc} 返回 LDAP 组成员（IMPORT 模式返回空以避免重复）。 */
    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel kcGroup, int firstResult, int maxResults) {
        if (config.getMode() == LDAPGroupMapperMode.IMPORT) {
            // IMPORT 模式仅返回 Keycloak 结果，避免与 LDAP 导入项重复
            return Collections.emptyList();
        }

        if (!isGroupInGroupPath(realm, kcGroup)) {
            // 所查组不在本映射器管理的 groups path 下
            return Collections.emptyList();
        }

        // TODO：AD 范围搜索可进一步优化（暂未实现）
        LDAPObject ldapGroup = loadLDAPGroupByName(kcGroup.getName());
        if (ldapGroup == null) {
            return Collections.emptyList();
        }

        String strategyKey = config.getUserGroupsRetrieveStrategy();
        UserRolesRetrieveStrategy strategy = factory.getUserGroupsRetrieveStrategy(strategyKey);
        return strategy.getLDAPRoleMembers(realm, this, ldapGroup, firstResult, maxResults);
    }

    /** 在 LDAP 组中添加用户成员关系，必要时先同步 Keycloak 组到 LDAP。 */
    public void addGroupMappingInLDAP(RealmModel realm, GroupModel kcGroup, LDAPObject ldapUser) {
        String groupName = kcGroup.getName();
        LDAPObject ldapGroup = loadLDAPGroupByName(groupName);

        if (ldapGroup == null) {
            // 需要部分同步 Keycloak 组到 LDAP
            if (config.isPreserveGroupsInheritance()) {
                GroupModel groupsPathGroup = getKcGroupsPathGroup(realm);
                GroupModel highestGroupToSync = getHighestPredecessorNotExistentInLdap(groupsPathGroup, kcGroup);

                logger.debugf("Will sync group '%s' and it's subgroups from DB to LDAP", highestGroupToSync.getName());

                Map<String, LDAPObject> syncedLDAPGroups = new HashMap<>();
                processKeycloakGroupSyncToLDAP(highestGroupToSync, syncedLDAPGroups, new HashSet<>(), new SynchronizationResult());
                processKeycloakGroupMembershipsSyncToLDAP(highestGroupToSync, syncedLDAPGroups);

                ldapGroup = loadLDAPGroupByName(groupName);

                // 最后更新父组中的 LDAP 成员关系
                if (highestGroupToSync.getParent() != groupsPathGroup) {
                    LDAPObject ldapParentGroup = loadLDAPGroupByName(highestGroupToSync.getParent().getName());
                    LDAPUtils.addMember(ldapProvider, MembershipType.DN, config.getMembershipLdapAttribute(), getMembershipUserLdapAttribute(), ldapParentGroup, ldapGroup);
                }
            } else {
                // 不保留继承时，仅同步当前组
                logger.debugf("Will sync group '%s' from DB to LDAP", groupName);
                processKeycloakGroupSyncToLDAP(kcGroup, new HashMap<>(), new HashSet<>(), new SynchronizationResult());
                ldapGroup = loadLDAPGroupByName(groupName);
            }
        }

        String membershipUserLdapAttrName = getMembershipUserLdapAttribute();

        LDAPUtils.addMember(ldapProvider, config.getMembershipTypeLdapAttribute(), config.getMembershipLdapAttribute(), membershipUserLdapAttrName, ldapGroup, ldapUser);
    }

    /** 从 LDAP 组中移除用户成员关系。 */
    public void deleteGroupMappingInLDAP(LDAPObject ldapUser, LDAPObject ldapGroup) {
        String membershipUserLdapAttrName = getMembershipUserLdapAttribute();
        LDAPUtils.deleteMember(ldapProvider, config.getMembershipTypeLdapAttribute(), config.getMembershipLdapAttribute(), membershipUserLdapAttrName, ldapGroup, ldapUser);
    }

    /** 按配置策略获取用户的 LDAP 组映射。 */
    protected List<LDAPObject> getLDAPGroupMappings(LDAPObject ldapUser) {
        String strategyKey = config.getUserGroupsRetrieveStrategy();
        UserRolesRetrieveStrategy strategy = factory.getUserGroupsRetrieveStrategy(strategyKey);

        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();
        return strategy.getLDAPRoleMappings(this, ldapUser, ldapConfig);
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {
        String strategyKey = config.getUserGroupsRetrieveStrategy();
        UserRolesRetrieveStrategy strategy = factory.getUserGroupsRetrieveStrategy(strategyKey);
        strategy.beforeUserLDAPQuery(this, query);
    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        final LDAPGroupMapperMode mode = config.getMode();

        // IMPORT 模式下所有操作针对本地数据库
        if (mode == LDAPGroupMapperMode.IMPORT) {
            return delegate;
        } else {
            return new LDAPGroupMappingsUserDelegate(realm, delegate, ldapUser);
        }
    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        LDAPGroupMapperMode mode = config.getMode();

        // 目前仅在用户创建时从 LDAP 导入组映射
        if (mode == LDAPGroupMapperMode.IMPORT && isCreate) {

            List<LDAPObject> ldapGroups = getLDAPGroupMappings(ldapUser);
            if (!ldapGroups.isEmpty()) {
                GroupModel parent = getKcGroupsPathGroup(realm);
                // 从 LDAP 导入组成员关系到 Keycloak
                for (LDAPObject ldapGroup : ldapGroups) {

                    GroupModel kcGroup = findKcGroupOrSyncFromLDAP(realm, parent, ldapGroup, user);
                    if (kcGroup != null) {
                        logger.debugf("User '%s' joins group '%s' during import from LDAP", user.getUsername(), kcGroup.getName());
                        user.joinGroup(kcGroup);
                    }
                }
            }
        }
    }


    protected String getMembershipUserLdapAttribute() {
        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();
        return config.getMembershipUserLdapAttribute(ldapConfig);
    }


    /** 用户代理：合并 LDAP 与 Keycloak 组成员关系，并按模式处理 join/leave。 */
    public class LDAPGroupMappingsUserDelegate extends UserModelDelegate {

        private final RealmModel realm;
        private final LDAPObject ldapUser;

        // 避免同一请求内多次从 LDAP 加载组映射
        private Set<GroupModel> cachedLDAPGroupMappings;

        public LDAPGroupMappingsUserDelegate(RealmModel realm, UserModel user, LDAPObject ldapUser) {
            super(user);
            this.realm = realm;
            this.ldapUser = ldapUser;
        }

        @Override
        public boolean hasRole(RoleModel role) {
            return super.hasRole(role) || RoleUtils.hasRoleFromGroup(getGroupsStream(), role, true);
        }

        @Override
        public Stream<GroupModel> getGroupsStream() {
            Stream<GroupModel> ldapGroupMappings = getLDAPGroupMappingsConverted();
            if (config.isTopLevelGroupsPath() && config.getMode() == LDAPGroupMapperMode.LDAP_ONLY) {
                // 顶级 groups path 且 LDAP_ONLY：仅使用 LDAP 组映射
                return ldapGroupMappings;
            } else {
                // 合并 LDAP 与数据库映射（含其他映射器分配的组）
                return Stream.concat(ldapGroupMappings, super.getGroupsStream());
            }
        }

        @Override
        public void joinGroup(GroupModel group) {
            if (config.getMode() == LDAPGroupMapperMode.LDAP_ONLY && isGroupInGroupPath(realm, group)) {
                // LDAP_ONLY 且组在本映射器 path 下：在 LDAP 中创建成员关系
                cachedLDAPGroupMappings = null;
                addGroupMappingInLDAP(realm, group, ldapUser);
            } else {
                super.joinGroup(group);
            }
        }

        @Override
        public void leaveGroup(GroupModel group) {
            // 退出的组不由本映射器管理时，委托后续映射器或数据库
            if (!isGroupInGroupPath(realm, group)) {
                super.leaveGroup(group);
            }

            try (LDAPQuery ldapQuery = createGroupQuery(true)) {
                LDAPQueryConditionsBuilder conditionsBuilder = new LDAPQueryConditionsBuilder();
                Condition roleNameCondition = conditionsBuilder.equal(config.getGroupNameLdapAttribute(), group.getName());

                String membershipUserLdapAttrName = getMembershipUserLdapAttribute();
                String membershipUserAttr = LDAPUtils.getMemberValueOfChildObject(ldapUser, config.getMembershipTypeLdapAttribute(), membershipUserLdapAttrName);
                Condition membershipCondition = conditionsBuilder.equal(config.getMembershipLdapAttribute(), membershipUserAttr);

                ldapQuery.addWhereCondition(roleNameCondition).addWhereCondition(membershipCondition);
                LDAPObject ldapGroup = ldapQuery.getFirstResult();

                if (ldapGroup == null) {
                    // LDAP 中无此映射：LDAP_ONLY 无需操作；READ_ONLY 删除本地映射
                    if (config.getMode() == LDAPGroupMapperMode.READ_ONLY) {
                        super.leaveGroup(group);
                    }
                } else {
                    // LDAP 中存在映射：READ_ONLY 不可删；LDAP_ONLY 从 LDAP 删除
                    if (config.getMode() == LDAPGroupMapperMode.READ_ONLY) {
                        throw new ModelException("Not possible to delete LDAP group mappings as mapper mode is READ_ONLY");
                    } else {
                        // 删除 LDAP 组成员关系
                        cachedLDAPGroupMappings = null;
                        deleteGroupMappingInLDAP(ldapUser, ldapGroup);
                    }
                }
            }
        }

        @Override
        public boolean isMemberOf(GroupModel group) {
            if (!isGroupInGroupPath(realm, group)) {
                // 本映射器不管理该组，委托后续映射器或 JPA 存储
                return super.isMemberOf(group);
            }
            return RoleUtils.isDirectMember(getGroupsStream(),group);
        }

        protected Stream<GroupModel> getLDAPGroupMappingsConverted() {
            if (cachedLDAPGroupMappings != null) {
                return cachedLDAPGroupMappings.stream();
            }

            cachedLDAPGroupMappings = Set.of();

            List<LDAPObject> ldapGroups = getLDAPGroupMappings(ldapUser);
            if (!ldapGroups.isEmpty()) {
                GroupModel parent = getKcGroupsPathGroup(realm);

                cachedLDAPGroupMappings = ldapGroups.stream()
                        .map(ldapGroup -> findKcGroupOrSyncFromLDAP(realm, parent, ldapGroup, this))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                return cachedLDAPGroupMappings.stream();
            }
            return Stream.empty();
        }
    }

    // LDAP 组路径相关操作

    /**
     * 将 LDAP 组名转换为 groups path 下的 Keycloak 组路径。
     */
    protected String getKcGroupPathFromLDAPGroupName(String ldapGroupName) {
        return config.getGroupsPathWithTrailingSlash() + ldapGroupName;
    }

    /**
     * 返回配置的 groups path 对应 Keycloak 组；顶级路径时返回 null。
     */
    protected GroupModel getKcGroupsPathGroup(RealmModel realm) {
        return config.isTopLevelGroupsPath() ? null : KeycloakModelUtils.findGroupByPath(getSession(), realm, config.getGroupsPath());
    }

    /** 判断组是否位于本映射器配置的 groups path 下。 */
    protected boolean isGroupInGroupPath(RealmModel realm, GroupModel group) {
        if (group.getType() == GroupModel.Type.ORGANIZATION) {
            return false; // 组织内部组始终跳过
        }
        if (config.isTopLevelGroupsPath()) {
            return true; // 顶级路径下任意组均受管
        }
        GroupModel groupPathGroup = KeycloakModelUtils.findGroupByPath(getSession(), realm, config.getGroupsPath());
        if (groupPathGroup != null) {
            while(!groupPathGroup.getId().equals(group.getId())) {
                group = group.getParent();
                if (group == null) {
                    return false; // 已遍历所有祖先，均不匹配 groups path
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 在指定父组或 groups path 下，根据 LDAP 组名创建 Keycloak 组。
     */
    protected GroupModel createKcGroup(RealmModel realm, String ldapGroupName, GroupModel parentGroup) {

        // 未指定父组时使用 groups path
        if (parentGroup == null) {
            parentGroup = getKcGroupsPathGroup(realm);
        }
        return realm.createGroup(ldapGroupName, parentGroup);
    }

    /**
     * 返回指定父组（或 groups path）下的 Keycloak 直接子组流。
     */
    protected Stream<GroupModel> getKcSubGroups(RealmModel realm, GroupModel parentGroup) {

        // 未指定父组时使用 groups path
        if (parentGroup == null) {
            parentGroup = getKcGroupsPathGroup(realm);
        }
        return parentGroup == null ? getSession().groups().getTopLevelGroupsStream(realm) :
            parentGroup.getSubGroupsStream();
    }

    /**
     * 返回 groups path 配置下所有 Keycloak 组（含子组）的流。
     */
    protected Stream<GroupModel> getAllKcGroups(RealmModel realm, GroupModel topParentGroup) {
        Stream<GroupModel> allGroups = realm.getGroupsStream();
        if (topParentGroup == null) return allGroups;

        return allGroups.filter(group -> {
            // 检查是否为 topParentGroup（Groups Path）的后代
            GroupModel parent = group.getParent();
            while (parent != null) {
                if (parent.getId().equals(topParentGroup.getId())) {
                    return true;
                }
                parent = parent.getParent();
            }
            return false;
        });
    }
}
