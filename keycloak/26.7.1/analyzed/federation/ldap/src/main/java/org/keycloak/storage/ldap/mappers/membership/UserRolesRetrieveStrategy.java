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

package org.keycloak.storage.ldap.mappers.membership;


import java.util.List;
import java.util.Set;

import org.keycloak.models.LDAPConstants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.LDAPUtils;
import org.keycloak.storage.ldap.idm.model.LDAPDn;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.utils.StreamsUtil;

/**
 * 用户 LDAP 角色/组检索策略：定义如何从目录获取用户的组成员关系。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface UserRolesRetrieveStrategy {


    /** 获取指定 LDAP 用户所属的角色/组 LDAP 对象列表。 */
    List<LDAPObject> getLDAPRoleMappings(CommonLDAPGroupMapper roleOrGroupMapper, LDAPObject ldapUser, LDAPConfig ldapConfig);

    /** 分页返回属于指定 LDAP 角色/组的用户。 */
    List<UserModel> getLDAPRoleMembers(RealmModel realm, CommonLDAPGroupMapper roleOrGroupMapper, LDAPObject ldapRoleOrGroup, int firstResult, int maxResults);

    /** 用户 LDAP 查询执行前的预处理（例如追加 memberOf 返回属性）。 */
    void beforeUserLDAPQuery(CommonLDAPGroupMapper roleOrGroupMapper, LDAPQuery query);


    // 实现子类

    /**
     * 通过 LDAP 查询检索 member 属性包含当前用户的所有角色/组。
     */
    class LoadRolesByMember implements UserRolesRetrieveStrategy {

        @Override
        public List<LDAPObject> getLDAPRoleMappings(CommonLDAPGroupMapper roleOrGroupMapper, LDAPObject ldapUser, LDAPConfig ldapConfig) {
            try (LDAPQuery ldapQuery = roleOrGroupMapper.createLDAPGroupQuery()) {
                String membershipAttr = roleOrGroupMapper.getConfig().getMembershipLdapAttribute();

                String membershipUserAttrName = roleOrGroupMapper.getConfig().getMembershipUserLdapAttribute(ldapConfig);
                String userMembership = LDAPUtils.getMemberValueOfChildObject(ldapUser, roleOrGroupMapper.getConfig().getMembershipTypeLdapAttribute(), membershipUserAttrName);

                Condition membershipCondition = getMembershipCondition(membershipAttr, userMembership);
                ldapQuery.addWhereCondition(membershipCondition);

                return LDAPUtils.loadAllLDAPObjects(ldapQuery, ldapConfig);
            }
        }

        @Override
        public List<UserModel> getLDAPRoleMembers(RealmModel realm, CommonLDAPGroupMapper roleOrGroupMapper, LDAPObject ldapRoleOrGroup, int firstResult, int maxResults) {
            MembershipType membershipType = roleOrGroupMapper.getConfig().getMembershipTypeLdapAttribute();
            return membershipType.getGroupMembers(realm, roleOrGroupMapper, ldapRoleOrGroup, firstResult, maxResults);
        }

        @Override
        public void beforeUserLDAPQuery(CommonLDAPGroupMapper roleOrGroupMapper, LDAPQuery query) {
        }

        protected Condition getMembershipCondition(String membershipAttr, String userMembership) {
            return new LDAPQueryConditionsBuilder().equal(membershipAttr, userMembership);
        }

    };

    /**
     * 从 LDAP 用户对象的 memberOf 属性加载其所属角色/组。
     */
    class GetRolesFromUserMemberOfAttribute implements UserRolesRetrieveStrategy {

        @Override
        public List<LDAPObject> getLDAPRoleMappings(CommonLDAPGroupMapper roleOrGroupMapper, LDAPObject ldapUser, LDAPConfig ldapConfig) {
            try (LDAPQuery ldapQuery = roleOrGroupMapper.createLDAPGroupQuery()) {
                CommonLDAPGroupMapperConfig config = roleOrGroupMapper.getConfig();
                String rdnAttr = config.getLDAPGroupNameLdapAttribute();
                LDAPQueryConditionsBuilder conditionBuilder = new LDAPQueryConditionsBuilder();
                Set<String> memberOfValues = ldapUser.getAttributeAsSetOrDefault(config.getMemberOfLdapAttribute(), Set.of());
                // 仅加载 memberOf 中位于组/角色基 DN 下的条目，并应用自定义过滤器
                Condition[] conditions = memberOfValues.stream()
                        .map(LDAPDn::fromString)
                        .filter(roleDN -> roleDN.isDescendantOf(LDAPDn.fromString(config.getLDAPGroupsDn())))
                        .map(roleDN -> conditionBuilder.equal(rdnAttr, roleDN.getFirstRdn().getAttrValue(rdnAttr)))
                        .toArray(Condition[]::new);

                if (conditions.length == 0) {
                    // 预过滤后无有效 memberOf 值，无需查询
                    return List.of();
                }

                ldapQuery.addWhereCondition(conditionBuilder.orCondition(conditions));

                return LDAPUtils.loadAllLDAPObjects(ldapQuery, ldapConfig);
            }
        }

        @Override
        public List<UserModel> getLDAPRoleMembers(RealmModel realm, CommonLDAPGroupMapper roleOrGroupMapper, LDAPObject ldapRoleOrGroup, int firstResult, int maxResults) {
            String memberOfLdapAttrName = roleOrGroupMapper.getConfig().getMemberOfLdapAttribute();
            String roleOrGroupDn = ldapRoleOrGroup.getDn().toString();
            return StreamsUtil.paginatedStream(
                    roleOrGroupMapper.getLdapProvider().searchForUserByUserAttributeStream(realm, memberOfLdapAttrName, roleOrGroupDn), firstResult, maxResults)
                    .toList();
        }

        @Override
        public void beforeUserLDAPQuery(CommonLDAPGroupMapper roleOrGroupMapper, LDAPQuery query) {
            String memberOfLdapAttrName = roleOrGroupMapper.getConfig().getMemberOfLdapAttribute();

            query.addReturningLdapAttribute(memberOfLdapAttrName);
            query.addReturningReadOnlyLdapAttribute(memberOfLdapAttrName);
        }

    };

    /**
     * Active Directory 扩展：使用 {@link LDAPConstants#LDAP_MATCHING_RULE_IN_CHAIN} 递归检索嵌套组成员关系。
     */
    class LoadRolesByMemberRecursively extends LoadRolesByMember {

        @Override
        protected Condition getMembershipCondition(String membershipAttr, String userMembership) {
            return new LDAPQueryConditionsBuilder().equal(membershipAttr + LDAPConstants.LDAP_MATCHING_RULE_IN_CHAIN, userMembership);
        }

        @Override
        public List<UserModel> getLDAPRoleMembers(RealmModel realm, CommonLDAPGroupMapper roleOrGroupMapper, LDAPObject ldapRoleOrGroup, int firstResult, int maxResults) {
            String memberOfLdapAttrName = roleOrGroupMapper.getConfig().getMemberOfLdapAttribute();
            String roleOrGroupDn = ldapRoleOrGroup.getDn().toString();
            return StreamsUtil.paginatedStream(
                    roleOrGroupMapper.getLdapProvider().searchForUserByUserAttributeStream(realm, memberOfLdapAttrName + LDAPConstants.LDAP_MATCHING_RULE_IN_CHAIN, roleOrGroupDn), firstResult, maxResults)
                    .toList();
        }

    };

}
