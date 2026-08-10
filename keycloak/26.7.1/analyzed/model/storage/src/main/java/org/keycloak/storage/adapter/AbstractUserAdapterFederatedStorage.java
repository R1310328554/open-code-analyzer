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
package org.keycloak.storage.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModelDefaultMethods;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageUtil;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

/**
 * 基于联邦存储的用户适配器抽象基类：除用户名外，其余数据均委托 {@link UserFederatedStorageProvider} 持久化。
 * <p>
 * {@link #getId()} 默认 {@code "f:" + providerId + ":" + getUsername()}；
 * enabled、firstName、lastName、email 等 {@link UserModel} 属性以联邦存储中的自定义属性形式保存。
 * <p>
 * 若联邦存储未设置 {@link #ENABLED_ATTRIBUTE}，{@link #isEnabled()} 默认为 {@code true}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractUserAdapterFederatedStorage extends UserModelDefaultMethods {
    /** 联邦存储中 firstName 对应的属性键。 */
    public static String FIRST_NAME_ATTRIBUTE = "FIRST_NAME";
    /** 联邦存储中 lastName 对应的属性键。 */
    public static String LAST_NAME_ATTRIBUTE = "LAST_NAME";
    /** 联邦存储中 email 对应的属性键。 */
    public static String EMAIL_ATTRIBUTE = "EMAIL";
    /** 联邦存储中邮箱已验证标志对应的属性键。 */
    public static String EMAIL_VERIFIED_ATTRIBUTE = "EMAIL_VERIFIED";
    /** 联邦存储中创建时间戳对应的属性键。 */
    public static String CREATED_TIMESTAMP_ATTRIBUTE = "CREATED_TIMESTAMP";
    /** 联邦存储中启用状态对应的属性键。 */
    public static String ENABLED_ATTRIBUTE = "ENABLED";


    /** 当前 Keycloak 会话。 */
    protected KeycloakSession session;
    /** 用户所属 realm。 */
    protected RealmModel realm;
    /** 用户存储 Provider 的组件配置模型。 */
    protected ComponentModel storageProviderModel;

    /**
     * 构造基于联邦存储的用户适配器。
     *
     * @param session               当前会话
     * @param realm                 所属 realm
     * @param storageProviderModel  用户存储 Provider 组件模型
     */
    public AbstractUserAdapterFederatedStorage(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel) {
        this.session = session;
        this.realm = realm;
        this.storageProviderModel = storageProviderModel;
    }

    /**
     * 获取当前会话的 {@link UserFederatedStorageProvider}。
     *
     * @return 联邦存储 Provider 实例
     */
    public UserFederatedStorageProvider getFederatedStorage() {
        return UserStorageUtil.userFederatedStorage(session);
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return getFederatedStorage().getRequiredActionsStream(realm, this.getId());
    }

    @Override
    public void addRequiredAction(String action) {
        getFederatedStorage().addRequiredAction(realm, this.getId(), action);

    }

    @Override
    public void removeRequiredAction(String action) {
        getFederatedStorage().removeRequiredAction(realm, this.getId(), action);
    }

    @Override
    public void addRequiredAction(RequiredAction action) {
        getFederatedStorage().addRequiredAction(realm, this.getId(), action.name());

    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        getFederatedStorage().removeRequiredAction(realm, this.getId(), action.name());
    }

    /**
     * 获取由本存储 Provider 直接管理的组成员关系。
     *
     * @return Provider 管理的组集合，默认空集
     */
    protected Set<GroupModel> getGroupsInternal() {
        return Collections.emptySet();
    }

    /**
     * 是否在组列表中追加 realm 默认组。
     * <p>
     * 若存储 Provider 不管理组映射，建议返回 {@code true}。
     *
     * @return 追加默认组返回 {@code true}
     */
    protected boolean appendDefaultGroups() {
        return true;
    }

    /**
     * 从联邦存储读取组，并追加 realm 默认组及 {@link #getGroupsInternal()} 结果。
     */
    @Override
    public Stream<GroupModel> getGroupsStream() {
        Stream<GroupModel> groups = getFederatedStorage().getGroupsStream(realm, this.getId());
        if (appendDefaultGroups()) groups = Stream.concat(groups, realm.getDefaultGroupsStream());
        return Stream.concat(groups, getGroupsInternal().stream());
    }

    @Override
    public void joinGroup(GroupModel group) {
        getFederatedStorage().joinGroup(realm, this.getId(), group);

    }

    @Override
    public void leaveGroup(GroupModel group) {
        getFederatedStorage().leaveGroup(realm, this.getId(), group);

    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        return RoleUtils.isMember(getGroupsStream(), group);
    }

    /**
     * 从联邦存储读取 realm 角色映射（基于 {@link #getRoleMappingsStream()} 过滤）。
     */
    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return this.getRoleMappingsStream().filter(RoleUtils::isRealmRole);
    }

    /**
     * 从联邦存储读取指定客户端的角色映射。
     */
    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
        return getRoleMappingsStream().filter(r -> RoleUtils.isClientRole(r, app));
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return RoleUtils.hasRole(getRoleMappingsStream(), role)
          || RoleUtils.hasRoleFromGroup(getGroupsStream(), role, true);
    }

    @Override
    public void grantRole(RoleModel role) {
        if (hasDirectRole(role)) return;
        getFederatedStorage().grantRole(realm, this.getId(), role);
    }

    /**
     * 是否在角色映射中追加 realm 默认角色。
     * <p>
     * 若存储 Provider 不管理全部角色映射，建议返回 {@code true}。
     *
     * @return 追加默认角色返回 {@code true}
     */
    protected boolean appendDefaultRolesToRoleMappings() {
        return true;
    }

    /** 获取由本存储 Provider 直接管理的角色映射，默认空集。 */
    protected Set<RoleModel> getRoleMappingsInternal() {
        return Collections.emptySet();
    }

    /**
     * 合并联邦存储角色映射、realm 默认角色及 {@link #getRoleMappingsInternal()} 结果。
     */
    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        Stream<RoleModel> roleMappings = getFederatedRoleMappingsStream();
        if (appendDefaultRolesToRoleMappings()) {
            roleMappings = Stream.concat(roleMappings, realm.getDefaultRole().getCompositesStream());
        }
        return Stream.concat(roleMappings, getRoleMappingsInternal().stream());
    }

    /**
     * @deprecated Use {@link #getFederatedRoleMappingsStream()} instead
     */
    @Deprecated
    protected Set<RoleModel> getFederatedRoleMappings() {
        return getFederatedRoleMappingsStream().collect(Collectors.toSet());
    }

    /** 从联邦存储读取角色映射 Stream。 */
    protected Stream<RoleModel> getFederatedRoleMappingsStream() {
        return getFederatedStorage().getRoleMappingsStream(realm, this.getId());
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        getFederatedStorage().deleteRoleMapping(realm, this.getId(), role);

    }

    @Override
    public boolean isEnabled() {
        String val = getFirstAttribute(ENABLED_ATTRIBUTE);
        if (val == null) return true;
        else return Boolean.valueOf(val);
    }

    @Override
    public void setEnabled(boolean enabled) {
       setSingleAttribute(ENABLED_ATTRIBUTE, Boolean.toString(enabled));
    }

    /** 返回联邦链接（存储 Provider 组件 ID）；不应被实现类覆盖。 */
    @Override
    public String getFederationLink() {
        return StorageId.providerId(getId());
    }

    /** 设置联邦链接；联邦存储适配器中为空操作。 */
    @Override
    public void setFederationLink(String link) {

    }

    /** 返回服务账户客户端链接，默认 {@code null}。 */
    @Override
    public String getServiceAccountClientLink() {
        return null;
    }

    /** 设置服务账户客户端链接；联邦存储适配器中为空操作。 */
    @Override
    public void setServiceAccountClientLink(String clientInternalId) {

    }

    /** 缓存的用户存储 ID 对象。 */
    protected StorageId storageId;

    /**
     * 默认 ID 格式：{@code 'f:' + storageProvider.getId() + ':' + getUsername()}。
     *
     * @return 联邦用户存储 ID
     */
    @Override
    public String getId() {
        if (storageId == null) {
            storageId = new StorageId(storageProviderModel.getId(), getUsername());
        }
        return storageId.getId();
    }

    @Override
    public Long getCreatedTimestamp() {
        String val = getFirstAttribute(CREATED_TIMESTAMP_ATTRIBUTE);
        if (val == null) return null;
        else return Long.valueOf(val);
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        if (timestamp == null) {
            setSingleAttribute(CREATED_TIMESTAMP_ATTRIBUTE, null);
        } else {
            setSingleAttribute(CREATED_TIMESTAMP_ATTRIBUTE, Long.toString(timestamp));
        }

    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (UserModel.USERNAME.equals(name)) {
            setUsername(value);
        } else {
            getFederatedStorage().setSingleAttribute(realm, this.getId(), mapAttribute(name), value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        getFederatedStorage().removeAttribute(realm, this.getId(), name);

    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (UserModel.USERNAME.equals(name)) {
            setUsername((values != null && !values.isEmpty()) ? values.get(0) : null);
        } else {
            getFederatedStorage().setAttribute(realm, this.getId(), mapAttribute(name), values);
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        if (UserModel.USERNAME.equals(name)) {
            return getUsername();
        }
        return getFederatedStorage().getAttributes(realm, this.getId()).getFirst(mapAttribute(name));
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = getFederatedStorage().getAttributes(realm, this.getId());
        if (attributes == null) {
            attributes = new MultivaluedHashMap<>();
        }
        List<String> firstName = attributes.remove(FIRST_NAME_ATTRIBUTE);
        attributes.add(UserModel.FIRST_NAME, firstName != null && firstName.size() >= 1 ? firstName.get(0) : null);
        List<String> lastName = attributes.remove(LAST_NAME_ATTRIBUTE);
        attributes.add(UserModel.LAST_NAME, lastName != null && lastName.size() >= 1 ? lastName.get(0) : null);
        List<String> email = attributes.remove(EMAIL_ATTRIBUTE);
        attributes.add(UserModel.EMAIL, email != null && email.size() >= 1 ? email.get(0) : null);
        attributes.add(UserModel.USERNAME, getUsername());
        return attributes;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        if (UserModel.USERNAME.equals(name)) {
            return Stream.of(getUsername());
        }
        List<String> result = getFederatedStorage().getAttributes(realm, this.getId()).get(mapAttribute(name));
        return (result == null) ? Stream.empty() : result.stream();
    }

    /**
     * 将 {@link UserModel} 标准属性名映射为联邦存储中的自定义属性键。
     *
     * @param attributeName {@link UserModel} 属性名
     * @return 联邦存储属性键
     */
    protected String mapAttribute(String attributeName) {
        if (UserModel.FIRST_NAME.equals(attributeName)) {
            return FIRST_NAME_ATTRIBUTE;
        } else if (UserModel.LAST_NAME.equals(attributeName)) {
            return LAST_NAME_ATTRIBUTE;
        } else if (UserModel.EMAIL.equals(attributeName)) {
            return EMAIL_ATTRIBUTE;
        }
        return attributeName;
    }

    @Override
    public boolean isEmailVerified() {
        String val = getFirstAttribute(EMAIL_VERIFIED_ATTRIBUTE);
        if (val == null) return false;
        else return Boolean.valueOf(val);
    }

    /**
     * 将邮箱验证状态写入联邦存储的 {@link #EMAIL_VERIFIED_ATTRIBUTE} 属性。
     *
     * @param verified 是否已验证
     */
    @Override
    public void setEmailVerified(boolean verified) {
        setSingleAttribute(EMAIL_VERIFIED_ATTRIBUTE, Boolean.toString(verified));

    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return session.users().getUserCredentialManager(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof UserModel)) return false;

        UserModel that = (UserModel) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * @deprecated 父接口已移除基于集合的方法，可直接使用 {@link AbstractUserAdapterFederatedStorage}，无需此 Streams 子类。
     */
    @Deprecated
    public abstract static class Streams extends AbstractUserAdapterFederatedStorage implements UserModel {

        public Streams(final KeycloakSession session, final RealmModel realm, final ComponentModel storageProviderModel) {
            super(session, realm, storageProviderModel);
        }
    }
}
