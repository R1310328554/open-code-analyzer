/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.query;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureEntity;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureKey;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.api.query.Query;

/**
 * 针对 {@link LoginFailureEntity} 的 Infinispan Ickle 查询工具类。
 * <p>
 * 用于远程登录失败记录在 realm 维度的检索与批量更新。
 */
public final class LoginFailureQueries {

    private LoginFailureQueries() {
    }

    /** ProtoStream 实体名，用于 Ickle FROM 子句。 */
    public static final String LOGIN_FAILURE = Marshalling.protoEntity(LoginFailureEntity.class);

    private static final String BASE_QUERY = "FROM %s as e ".formatted(LOGIN_FAILURE);
    private static final String BY_REALM_ID = BASE_QUERY + "WHERE e.realmId = :realmId";

    /**
     * 按 realm ID 查询该 realm 下全部登录失败记录。
     *
     * @param cache   远程登录失败缓存
     * @param realmId realm ID
     */
    public static Query<LoginFailureEntity> searchByRealmId(RemoteCache<LoginFailureKey, LoginFailureEntity> cache, String realmId) {
        return cache.<LoginFailureEntity>query(BY_REALM_ID)
                .setParameter("realmId", realmId);
    }
}
