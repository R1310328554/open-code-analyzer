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

package org.keycloak.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.client.ClientStorageProvider;
import org.keycloak.storage.client.ClientStorageProviderModel;
import org.keycloak.storage.role.RoleStorageProvider;
import org.keycloak.storage.role.RoleStorageProviderModel;

/**
 * 扩展 {@link RealmModel}，提供 realm 内各类存储 Provider 的便捷访问。
 *
 * @author Alexander Schwartz
 */
public interface StorageProviderRealmModel extends RealmModel {
    /**
     * @deprecated 请改用 {@link #getClientStorageProvidersStream() getClientStorageProvidersStream}。
     */
    @Deprecated
    default List<ClientStorageProviderModel> getClientStorageProviders() {
        return getClientStorageProvidersStream().collect(Collectors.toList());
    }

    /**
     * 返回已排序的 {@link ClientStorageProviderModel} 流。
     * <p>
     * 若需保持顺序，应配合 {@code forEachOrdered} 使用。
     *
     * @return 排序后的客户端存储 Provider 流，永不为 {@code null}。
     */
    default Stream<ClientStorageProviderModel> getClientStorageProvidersStream() {
        return getComponentsStream(getId(), ClientStorageProvider.class.getName())
                .map(ClientStorageProviderModel::new)
                .sorted(ClientStorageProviderModel.comparator);
    }

    /**
     * @deprecated 请改用 {@link #getRoleStorageProvidersStream() getRoleStorageProvidersStream}。
     */
    @Deprecated
    default List<RoleStorageProviderModel> getRoleStorageProviders() {
        return getRoleStorageProvidersStream().collect(Collectors.toList());
    }

    /**
     * 返回已排序的 {@link RoleStorageProviderModel} 流。
     * <p>
     * 若需保持顺序，应配合 {@code forEachOrdered} 使用。
     *
     * @return 排序后的角色存储 Provider 流，永不为 {@code null}。
     */
    default Stream<RoleStorageProviderModel> getRoleStorageProvidersStream() {
        return getComponentsStream(getId(), RoleStorageProvider.class.getName())
                .map(RoleStorageProviderModel::new)
                .sorted(RoleStorageProviderModel.comparator);
    }

    /**
     * @deprecated 请改用 {@link #getUserStorageProvidersStream() getUserStorageProvidersStream}。
     */
    @Deprecated
    default List<UserStorageProviderModel> getUserStorageProviders() {
        return getUserStorageProvidersStream().collect(Collectors.toList());
    }

    /**
     * 返回已排序的 {@link UserStorageProviderModel} 流。
     * <p>
     * 若需保持顺序，应配合 {@code forEachOrdered} 使用。
     *
     * @return 排序后的用户存储 Provider 流，永不为 {@code null}。
     */
    default Stream<UserStorageProviderModel> getUserStorageProvidersStream() {
        return getComponentsStream(getId(), UserStorageProvider.class.getName())
                .map(UserStorageProviderModel::new)
                .sorted(UserStorageProviderModel.comparator);
    }

}
