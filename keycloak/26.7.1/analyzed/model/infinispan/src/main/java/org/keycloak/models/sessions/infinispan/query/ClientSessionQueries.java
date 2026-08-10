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

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.sessions.infinispan.entities.ClientSessionKey;
import org.keycloak.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.api.query.Query;

/**
 * 针对 {@link RemoteAuthenticatedClientSessionEntity} 的 Infinispan Ickle 查询工具类。
 * <p>
 * 封装远程客户端会话缓存的常用查询语句，供 Provider 与条件删除器复用。
 */
public final class ClientSessionQueries {

    private ClientSessionQueries() {
    }

    /** ProtoStream 实体名，用于 Ickle FROM 子句。 */
    public static final String CLIENT_SESSION = Marshalling.protoEntity(RemoteAuthenticatedClientSessionEntity.class);

    private static final String FETCH_USER_SESSION_ID = "SELECT e.userSessionId FROM %s as e WHERE e.realmId = :realmId && e.clientId = :clientId ORDER BY e.userSessionId".formatted(CLIENT_SESSION);
    private static final String PER_CLIENT_COUNT = "SELECT e.clientId, count(e.clientId) FROM %s as e WHERE e.realmId = :realmId GROUP BY e.clientId ORDER BY e.clientId".formatted(CLIENT_SESSION);
    private static final String CLIENT_SESSION_COUNT = "SELECT count(e) FROM %s as e WHERE e.realmId = :realmId && e.clientId = :clientId".formatted(CLIENT_SESSION);
    private static final String FROM_USER_SESSION = "FROM %s as e WHERE e.userSessionId = :userSessionId ORDER BY e.clientId".formatted(CLIENT_SESSION);
    private static final String FROM_MULTI_USER_SESSION = "FROM %s as e WHERE e.userSessionId IN (%s) ORDER BY e.clientId";
    private static final String IDS_FROM_USER_SESSION = "SELECT e.clientId FROM %s as e WHERE e.userSessionId = :userSessionId ORDER BY e.clientId".formatted(CLIENT_SESSION);

    /**
     * 查询指定客户端下所有客户端会话关联的用户会话 ID 投影。
     *
     * @param cache    远程客户端会话缓存
     * @param realmId  realm ID
     * @param clientId 客户端 ID
     */
    public static Query<Object[]> fetchUserSessionIdForClientId(RemoteCache<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> cache, String realmId, String clientId) {
        return cache.<Object[]>query(FETCH_USER_SESSION_ID)
                .setParameter("realmId", realmId)
                .setParameter("clientId", clientId);
    }

    /**
     * 按客户端统计 realm 内各客户端的活跃会话数量。
     *
     * @param cache   远程客户端会话缓存
     * @param realmId realm ID
     */
    public static Query<Object[]> activeClientCount(RemoteCache<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> cache, String realmId) {
        return cache.<Object[]>query(PER_CLIENT_COUNT)
                .setParameter("realmId", realmId);
    }

    /**
     * 统计指定客户端在 realm 内的客户端会话总数。
     *
     * @param cache    远程客户端会话缓存
     * @param realmId  realm ID
     * @param clientId 客户端 ID
     */
    public static Query<Object[]> countClientSessions(RemoteCache<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> cache, String realmId, String clientId) {
        return cache.<Object[]>query(CLIENT_SESSION_COUNT)
                .setParameter("realmId", realmId)
                .setParameter("clientId", clientId);
    }

    /**
     * 查询属于指定用户会话 ID 的全部客户端会话实体。
     *
     * @param cache         远程客户端会话缓存
     * @param userSessionId 用户会话 ID
     */
    public static Query<RemoteAuthenticatedClientSessionEntity> fetchClientSessions(RemoteCache<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> cache, String userSessionId) {
        return cache.<RemoteAuthenticatedClientSessionEntity>query(FROM_USER_SESSION)
                .setParameter("userSessionId", userSessionId);
    }

    /**
     * 查询属于指定用户会话的客户端 ID 投影。
     * <p>
     * 返回的 {@link Object}[]} 数组仅含一个 {@link String} 元素，即客户端 ID。
     *
     * @param cache         远程客户端会话缓存
     * @param userSessionId 用户会话 ID
     */
    public static Query<Object[]> fetchClientSessionsIds(RemoteCache<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> cache, String userSessionId) {
        return cache.<Object[]>query(IDS_FROM_USER_SESSION)
                .setParameter("userSessionId", userSessionId);
    }

    /**
     * 批量查询多个用户会话 ID 下的全部客户端会话。
     * <p>
     * 单个 ID 时复用单会话查询；多个 ID 时动态生成 IN 子句与命名参数。
     *
     * @param cache          远程客户端会话缓存
     * @param userSessionIds 用户会话 ID 集合（不可为空）
     */
    public static Query<RemoteAuthenticatedClientSessionEntity> fetchClientSessions(RemoteCache<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> cache, Collection<String> userSessionIds) {
        var size = userSessionIds.size();
        if (size == 0) {
            throw new IllegalArgumentException("userSessionIds must not be empty");
        }
        if (size == 1) {
            return fetchClientSessions(cache, userSessionIds.iterator().next());
        }
        var count = new AtomicInteger();
        var params = new HashMap<String, Object>();
        String parameterNames = userSessionIds.stream()
                .map(sessionId -> {
                    String paramName = "p" + count.incrementAndGet();
                    params.put(paramName, sessionId);
                    return ":" + paramName;
                })
                .collect(Collectors.joining(","));
        return cache.<RemoteAuthenticatedClientSessionEntity>query(FROM_MULTI_USER_SESSION.formatted(CLIENT_SESSION, parameterNames))
                .setParameters(params);
    }

}
