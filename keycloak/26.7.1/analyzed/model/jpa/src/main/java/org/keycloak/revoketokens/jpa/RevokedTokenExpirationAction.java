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

import java.util.function.IntConsumer;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.expiration.jpa.ExpirationAction;
import org.keycloak.models.KeycloakSession;

/**
 * 已吊销令牌过期清理动作：批量删除数据库中已过期的吊销记录。
 */
public enum RevokedTokenExpirationAction implements ExpirationAction {
    /** 单例实例。 */
    INSTANCE;

    @Override
    public boolean removeExpired(KeycloakSession session, String realmId, int currentTime, int maxRemoval, IntConsumer removeCount) {
        var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        var ids = em.createNamedQuery("findExpiredRevokedTokenIds", String.class)
                .setParameter("currentTime", currentTime)
                .setMaxResults(maxRemoval)
                .getResultList();
        if (ids.isEmpty()) {
            return false;
        }
        var removed = em.createNamedQuery("deleteExpiredRevokedTokenByIds")
                .setParameter("ids", ids)
                .setParameter("currentTime", currentTime)
                .executeUpdate();
        removeCount.accept(removed);
        return ids.size() >= maxRemoval;
    }
}
