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

package org.keycloak.loginfailures.jpa;

import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.expiration.jpa.ExpirationAction;
import org.keycloak.models.KeycloakSession;

/**
 * 登录失败记录的过期删除策略（{@link ExpirationAction} 单例）。
 * <p>
 * 过期判定：{@code lastFailure < currentTime - realm.maxDeltaTimeSeconds}（毫秒）。
 * 永久锁定且不允许临时锁定的 realm 跳过清理（失败计数需长期保留）。
 */
public enum LoginFailureExpirationAction implements ExpirationAction {
    INSTANCE;

    /**
     * 在指定 realm 内查找并删除一批过期失败记录。
     *
     * @return {@code true} 若本批命中 {@code maxRemoval} 上限，调用方应继续下一批
     */
    @Override
    public boolean removeExpired(KeycloakSession session, String realmId, int currentTime, int maxRemoval, IntConsumer removeCount) {
        var realm = session.realms().getRealm(realmId);
        if (realm == null) {
            return false;
        }
        if (realm.isPermanentLockout() && realm.getMaxTemporaryLockouts() == 0) {
            // 纯永久锁定模式无法配置 failure reset time，登录失败记录不应过期
            return false;
        }
        // 过期阈值：lastFailure 早于 (currentTime - maxDeltaTimeSeconds)
        var expired = TimeUnit.SECONDS.toMillis(currentTime - realm.getMaxDeltaTimeSeconds());
        var em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        var userIds = em.createNamedQuery("findExpiredLoginFailureUserIdsByRealm", String.class)
                .setParameter("realmId", realmId)
                .setParameter("expire", expired)
                .setMaxResults(maxRemoval)
                .getResultList();
        if (userIds.isEmpty()) {
            return false;
        }
        var removed = em.createNamedQuery("deleteExpiredLoginFailureByRealmAndUserIds")
                .setParameter("realmId", realmId)
                .setParameter("userIds", userIds)
                .setParameter("expire", expired)
                .executeUpdate();
        removeCount.accept(removed);
        return userIds.size() >= maxRemoval;
    }
}
