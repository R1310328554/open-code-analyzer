/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.util.reflections.Types;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.RoleProvider;
import org.keycloak.models.StorageProviderRealmModel;
import org.keycloak.storage.role.RoleLookupProvider;
import org.keycloak.storage.role.RoleStorageProvider;
import org.keycloak.storage.role.RoleStorageProviderFactory;
import org.keycloak.storage.role.RoleStorageProviderModel;
import org.keycloak.utils.ServicesUtils;

import org.jboss.logging.Logger;

/**
 * 角色存储管理器：在本地 {@link RoleProvider} 与外部 {@link RoleStorageProvider} 之间路由角色查询与管理操作。
 * <p>
 * 联邦角色的删除不受支持；跨存储搜索时合并本地与外部结果，并对联邦查询施加超时限制。
 */
public class RoleStorageManager implements RoleProvider {
    private static final Logger logger = Logger.getLogger(RoleStorageManager.class);

    protected KeycloakSession session;

    /** 外部角色存储查询超时（毫秒）。 */
    private final long roleStorageProviderTimeout;

    /** 构造角色存储管理器并设置联邦存储查询超时。 */
    public RoleStorageManager(KeycloakSession session, long roleStorageProviderTimeout) {
        this.session = session;
        this.roleStorageProviderTimeout = roleStorageProviderTimeout;
    }

    /** 获取本地角色 Provider（非联邦存储）。 */
    private RoleProvider localStorage() {
        return session.getProvider(RoleProvider.class);
    }

    /** 判断指定领域的角色存储 Provider 是否已启用。 */
    public static boolean isStorageProviderEnabled(RealmModel realm, String providerId) {
        RoleStorageProviderModel model = getStorageProviderModel(realm, providerId);
        return model.isEnabled();
    }

    /** 按组件 ID 获取角色存储 Provider 配置模型。 */
    public static RoleStorageProviderModel getStorageProviderModel(RealmModel realm, String componentId) {
        ComponentModel model = realm.getComponent(componentId);
        if (model == null) return null;
        return new RoleStorageProviderModel(model);
    }

    /** 获取指定组件 ID 对应的 {@link RoleStorageProvider} 实例。 */
    public static RoleStorageProvider getStorageProvider(KeycloakSession session, RealmModel realm, String componentId) {
        ComponentModel model = realm.getComponent(componentId);
        if (model == null) return null;
        RoleStorageProviderModel storageModel = new RoleStorageProviderModel(model);
        RoleStorageProviderFactory factory = (RoleStorageProviderFactory)session.getKeycloakSessionFactory().getProviderFactory(RoleStorageProvider.class, model.getProviderId());
        if (factory == null) {
            throw new ModelException("Could not find RoletStorageProviderFactory for: " + model.getProviderId());
        }
        return getStorageProviderInstance(session, storageModel, factory);
    }

    /** 返回支持指定类型的已配置角色存储 Provider 模型流。 */
    public static <T> Stream<RoleStorageProviderModel> getStorageProviders(RealmModel realm, KeycloakSession session, Class<T> type) {
        return ((StorageProviderRealmModel) realm).getRoleStorageProvidersStream()
                .filter(model -> {
                    RoleStorageProviderFactory factory = getRoleStorageProviderFactory(model, session);
                    if (factory == null) {
                        logger.warnv("Configured RoleStorageProvider {0} of provider id {1} does not exist in realm {2}",
                                model.getName(), model.getProviderId(), realm.getName());
                        return false;
                    } else {
                        return Types.supports(type, factory, RoleStorageProviderFactory.class);
                    }
                });
    }

    private static RoleStorageProviderFactory getRoleStorageProviderFactory(RoleStorageProviderModel model, KeycloakSession session) {
        return (RoleStorageProviderFactory) session.getKeycloakSessionFactory()
                .getProviderFactory(RoleStorageProvider.class, model.getProviderId());
    }

    /** 获取或创建角色存储 Provider 实例（会话级缓存）。 */
    public static RoleStorageProvider getStorageProviderInstance(KeycloakSession session, RoleStorageProviderModel model, RoleStorageProviderFactory factory) {
        RoleStorageProvider instance = (RoleStorageProvider)session.getAttribute(model.getId());
        if (instance != null) return instance;
        instance = factory.create(session, model);
        if (instance == null) {
            throw new IllegalStateException("RoleStorageProvideFactory (of type " + factory.getClass().getName() + ") produced a null instance");
        }
        session.enlistForClose(instance);
        session.setAttribute(model.getId(), instance);
        return instance;
    }


    public static <T> Stream<T> getStorageProviders(KeycloakSession session, RealmModel realm, Class<T> type) {
        return getStorageProviders(realm, session, type)
                .map(model -> type.cast(getStorageProviderInstance(session, model, getRoleStorageProviderFactory(model, session))));
    }


    /** 返回已启用的指定类型角色存储 Provider 实例流。 */
    public static <T> Stream<T> getEnabledStorageProviders(KeycloakSession session, RealmModel realm, Class<T> type) {
        return getStorageProviders(realm, session, type)
                .filter(RoleStorageProviderModel::isEnabled)
                .map(model -> type.cast(getStorageProviderInstance(session, model, getRoleStorageProviderFactory(model, session))));
    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String name) {
        return localStorage().addRealmRole(realm, name);
    }

    @Override
    public RoleModel addRealmRole(RealmModel realm, String id, String name) {
        return localStorage().addRealmRole(realm, id, name);
    }

    @Override
    public RoleModel getRealmRole(RealmModel realm, String name) {
        RoleModel realmRole = localStorage().getRealmRole(realm, name);
        if (realmRole != null) return realmRole;
        return getEnabledStorageProviders(session, realm, RoleLookupProvider.class)
                .map(provider -> provider.getRealmRole(realm, name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public RoleModel getRoleById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        if (storageId.getProviderId() == null) {
            return localStorage().getRoleById(realm, id);
        }
        RoleLookupProvider provider = (RoleLookupProvider)getStorageProvider(session, realm, storageId.getProviderId());
        if (provider == null) return null;
        if (! isStorageProviderEnabled(realm, storageId.getProviderId())) return null;
        return provider.getRoleById(realm, id);
    }

    @Override
    public Stream<RoleModel> getRealmRolesStream(RealmModel realm, Integer first, Integer max) {
        return localStorage().getRealmRolesStream(realm, first, max);
    }

    @Override
    public Stream<RoleModel> getRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        return localStorage().getRolesStream(realm, ids, search, first, max);
    }

    @Override
    public Stream<RoleModel> getCompositeRolesStream(RealmModel realm, Set<String> parentRoleIds) {
        if (parentRoleIds == null || parentRoleIds.isEmpty()) {
            return Stream.empty();
        }
        // 拆分本地与外部 ID：本地 ID 走单次批量查询；联邦 ID 逐个 getRoleById 后展开组合关系。
        Set<String> localIds = parentRoleIds.stream()
                .filter(StorageId::isLocalStorage)
                .collect(Collectors.toSet());
        Set<String> externalIds = parentRoleIds.stream()
                .filter(id -> !StorageId.isLocalStorage(id))
                .collect(Collectors.toSet());

        Stream<RoleModel> localComposites = localIds.isEmpty()
                ? Stream.empty()
                : localStorage().getCompositeRolesStream(realm, localIds);

        Stream<RoleModel> externalComposites = externalIds.stream()
                .map(id -> getRoleById(realm, id))
                .filter(Objects::nonNull)
                .flatMap(RoleModel::getCompositesStream);

        return Stream.concat(localComposites, externalComposites).distinct();
    }

    /**
     * 从外部角色存储获取角色时受超时限制；外部存储不可用时至少返回本地存储中的角色。
     * 使用 <code>org.keycloak.services.DefaultKeycloakSessionFactory#getRoleStorageProviderTimeout()</code> 配置超时，
     * 默认 3000 毫秒，可配置。详见 <code>org.keycloak.services.DefaultKeycloakSessionFactory</code>。
     */
    @Override
    public Stream<RoleModel> searchForRolesStream(RealmModel realm, String search, Integer first, Integer max) {
        Stream<RoleModel> local = localStorage().searchForRolesStream(realm, search, first, max);
        Stream<RoleModel> ext = getEnabledStorageProviders(session, realm, RoleLookupProvider.class)
                .flatMap(ServicesUtils.timeBound(session,
                        roleStorageProviderTimeout,
                        p -> ((RoleLookupProvider) p).searchForRolesStream(realm, search, first, max)));

        return Stream.concat(local, ext);
    }

    @Override
    public boolean removeRole(RoleModel role) {
        if (!StorageId.isLocalStorage(role.getId())) {
            throw new RuntimeException("Federated roles do not support this operation");
        }
        return localStorage().removeRole(role);
    }

    @Override
    public void removeRoles(RealmModel realm) {
        localStorage().removeRoles(realm);
    }

    @Override
    public void removeRoles(ClientModel client) {
        localStorage().removeRoles(client);
    }

    @Override
    public RoleModel addClientRole(ClientModel client, String name) {
        return localStorage().addClientRole(client, name);
    }

    @Override
    public RoleModel addClientRole(ClientModel client, String id, String name) {
        return localStorage().addClientRole(client, id, name);
    }

    @Override
    public RoleModel getClientRole(ClientModel client, String name) {
        RoleModel clientRole = localStorage().getClientRole(client, name);
        if (clientRole != null) return clientRole;
        return getEnabledStorageProviders(session, client.getRealm(), RoleLookupProvider.class)
                .map(provider -> provider.getClientRole(client, name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Stream<RoleModel> getClientRolesStream(ClientModel client) {
        return localStorage().getClientRolesStream(client);
    }

    @Override
    public Stream<RoleModel> getClientRolesStream(ClientModel client, Integer first, Integer max) {
        return localStorage().getClientRolesStream(client, first, max);
    }

    /**
     * 从外部角色存储搜索客户端角色时受超时限制；外部存储不可用时至少返回本地结果。
     * 使用 <code>org.keycloak.services.DefaultKeycloakSessionFactory#getRoleStorageProviderTimeout()</code> 配置超时，
     * 默认 3000 毫秒，可配置。详见 <code>org.keycloak.services.DefaultKeycloakSessionFactory</code>。
     */
    @Override
    public Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max) {
        Stream<RoleModel> local = localStorage().searchForClientRolesStream(client, search, first, max);
        Stream<RoleModel> ext = getEnabledStorageProviders(session, client.getRealm(), RoleLookupProvider.class)
                .flatMap(ServicesUtils.timeBound(session,
                        roleStorageProviderTimeout,
                        p -> ((RoleLookupProvider) p).searchForClientRolesStream(client, search, first, max)));

        return Stream.concat(local, ext);
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        Stream<RoleModel> local = localStorage().searchForClientRolesStream(realm, ids, search, first, max);
        Stream<RoleModel> ext = getEnabledStorageProviders(session, realm, RoleLookupProvider.class)
                .flatMap(ServicesUtils.timeBound(session,
                        roleStorageProviderTimeout,
                        p -> ((RoleLookupProvider) p).searchForClientRolesStream(realm, ids, search, first, max)));

        return Stream.concat(local, ext);
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(RealmModel realm, String search, Stream<String> excludedIds, Integer first, Integer max) {
        Stream<RoleModel> local = localStorage().searchForClientRolesStream(realm, search, excludedIds, first, max);
        Stream<RoleModel> ext = getEnabledStorageProviders(session, realm, RoleLookupProvider.class)
                .flatMap(ServicesUtils.timeBound(session,
                        roleStorageProviderTimeout,
                        p -> ((RoleLookupProvider) p).searchForClientRolesStream(realm, search, excludedIds, first, max)));

        return Stream.concat(local, ext);
    }

    @Override
    public void close() {
    }
}
