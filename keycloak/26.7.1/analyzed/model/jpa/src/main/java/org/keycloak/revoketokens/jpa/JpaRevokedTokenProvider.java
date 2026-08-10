/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.revoketokens.jpa;

import java.util.Objects;

import jakarta.persistence.EntityManager;

import org.keycloak.cache.LocalCache;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RevokedTokenProvider;

/**
 * 基于 JPA 的 {@link RevokedTokenProvider}：在数据库中记录已吊销令牌 ID 及过期时间。
 * <p>
 * 查询路径使用本地 {@link LocalCache} 缓存热点 ID，减轻恶意客户端反复校验同一令牌时的数据库压力。
 */
public class JpaRevokedTokenProvider implements RevokedTokenProvider {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 已吊销令牌 ID 的本地缓存（值为剩余寿命秒数）。 */
    private final LocalCache<String, Long> cache;

    public JpaRevokedTokenProvider(KeycloakSession session, LocalCache<String, Long> cache) {
        this.session = Objects.requireNonNull(session);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public boolean put(String id, long lifespanSeconds) {
        var em = getEntityManager();
        var currentTime = Time.currentTime();
        var expire = currentTime + lifespanSeconds;
        var rows = em.createNamedQuery("insertRevokeTokenIfAbsent")
                .setParameter("id", id)
                .setParameter("currentTime", currentTime)
                .setParameter("expire", expire)
                .executeUpdate();
        return rows == 1;
    }

    @Override
    public boolean contains(String id) {
        if (cache.get(id) != null) {
            return true;
        }
        var expireTime = getEntityManager().createNamedQuery("findRevokeTokenExpireTime", Long.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getSingleResultOrNull();
        if (expireTime == null) {
            return false;
        }
        var lifespan = expireTime - Time.currentTime();
        if (lifespan > 0) {
            // 写入缓存，应对恶意客户端反复复用同一令牌的场景
            cache.put(id, lifespan);
            return true;
        }
        return false;
    }

    @Override
    public void close() {

    }

    /** 从会话获取 JPA {@link EntityManager}。 */
    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }
}
