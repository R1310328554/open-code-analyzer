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

import org.keycloak.common.util.ObjectUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.membership.CommonLDAPGroupMapperConfig;

/**
 * LDAP 组映射器配置：读取并解析 {@link GroupLDAPStorageMapper} 的组件配置项。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GroupMapperConfig extends CommonLDAPGroupMapperConfig {

    /** 保存该组树的 LDAP DN。 */
    public static final String GROUPS_DN = "groups.dn";
    /** 相对于组 DN 的创建路径前缀。 */
    public static final String GROUPS_RELATIVE_CREATE_DN = "groups.relative.create.dn";

    /** 组对象中用作名称与 RDN 的 LDAP 属性，通常为 cn。 */
    public static final String GROUP_NAME_LDAP_ATTRIBUTE = "group.name.ldap.attribute";

    /** 组对象的 objectClass 列表。 */
    public static final String GROUP_OBJECT_CLASSES = "group.object.classes";

    /** 是否将 LDAP 组继承关系同步到 Keycloak 组层次结构。 */
    public static final String PRESERVE_GROUP_INHERITANCE = "preserve.group.inheritance";

    /** 是否忽略 LDAP 中引用但不存在的缺失组。 */
    public static final String IGNORE_MISSING_GROUPS = "ignore.missing.groups";

    /** 附加到 LDAP 组查询的自定义过滤器。 */
    public static final String GROUPS_LDAP_FILTER = "groups.ldap.filter";

    /** 从 LDAP 组对象映射到 Keycloak 组属性的 LDAP 属性名列表。 */
    public static final String MAPPED_GROUP_ATTRIBUTES = "mapped.group.attributes";

    /** 是否将 UUID 类 LDAP 属性（如 objectGUID）解码为 UUID 格式而非保留 base64。 */
    public static final String DECODE_GROUP_UUID_ATTRIBUTE = "decode.group.uuid.attribute";

    /** 从 LDAP 同步到 Keycloak 时，删除 LDAP 中已不存在的 Keycloak 组。 */
    public static final String DROP_NON_EXISTING_GROUPS_DURING_SYNC = "drop.non.existing.groups.during.sync";

    /** 参见 {@link org.keycloak.storage.ldap.mappers.membership.UserRolesRetrieveStrategy}。 */
    public static final String LOAD_GROUPS_BY_MEMBER_ATTRIBUTE = "LOAD_GROUPS_BY_MEMBER_ATTRIBUTE";
    public static final String GET_GROUPS_FROM_USER_MEMBEROF_ATTRIBUTE = "GET_GROUPS_FROM_USER_MEMBEROF_ATTRIBUTE";
    public static final String LOAD_GROUPS_BY_MEMBER_ATTRIBUTE_RECURSIVELY = "LOAD_GROUPS_BY_MEMBER_ATTRIBUTE_RECURSIVELY";

    /** LDAP 组挂载到 Keycloak 的路径，默认为顶层 "/"。 */
    public static final String LDAP_GROUPS_PATH = "groups.path";
    public static final String DEFAULT_LDAP_GROUPS_PATH = "/";

    public GroupMapperConfig(ComponentModel mapperModel) {
        super(mapperModel);
    }

    /** 返回组树的 LDAP DN，未配置时抛出 {@link ModelException}。 */
    public String getGroupsDn() {
        String groupsDn = mapperModel.getConfig().getFirst(GROUPS_DN);
        if (groupsDn == null) {
            throw new ModelException("Groups DN is null! Check your configuration");
        }
        return groupsDn;
    }

    /** 返回创建组时使用的相对 DN 前缀，末尾保证带逗号。 */
    public String getRelativeCreateDn() {
        String relativeCreateDn = mapperModel.getConfig().getFirst(GROUPS_RELATIVE_CREATE_DN);
        if(relativeCreateDn != null) {
            relativeCreateDn = relativeCreateDn.trim();
            return relativeCreateDn.endsWith(",") ? relativeCreateDn : relativeCreateDn + ",";
        }
        return "";
    }

    @Override
    public String getLDAPGroupsDn() {
        return getGroupsDn();
    }

    /** 返回组名称对应的 LDAP 属性名，默认 cn。 */
    public String getGroupNameLdapAttribute() {
        String rolesRdnAttr = mapperModel.getConfig().getFirst(GROUP_NAME_LDAP_ATTRIBUTE);
        return rolesRdnAttr!=null ? rolesRdnAttr : LDAPConstants.CN;
    }

    @Override
    public String getLDAPGroupNameLdapAttribute() {
        return getGroupNameLdapAttribute();
    }

    /** 是否保留 LDAP 组继承关系。 */
    public boolean isPreserveGroupsInheritance() {
        return AbstractLDAPStorageMapper.parseBooleanParameter(mapperModel, PRESERVE_GROUP_INHERITANCE);
    }

    /** 是否忽略缺失的组引用。 */
    public boolean isIgnoreMissingGroups() {
        return AbstractLDAPStorageMapper.parseBooleanParameter(mapperModel, IGNORE_MISSING_GROUPS);
    }

    /** 返回组对象的 objectClass 集合；Active Directory 默认为 group，其他服务器默认为 groupOfNames。 */
    public Collection<String> getGroupObjectClasses(LDAPStorageProvider ldapProvider) {
        String objectClasses = mapperModel.getConfig().getFirst(GROUP_OBJECT_CLASSES);
        if (objectClasses == null) {
            // Active Directory 默认为 group，其他 LDAP 服务器默认为 groupOfNames
            objectClasses = ldapProvider.getLdapIdentityStore().getConfig().isActiveDirectory() ? LDAPConstants.GROUP : LDAPConstants.GROUP_OF_NAMES;
        }

        return getConfigValues(objectClasses);
    }

    /** 返回需要映射到 Keycloak 组的 LDAP 属性名集合。 */
    public Collection<String> getGroupAttributes() {
        String groupAttrs = mapperModel.getConfig().getFirst(MAPPED_GROUP_ATTRIBUTES);
        return (groupAttrs == null) ? Collections.<String>emptySet() : getConfigValues(groupAttrs);
    }

    /** 返回自定义 LDAP 过滤器。 */
    public String getCustomLdapFilter() {
        return mapperModel.getConfig().getFirst(GROUPS_LDAP_FILTER);
    }

    /** 是否解码组 UUID 属性。 */
    public boolean isDecodeGroupUuidAttribute() {
        return AbstractLDAPStorageMapper.parseBooleanParameter(mapperModel, DECODE_GROUP_UUID_ATTRIBUTE);
    }

    /** 同步时是否删除 LDAP 中已不存在的 Keycloak 组。 */
    public boolean isDropNonExistingGroupsDuringSync() {
        return AbstractLDAPStorageMapper.parseBooleanParameter(mapperModel, DROP_NON_EXISTING_GROUPS_DURING_SYNC);
    }

    /** 返回用户组检索策略键，默认按 member 属性加载。 */
    public String getUserGroupsRetrieveStrategy() {
        String strategyString = mapperModel.getConfig().getFirst(USER_ROLES_RETRIEVE_STRATEGY);
        return strategyString!=null ? strategyString : LOAD_GROUPS_BY_MEMBER_ATTRIBUTE;
    }

    /** 返回 LDAP 组在 Keycloak 中的挂载路径。 */
    public String getGroupsPath() {
        String groupsPath = mapperModel.getConfig().getFirst(LDAP_GROUPS_PATH);
        return ObjectUtil.isBlank(groupsPath) ? DEFAULT_LDAP_GROUPS_PATH : groupsPath.trim();
    }

    /** 返回以斜杠结尾的组路径。 */
    public String getGroupsPathWithTrailingSlash() {
        String path = getGroupsPath();
        while (!path.endsWith("/")) {
            path = getGroupsPath() + "/";
        }
        return path;
    }

    /** 是否为顶层组路径 "/"。 */
    public boolean isTopLevelGroupsPath() {
        return "/".equals(getGroupsPath());
    }
}
