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

package org.keycloak.storage;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.cache.UserCache;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

/**
 * 用户存储相关 Provider 的便捷访问工具类。
 *
 * @author Alexander Schwartz
 */
public class UserStorageUtil {

    /**
     * 从当前会话获取 {@link UserFederatedStorageProvider}，用于读写联邦用户的属性、角色、组等扩展数据。
     *
     * @param session 当前 Keycloak 会话
     * @return 用户联邦存储 Provider 实例
     */
    public static UserFederatedStorageProvider userFederatedStorage(KeycloakSession session) {
        return session.getProvider(UserFederatedStorageProvider.class);
    }

    /**
     * 从当前会话获取 {@link UserCache}，用于用户查询缓存的失效与更新。
     *
     * @param session 当前 Keycloak 会话
     * @return 用户缓存 Provider 实例
     */
    public static UserCache userCache(KeycloakSession session) {
        return session.getProvider(UserCache.class);
    }

}
