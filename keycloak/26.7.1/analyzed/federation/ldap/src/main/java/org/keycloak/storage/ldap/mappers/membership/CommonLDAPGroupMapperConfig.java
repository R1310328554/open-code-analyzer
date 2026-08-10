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

import java.util.HashSet;
import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.storage.ldap.LDAPConfig;

/**
 * LDAP 组/角色映射器通用配置：成员属性、成员类型、同步模式及检索策略等。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class CommonLDAPGroupMapperConfig {

    // 组对象上用于成员关系的 LDAP 属性名，通常为 "member"
    public static final String MEMBERSHIP_LDAP_ATTRIBUTE = "membership.ldap.attribute";

    // 参见 {@link MembershipType} 枚举说明
    public static final String MEMBERSHIP_ATTRIBUTE_TYPE = "membership.attribute.type";

    // 仅 membershipType=UID 时使用：用户对象上的成员标识属性，通常为 "uid"
    public static final String MEMBERSHIP_USER_LDAP_ATTRIBUTE = "membership.user.ldap.attribute";

    // 参见 {@link LDAPGroupMapperMode} 枚举说明
    public static final String MODE = "mode";

    // 参见 {@link UserRolesRetrieveStrategy} 枚举说明
    public static final String USER_ROLES_RETRIEVE_STRATEGY = "user.roles.retrieve.strategy";

    // 仅 UserRolesRetrieveStrategy.GetRolesFromUserMemberOfAttribute 时使用：用户对象上的 memberOf 类属性，通常为 "memberof"
    public static final String MEMBEROF_LDAP_ATTRIBUTE = "memberof.ldap.attribute";


    protected final ComponentModel mapperModel;

    /** 绑定映射器组件模型。 */
    public CommonLDAPGroupMapperConfig(ComponentModel mapperModel) {
        this.mapperModel = mapperModel;
    }

    /** 成员关系 LDAP 属性名，默认 {@link LDAPConstants#MEMBER}。 */
    public String getMembershipLdapAttribute() {
        String membershipAttrName = mapperModel.getConfig().getFirst(MEMBERSHIP_LDAP_ATTRIBUTE);
        return membershipAttrName!=null ? membershipAttrName : LDAPConstants.MEMBER;
    }

    /** 成员值类型（DN 或 UID）。 */
    public MembershipType getMembershipTypeLdapAttribute() {
        String membershipType = mapperModel.getConfig().getFirst(MEMBERSHIP_ATTRIBUTE_TYPE);
        return (membershipType!=null && !membershipType.isEmpty()) ? Enum.valueOf(MembershipType.class, membershipType) : MembershipType.DN;
    }

    /** UID 模式下用户对象上的成员标识属性。 */
    public String getMembershipUserLdapAttribute(LDAPConfig ldapConfig) {
        String membershipUserAttrName = mapperModel.getConfig().getFirst(MEMBERSHIP_USER_LDAP_ATTRIBUTE);
        return membershipUserAttrName!=null ? membershipUserAttrName : ldapConfig.getUsernameLdapAttribute();
    }

    /** 用户对象上的 memberOf 类属性名，默认 {@link LDAPConstants#MEMBER_OF}。 */
    public String getMemberOfLdapAttribute() {
        String memberOfLdapAttrName = mapperModel.getConfig().getFirst(MEMBEROF_LDAP_ATTRIBUTE);
        return memberOfLdapAttrName!=null ? memberOfLdapAttrName : LDAPConstants.MEMBER_OF;
    }

    /** 映射器同步模式（LDAP_ONLY / IMPORT / READ_ONLY）。 */
    public LDAPGroupMapperMode getMode() {
        String modeString = mapperModel.getConfig().getFirst(MODE);
        if (modeString == null || modeString.isEmpty()) {
            throw new ModelException("Mode is missing! Check your configuration");
        }

        return Enum.valueOf(LDAPGroupMapperMode.class, modeString.toUpperCase());
    }

    /** 将逗号分隔字符串解析为去重后的集合。 */
    protected Set<String> getConfigValues(String str) {
        String[] objClasses = str.split(",");
        Set<String> trimmed = new HashSet<>();
        for (String objectClass : objClasses) {
            objectClass = objectClass.trim();
            if (objectClass.length() > 0) {
                trimmed.add(objectClass);
            }
        }
        return trimmed;
    }

    /** LDAP 组/角色树的 DN 根。 */
    public abstract String getLDAPGroupsDn();

    /** 组/角色对象上用作名称与 RDN 的 LDAP 属性。 */
    public abstract String getLDAPGroupNameLdapAttribute();


}
