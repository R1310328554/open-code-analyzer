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

package org.keycloak.models;

import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.provider.Provider;

/**
 * Realm 数据提供者：负责 Realm 的 CRUD、本地化与客户端初始访问管理。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RealmProvider extends Provider {

    /**
     * 创建指定名称的新 Realm，内部 ID 自动生成。
     * Creates new realm with the given name. The internal ID will be generated automatically.
     * @param name String name of the realm
     * @return Model of the created realm.
     */
    RealmModel createRealm(String name);

    /**
     * 使用给定 ID 与名称创建新 Realm。
     * Created new realm with given ID and name.
     * @param id Internal ID of the realm or {@code null} if one is to be created by the underlying store. If the store
     *           expects the ID to have a certain format (for example {@code UUID}) and the supplied ID doesn't follow
     *           the expected format, the store may replace the {@code id} with a new one at its own discretion.
     * @param name String name of the realm
     * @return Model of the created realm.
     */
    RealmModel createRealm(String id, String name);

    /**
     * 按内部 ID 精确查找 Realm。
     * Exact search for a realm by its internal ID.
     * @param id Internal ID of the realm.
     * @return Model of the realm
     */
    RealmModel getRealm(String id);

    /**
     * 按名称精确查找 Realm。
     * Exact search for a realm by its name.
     * @param name String name of the realm
     * @return Model of the realm
     */
    RealmModel getRealmByName(String name);

    /**
     * 以流形式返回所有 Realm。
     * Returns realms as a stream.
     * @return Stream of {@link RealmModel}. Never returns {@code null}.
     */
    Stream<RealmModel> getRealmsStream();

    /**
     * 按名称搜索条件返回 Realm 流。
     * Returns realms as a stream filtered by search.
     * @param search String to search for in realm names
     * @return Stream of {@link RealmModel}. Never returns {@code null}.
     */
    default Stream<RealmModel> getRealmsStream(String search) {
        return getRealmsStream().filter(realm -> search.isEmpty() || realm.getName().toLowerCase().contains(search.trim().toLowerCase()));
    }

    /**
     * 返回包含指定 Provider 类型组件的 Realm 流。
     * Returns stream of realms which has component with the given provider type.
     * @param type {@code Class<?>} Type of the provider.
     * @return Stream of {@link RealmModel}. Never returns {@code null}.
     */
    Stream<RealmModel> getRealmsWithProviderTypeStream(Class<?> type);

    /**
     * 删除指定 ID 的 Realm。
     * Removes realm with the given id.
     * @param id of realm.
     * @return {@code true} if the realm was successfully removed.
     */
    boolean removeRealm(String id);

    /** @param realm Realm
     * @param expiration 过期时间
     * @param count 可用次数
     * @return 客户端初始访问模型 */
    default ClientInitialAccessModel createClientInitialAccessModel(RealmModel realm, int expiration, int count) {
        return realm.createClientInitialAccessModel(expiration, count);
    }
    default ClientInitialAccessModel getClientInitialAccessModel(RealmModel realm, String id) {
        return realm.getClientInitialAccessModel(id);
    }
    default void removeClientInitialAccessModel(RealmModel realm, String id) {
        realm.removeClientInitialAccessModel(id);
    }

    /**
     * 以流形式返回客户端初始访问配置。
     * Returns client's initial access as a stream.
     * @param realm {@link RealmModel} The realm where to list client's initial access.
     * @return Stream of {@link ClientInitialAccessModel}. Never returns {@code null}.
     */
    default Stream<ClientInitialAccessModel> listClientInitialAccessStream(RealmModel realm) {
        return realm.getClientInitialAccesses();
    }

    /**
     * 清理所有 Realm 中已过期的客户端初始访问记录。
     * Removes all expired client initial accesses from all realms.
     */
    void removeExpiredClientInitialAccess();

    /** 原子递减客户端初始访问剩余次数。 */
    default void decreaseRemainingCount(RealmModel realm, ClientInitialAccessModel clientInitialAccess) { // Separate provider method to ensure we decrease remainingCount atomically instead of doing classic update
        realm.decreaseRemainingCount(clientInitialAccess);
    }

    /** @param realm Realm
     * @param locale 语言区域
     * @param key 文本键
     * @param text 本地化文本 */
    void saveLocalizationText(RealmModel realm, String locale, String key, String text);

    /** @param realm Realm
     * @param locale 语言区域
     * @param localizationTexts 本地化文本映射 */
    void saveLocalizationTexts(RealmModel realm, String locale, Map<String, String> localizationTexts);

    /** 更新单条本地化文本。 */
    boolean updateLocalizationText(RealmModel realm, String locale, String key, String text);

    /** 删除指定语言区域的所有本地化文本。 */
    boolean deleteLocalizationTextsByLocale(RealmModel realm, String locale);

    /** 删除单条本地化文本。 */
    boolean deleteLocalizationText(RealmModel realm, String locale, String key);

    /** @param realm Realm
     * @param locale 语言区域
     * @param key 文本键
     * @return 本地化文本 */
    String getLocalizationTextsById(RealmModel realm, String locale, String key);
}
