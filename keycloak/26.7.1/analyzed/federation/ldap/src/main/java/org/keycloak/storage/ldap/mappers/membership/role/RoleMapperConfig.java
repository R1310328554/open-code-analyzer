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

import org.keycloak.component.ComponentModel;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.membership.CommonLDAPGroupMapperConfig;

/**
 * LDAP 角色映射器配置：读取并解析 {@link RoleLDAPStorageMapper} 的组件配置项。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RoleMapperConfig extends CommonLDAPGroupMapperConfig {

    /** 保存该角色树的 LDAP DN。 */
    public static final String ROLES_DN = "roles.dn";
    /** 相对于角色 DN 的创建路径前缀。 */
    public static final String ROLES_RELATIVE_CREATE_DN = "roles.relative.create.dn";

    /** 角色对象中用作名称与 RDN 的 LDAP 属性，通常为 cn。 */
    public static final String ROLE_NAME_LDAP_ATTRIBUTE = "role.name.ldap.attribute";

    /** 角色对象的 objectClass 列表。 */
    public static final String ROLE_OBJECT_CLASSES = "role.object.classes";

    /** 为 true 时映射到领域角色；为 false 时映射到 {@link #CLIENT_ID} 指定的客户端角色。 */
    public static final String USE_REALM_ROLES_MAPPING = "use.realm.roles.mapping";

    /** 客户端 ID，仅在 {@link #USE_REALM_ROLES_MAPPING} 为 false 时生效。 */
    public static final String CLIENT_ID = "client.id";

    /** 附加到 LDAP 角色查询的自定义过滤器。 */
    public static final String ROLES_LDAP_FILTER = "roles.ldap.filter";

    /** 参见 {@link org.keycloak.storage.ldap.mappers.membership.UserRolesRetrieveStrategy}。 */
    public static final String LOAD_ROLES_BY_MEMBER_ATTRIBUTE = "LOAD_ROLES_BY_MEMBER_ATTRIBUTE";
    public static final String GET_ROLES_FROM_USER_MEMBEROF_ATTRIBUTE = "GET_ROLES_FROM_USER_MEMBEROF_ATTRIBUTE";
    public static final String LOAD_ROLES_BY_MEMBER_ATTRIBUTE_RECURSIVELY = "LOAD_ROLES_BY_MEMBER_ATTRIBUTE_RECURSIVELY";


    public RoleMapperConfig(ComponentModel mapperModel) {
        super(mapperModel);
    }

    /** 返回角色树的 LDAP DN，未配置时抛出 {@link ModelException}。 */
    public String getRolesDn() {
        String rolesDn = mapperModel.getConfig().getFirst(ROLES_DN);
        if (rolesDn == null) {
            throw new ModelException("Roles DN is null! Check your configuration");
        }
        return rolesDn;
    }

    /** 返回创建角色时使用的相对 DN 前缀，末尾保证带逗号。 */
    public String getRelativeCreateDn() {
        String relativeCreateDn = mapperModel.getConfig().getFirst(ROLES_RELATIVE_CREATE_DN);
        if(relativeCreateDn != null) {
            relativeCreateDn = relativeCreateDn.trim();
            return relativeCreateDn.endsWith(",") ? relativeCreateDn : relativeCreateDn + ",";
        }
        return "";
    }

    @Override
    public String getLDAPGroupsDn() {
        return getRolesDn();
    }

    /** 返回角色名称对应的 LDAP 属性名，默认 cn。 */
    public String getRoleNameLdapAttribute() {
        String rolesRdnAttr = mapperModel.getConfig().getFirst(ROLE_NAME_LDAP_ATTRIBUTE);
        return rolesRdnAttr!=null ? rolesRdnAttr : LDAPConstants.CN;
    }

    @Override
    public String getLDAPGroupNameLdapAttribute() {
        return getRoleNameLdapAttribute();
    }

    /** 返回角色对象的 objectClass 集合；Active Directory 默认为 group，其他服务器默认为 groupOfNames。 */
    public Collection<String> getRoleObjectClasses(LDAPStorageProvider ldapProvider) {
        String objectClasses = mapperModel.getConfig().getFirst(ROLE_OBJECT_CLASSES);
        if (objectClasses == null) {
            // Active Directory 默认为 group，其他 LDAP 服务器默认为 groupOfNames
            objectClasses = ldapProvider.getLdapIdentityStore().getConfig().isActiveDirectory() ? LDAPConstants.GROUP : LDAPConstants.GROUP_OF_NAMES;
        }

        return getConfigValues(objectClasses);
    }

    /** 返回自定义 LDAP 过滤器。 */
    public String getCustomLdapFilter() {
        return mapperModel.getConfig().getFirst(ROLES_LDAP_FILTER);
    }

    /** 是否映射到领域角色（默认 true）。 */
    public boolean isRealmRolesMapping() {
        String realmRolesMapping = mapperModel.getConfig().getFirst(USE_REALM_ROLES_MAPPING);
        return realmRolesMapping==null || Boolean.parseBoolean(realmRolesMapping);
    }

    /** 返回目标客户端 ID（客户端角色映射时使用）。 */
    public String getClientId() {
        return mapperModel.getConfig().getFirst(CLIENT_ID);
    }


    /** 返回用户角色检索策略键，默认按 member 属性加载。 */
    public String getUserRolesRetrieveStrategy() {
        String strategyString = mapperModel.getConfig().getFirst(USER_ROLES_RETRIEVE_STRATEGY);
        return strategyString!=null ? strategyString : LOAD_ROLES_BY_MEMBER_ATTRIBUTE;
    }

}
