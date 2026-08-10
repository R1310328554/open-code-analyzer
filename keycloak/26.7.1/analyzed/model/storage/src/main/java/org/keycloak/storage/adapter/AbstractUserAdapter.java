/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
import java.util.HashSet;
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
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModelDefaultMethods;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;

/**
 * 外部用户存储适配器的抽象基类，除 {@link #getUsername()} 外提供默认实现。
 * <p>
 * {@link #getId()} 默认返回 {@code "f:" + providerId + ":" + getUsername()}；
 * {@link #isEnabled()} 默认 {@code true}；
 * {@link #getRoleMappings()} 会包含 realm 默认角色；
 * {@link #getGroups()} 会包含 realm 默认组。
 * <p>
 * 其余读方法按返回类型返回 {@code null}、空集合或 {@code false}；
 * 所有写操作抛出 {@link ReadOnlyException}，表示用户只读。
 * <p>
 * Provider 实现者应覆盖其支持属性、属性映射与角色/组映射的相关方法。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractUserAdapter extends UserModelDefaultMethods {
    /** 当前 Keycloak 会话。 */
    protected KeycloakSession session;
    /** 用户所属 realm。 */
    protected RealmModel realm;
    /** 用户存储 Provider 的组件配置模型。 */
    protected ComponentModel storageProviderModel;

    /**
     * 构造只读用户适配器。
     *
     * @param session               当前会话
     * @param realm                 所属 realm
     * @param storageProviderModel  用户存储 Provider 组件模型
     */
    public AbstractUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel) {
        this.session = session;
        this.realm = realm;
        this.storageProviderModel = storageProviderModel;
    }

    /**
     * @deprecated User {@link #getRequiredActionsStream()}
     */
    public Set<String> getRequiredActions() {
        return Collections.emptySet();
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return getRequiredActions().stream();
    }

    @Override
    public void addRequiredAction(String action) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public void removeRequiredAction(String action) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public void addRequiredAction(RequiredAction action) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        throw new ReadOnlyException("user is read only for this update");
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
     * 是否在 {@link #getGroups()} 结果中追加 realm 默认组。
     * <p>
     * 若存储 Provider 不管理组映射，建议返回 {@code true}。
     *
     * @return 追加默认组返回 {@code true}
     */
    protected boolean appendDefaultGroups() {
        return true;
    }

    /**
     * @deprecated Use {@link #getGroupsStream()} instead
     */
    public Set<GroupModel> getGroups() {
        Set<GroupModel> set = new HashSet<>();
        if (appendDefaultGroups()) set.addAll(realm.getDefaultGroupsStream().collect(Collectors.toSet()));
        set.addAll(getGroupsInternal());
        return set;
    }

    @Override
    public Stream<GroupModel> getGroupsStream() {
        return getGroups().stream();
    }

    @Override
    public void joinGroup(GroupModel group) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public void leaveGroup(GroupModel group) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        return RoleUtils.isMember(getGroups().stream(), group);
    }

    /**
     *
     * @deprecated Use {@link #getRealmRoleMappingsStream()} instead
     */
    public Set<RoleModel> getRealmRoleMappings() {
        return getRoleMappings().stream().filter(RoleUtils::isRealmRole).collect(Collectors.toSet());
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return getRealmRoleMappings().stream();
    }

    /**
     *
     * @deprecated Use {@link #getClientRoleMappingsStream(ClientModel)} instead
     */
    public Set<RoleModel> getClientRoleMappings(ClientModel app) {
        return getRoleMappings().stream().filter(r -> RoleUtils.isClientRole(r, app)).collect(Collectors.toSet());
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
        return getClientRoleMappings(app).stream();
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return RoleUtils.hasRole(getRoleMappings().stream(), role)
          || RoleUtils.hasRoleFromGroup(getGroups().stream(), role, true);
    }

    @Override
    public void grantRole(RoleModel role) {
        throw new ReadOnlyException("user is read only for this update");

    }

    /**
     * 是否在 {@link #getRoleMappings()} 结果中追加 realm 默认角色。
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
     *
     * @deprecated Use {@link #getRoleMappingsStream()} instead
     */
    public Set<RoleModel> getRoleMappings() {
        Set<RoleModel> set = new HashSet<>();
        if (appendDefaultRolesToRoleMappings()) set.addAll(realm.getDefaultRole().getCompositesStream().collect(Collectors.toSet()));
        set.addAll(getRoleMappingsInternal());
        return set;
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return getRoleMappings().stream();
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        throw new ReadOnlyException("user is read only for this update");
    }

    /**
     * 返回联邦链接（存储 Provider 组件 ID）；不应被实现类覆盖。
     *
     * @return Provider 组件 ID
     */
    @Override
    public String getFederationLink() {
        return StorageId.providerId(getId());
    }

    /** 设置联邦链接；只读适配器不允许修改，抛出 {@link ReadOnlyException}。 */
    @Override
    public void setFederationLink(String link) {
        throw new ReadOnlyException("user is read only for this update");

    }

    /** 返回服务账户客户端链接，只读适配器默认 {@code null}。 */
    @Override
    public String getServiceAccountClientLink() {
        return null;
    }

    /** 设置服务账户客户端链接；只读适配器不允许修改。 */
    @Override
    public void setServiceAccountClientLink(String clientInternalId) {
        throw new ReadOnlyException("user is read only for this update");

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
    public void setUsername(String username) {
        throw new ReadOnlyException("user is read only for this update");
    }

    /** 用户创建时间戳，默认构造时取当前毫秒时间。 */
    protected long created = System.currentTimeMillis();

    @Override
    public Long getCreatedTimestamp() {
        return created;
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public void setSingleAttribute(String name, String value) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public void removeAttribute(String name) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public void setAttribute(String name, List<String> values) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public String getFirstAttribute(String name) {
        if (name.equals(UserModel.USERNAME)) {
            return getUsername();
        }
        return null;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add(UserModel.USERNAME, getUsername());
        return attributes;
    }

    /**
     * @deprecated Use {@link #getAttributeStream(String)} instead
     */
    public List<String> getAttribute(String name) {
        if (name.equals(UserModel.USERNAME)) {
            return Collections.singletonList(getUsername());
        }
        return Collections.emptyList();
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return getAttribute(name).stream();
    }

    @Override
    public String getFirstName() {
        return null;
    }

    @Override
    public void setFirstName(String firstName) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public String getLastName() {
        return null;
    }

    @Override
    public void setLastName(String lastName) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public void setEmail(String email) {
        throw new ReadOnlyException("user is read only for this update");

    }

    @Override
    public boolean isEmailVerified() {
        return false;
    }

    @Override
    public void setEmailVerified(boolean verified) {
        throw new ReadOnlyException("user is read only for this update");

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
     * {@link Streams} 子类以 {@link Stream} 为首选实现方式：
     * 集合型方法委托给 Stream 变体，便于实现类优化内存与性能。
     * <p/>
     * 实现类只需覆盖 Stream 方法，集合方法由本类默认提供。
     */
    public abstract static class Streams extends AbstractUserAdapter implements UserModel {

        public Streams(final KeycloakSession session, final RealmModel realm, final ComponentModel storageProviderModel) {
            super(session, realm, storageProviderModel);
        }

        @Override
        public Set<String> getRequiredActions() {
            return this.getRequiredActionsStream().collect(Collectors.toSet());
        }

        @Override
        public Stream<String> getRequiredActionsStream() {
            return Stream.empty();
        }

        @Override
        public List<String> getAttribute(String name) {
            return this.getAttributeStream(name).collect(Collectors.toList());
        }

        @Override
        public Stream<String> getAttributeStream(String name) {
            if (name.equals(UserModel.USERNAME)) {
                return Stream.of(getUsername());
            }
            return Stream.empty();
        }

        // 组相关方法：Stream 优先实现。


        @Override
        public Set<GroupModel> getGroups() {
            return this.getGroupsStream().collect(Collectors.toSet());
        }

        @Override
        public Stream<GroupModel> getGroupsStream() {
            Stream<GroupModel> groups = getGroupsInternal().stream();
            if (appendDefaultGroups()) groups = Stream.concat(groups, realm.getDefaultGroupsStream());
            return groups;
        }

        @Override
        public boolean isMemberOf(GroupModel group) {
            return RoleUtils.isMember(this.getGroupsStream(), group);
        }

        // 角色相关方法：Stream 优先实现。


        @Override
        public Set<RoleModel> getRealmRoleMappings() {
            return this.getRealmRoleMappingsStream().collect(Collectors.toSet());
        }

        @Override
        public Stream<RoleModel> getRealmRoleMappingsStream() {
            return getRoleMappingsStream().filter(RoleUtils::isRealmRole);
        }

        @Override
        public Set<RoleModel> getClientRoleMappings(ClientModel app) {
            return this.getClientRoleMappingsStream(app).collect(Collectors.toSet());
        }

        @Override
        public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
            return getRoleMappingsStream().filter(r -> RoleUtils.isClientRole(r, app));
        }

        @Override
        public Set<RoleModel> getRoleMappings() {
            return this.getRoleMappingsStream().collect(Collectors.toSet());
        }

        @Override
        public Stream<RoleModel> getRoleMappingsStream() {
            Stream<RoleModel> roleMappings = getRoleMappingsInternal().stream();
            if (appendDefaultRolesToRoleMappings()) return Stream.concat(roleMappings, realm.getDefaultRole().getCompositesStream());
            return roleMappings;
        }

        @Override
        public boolean hasRole(RoleModel role) {
            return RoleUtils.hasRole(this.getRoleMappingsStream(), role)
                    || RoleUtils.hasRoleFromGroup(this.getGroupsStream(), role, true);
        }
    }
}
