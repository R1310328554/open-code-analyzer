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

package org.keycloak.authentication.jpa;

import java.util.function.IntConsumer;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.expiration.jpa.ExpirationAction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.SessionExpiration;

/** 按域清理过期的根认证会话及其子会话的 {@link ExpirationAction} 实现。 */
enum AuthenticationSessionExpirationAction implements ExpirationAction {
    INSTANCE;

    /** 查询并批量删除超出 lifespan 的根认证会话，返回是否可能还有更多待清理条目。 */
    @Override
    public boolean removeExpired(KeycloakSession session, String realmId, int currentTime, int maxRemoval, IntConsumer removeCount) {
        var realm = session.realms().getRealm(realmId);
        if (realm == null) {
            return false;
        }
        session.getContext().setRealm(realm);
        var lifespan = SessionExpiration.getAuthSessionLifespan(realm);
        var olderTimestamp = currentTime - lifespan;
        var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        var ids = em.createNamedQuery("findExpiredRootAuthSessionIdsByRealm", String.class)
                .setParameter("realmId", realmId)
                .setParameter("timestamp", olderTimestamp)
                .setMaxResults(maxRemoval)
                .getResultList();
        if (ids.isEmpty()) {
            return false;
        }
        var removed = em.createNamedQuery("deleteExpiredRootAuthSessionByIds")
                .setParameter("ids", ids)
                .setParameter("timestamp", olderTimestamp)
                .executeUpdate();
        removeCount.accept(removed);
        return ids.size() >= maxRemoval;
    }
}
