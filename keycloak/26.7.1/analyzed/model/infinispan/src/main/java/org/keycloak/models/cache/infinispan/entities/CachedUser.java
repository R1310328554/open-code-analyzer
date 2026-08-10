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

package org.keycloak.models.cache.infinispan.entities;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;

/**
 * 用户（User）的 Infinispan 缓存快照实体。
 * <p>
 * 核心身份字段（用户名、邮箱等）在构造时即加载；属性、角色、组、凭证等通过 {@link LazyLoader} 按需加载。
 * 继承 {@link AbstractExtendableRevisioned} 并实现 {@link InRealm}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedUser extends AbstractExtendableRevisioned implements InRealm  {

    /** 所属领域 ID。 */
    private final String realm;
    /** 用户创建时间戳。 */
    private final Long createdTimestamp;
    /** 用户最后修改时间戳。 */
    private final Long lastModifiedTimestamp;
    /** 邮箱是否已验证。 */
    private final boolean emailVerified;
    /** 用户是否启用。 */
    private final boolean enabled;
    /** 联邦存储链接 ID。 */
    private final String federationLink;
    /** 服务账户关联的客户端 ID。 */
    private final String serviceAccountClientLink;
    /** notBefore 时间戳，用于令牌失效控制。 */
    private final int notBefore;
    /** 必需操作（Required Actions）懒加载器。 */
    private final LazyLoader<UserModel, Set<String>> requiredActions;
    /** 非核心用户属性懒加载器。 */
    private final LazyLoader<UserModel, MultivaluedHashMap<String, String>> lazyLoadedAttributes;
    /** 构造时即加载的核心用户属性（用户名、姓名、邮箱）。 */
    private final MultivaluedHashMap<String,String> eagerLoadedAttributes;
    /** 角色映射懒加载器。 */
    private final LazyLoader<UserModel, Set<String>> roleMappings;
    /** 所属组懒加载器。 */
    private final LazyLoader<UserModel, Set<String>> groups;
    /** 存储凭证懒加载器。 */
    private final LazyLoader<UserModel, List<CredentialModel>> storedCredentials;

    /** 从用户模型构造缓存快照。 */
    public CachedUser(long revision, RealmModel realm, UserModel user, int notBefore) {
        super(revision, user.getId());
        this.realm = realm.getId();
        this.createdTimestamp = user.getCreatedTimestamp();
        this.lastModifiedTimestamp = user.getLastModifiedTimestamp();
        this.emailVerified = user.isEmailVerified();
        this.enabled = user.isEnabled();
        this.federationLink = user.getFederationLink();
        this.serviceAccountClientLink = user.getServiceAccountClientLink();
        this.notBefore = notBefore;
        this.eagerLoadedAttributes = new MultivaluedHashMap<>();
        this.eagerLoadedAttributes.putSingle(UserModel.USERNAME,user.getUsername());
        this.eagerLoadedAttributes.putSingle(UserModel.FIRST_NAME,user.getFirstName());
        this.eagerLoadedAttributes.putSingle(UserModel.LAST_NAME,user.getLastName());
        this.eagerLoadedAttributes.putSingle(UserModel.EMAIL,user.getEmail());
        this.lazyLoadedAttributes = new DefaultLazyLoader<>(userModel -> new MultivaluedHashMap<>(userModel.getAttributes()), MultivaluedHashMap::new);
        this.requiredActions = new DefaultLazyLoader<>(userModel -> userModel.getRequiredActionsStream().collect(Collectors.toSet()), Collections::emptySet);
        this.roleMappings = new DefaultLazyLoader<>(userModel -> userModel.getRoleMappingsStream().map(RoleModel::getId).collect(Collectors.toSet()), Collections::emptySet);
        this.groups = new DefaultLazyLoader<>(userModel -> userModel.getGroupsStream().map(GroupModel::getId).collect(Collectors.toCollection(LinkedHashSet::new)), LinkedHashSet::new);
        this.storedCredentials = new DefaultLazyLoader<>(userModel -> userModel.credentialManager().getStoredCredentialsStream().collect(Collectors.toCollection(LinkedList::new)), LinkedList::new);
    }

    /** 返回所属领域 ID。 */
    public String getRealm() {
        return realm;
    }

    /** 返回用户名（构造时即加载）。 */
    public String getUsername() {
        return eagerLoadedAttributes.getFirst(UserModel.USERNAME);
    }

    /** 按名称获取首个用户属性值，优先从 eager 缓存读取，否则按需懒加载。 */
    public String getFirstAttribute(KeycloakSession session, String name, Supplier<UserModel> userModel) {
        if(eagerLoadedAttributes.containsKey(name))
            return eagerLoadedAttributes.getFirst(name);
        else
            return this.lazyLoadedAttributes.get(session, userModel).getFirst(name);
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public String getEmail() {
        return eagerLoadedAttributes.getFirst(UserModel.EMAIL);
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public MultivaluedHashMap<String, String> getAttributes(KeycloakSession session, Supplier<UserModel> userModel) {
        return lazyLoadedAttributes.get(session, userModel);
    }

    public Set<String> getRequiredActions(KeycloakSession session, Supplier<UserModel> userModel) {
        return this.requiredActions.get(session, userModel);
    }

    public Set<String> getRoleMappings(KeycloakSession session, Supplier<UserModel> userModel) {
        return roleMappings.get(session, userModel);
    }

    public String getFederationLink() {
        return federationLink;
    }

    public String getServiceAccountClientLink() {
        return serviceAccountClientLink;
    }

    public Set<String> getGroups(KeycloakSession session, Supplier<UserModel> userModel) {
        return groups.get(session, userModel);
    }

    public int getNotBefore() {
        return notBefore;
    }

    /** 按需加载并返回存储凭证列表（浅拷贝，避免修改污染缓存）。 */
    public List<CredentialModel> getStoredCredentials(KeycloakSession session, Supplier<UserModel> userModel) {
        // 返回前浅拷贝凭证模型，避免外部修改污染缓存
        return storedCredentials.get(session, userModel).stream().map(CredentialModel::shallowClone).collect(Collectors.toList());
    }

}
