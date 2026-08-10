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
import org.keycloak.models.sessions.infinispan.entities.RemoteUserSessionEntity;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.api.query.Query;

/**
 * 针对 {@link RemoteUserSessionEntity} 的 Infinispan Ickle 查询工具类。
 * <p>
 * 封装远程用户会话缓存按 broker、用户及 realm 维度的检索语句。
 */
public final class UserSessionQueries {

    private UserSessionQueries() {
    }

    /** ProtoStream 实体名，用于 Ickle FROM 子句。 */
    public static final String USER_SESSION = Marshalling.protoEntity(RemoteUserSessionEntity.class);

    private static final String BASE_QUERY = "FROM %s as e ".formatted(USER_SESSION);
    private static final String BY_BROKER_SESSION_ID = BASE_QUERY + "WHERE e.realmId = :realmId && e.brokerSessionId = :brokerSessionId ORDER BY e.userSessionId";
    private static final String BY_USER_ID = BASE_QUERY + "WHERE e.realmId = :realmId && e.userId = :userId ORDER BY e.userSessionId";
    private static final String BY_BROKER_USER_ID = BASE_QUERY + "WHERE e.realmId = :realmId && e.brokerUserId = :brokerUserId ORDER BY e.userSessionId";
    private static final String BY_REALM = BASE_QUERY + "WHERE e.realmId = :realmId ORDER BY e.userSessionId";

    /**
     * 按 broker 会话 ID 查询关联的用户会话。
     *
     * @param cache           远程用户会话缓存
     * @param realmId         realm ID
     * @param brokerSessionId 身份 broker 侧会话 ID
     */
    public static Query<RemoteUserSessionEntity> searchByBrokerSessionId(RemoteCache<String, RemoteUserSessionEntity> cache, String realmId, String brokerSessionId) {
        return cache.<RemoteUserSessionEntity>query(BY_BROKER_SESSION_ID)
                .setParameter("realmId", realmId)
                .setParameter("brokerSessionId", brokerSessionId);
    }

    /**
     * 按用户 ID 查询该用户在 realm 内的全部用户会话。
     *
     * @param cache   远程用户会话缓存
     * @param realmId realm ID
     * @param userId  用户 ID
     */
    public static Query<RemoteUserSessionEntity> searchByUserId(RemoteCache<String, RemoteUserSessionEntity> cache, String realmId, String userId) {
        return cache.<RemoteUserSessionEntity>query(BY_USER_ID)
                .setParameter("realmId", realmId)
                .setParameter("userId", userId);
    }

    /**
     * 按 broker 用户 ID 查询关联的用户会话。
     *
     * @param cache         远程用户会话缓存
     * @param realmId       realm ID
     * @param brokerUserId  身份 broker 侧用户 ID
     */
    public static Query<RemoteUserSessionEntity> searchByBrokerUserId(RemoteCache<String, RemoteUserSessionEntity> cache, String realmId, String brokerUserId) {
        return cache.<RemoteUserSessionEntity>query(BY_BROKER_USER_ID)
                .setParameter("realmId", realmId)
                .setParameter("brokerUserId", brokerUserId);
    }

    /**
     * 查询指定 realm 下的全部用户会话。
     *
     * @param cache   远程用户会话缓存
     * @param realmId realm ID
     */
    public static Query<RemoteUserSessionEntity> searchByRealm(RemoteCache<String, RemoteUserSessionEntity> cache, String realmId) {
        return cache.<RemoteUserSessionEntity>query(BY_REALM)
                .setParameter("realmId", realmId);
    }
}
