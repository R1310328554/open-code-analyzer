/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.models.sessions.infinispan.util;

import java.util.concurrent.TimeUnit;

import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureEntity;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;
import org.keycloak.models.utils.SessionExpiration;
import org.keycloak.models.utils.SessionExpirationUtils;

/**
 * 计算 Infinispan 缓存中各类会话实体的 lifespan 与 maxIdle 毫秒值。
 * <p>
 * 返回值直接用于 put/replace 操作的过期参数；负值常量表示永不过期或已过期等特殊语义。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SessionTimeouts {

    /** 条目已过期，应从缓存中移除。 */
    public static final long ENTRY_EXPIRED_FLAG = -2;

    /** 永不过期标记。 */
    public static final long IMMORTAL_FLAG = -1;

    /**
     * 获取用户会话在 Infinispan 缓存中的最大存活时间（毫秒）。
     * <p>
     * 返回值作为 put/replace 操作的 lifespan 参数。
     *
     * @param realm
     * @param client
     * @param userSessionEntity
     * @return 剩余 lifespan（毫秒），或 {@link #ENTRY_EXPIRED_FLAG}/{@link #IMMORTAL_FLAG}
     */
    public static long getUserSessionLifespanMs(RealmModel realm, ClientModel client, UserSessionEntity userSessionEntity) {
        return getUserSessionLifespanMs(realm, false, userSessionEntity.isRememberMe(), userSessionEntity.getStarted());
    }

    public static long getUserSessionLifespanMs(RealmModel realm, boolean offline, boolean rememberMe, int started) {
        long lifespan = SessionExpirationUtils.calculateUserSessionMaxLifespanTimestamp(offline, rememberMe,
                TimeUnit.SECONDS.toMillis(started), realm);
        if (offline && lifespan == IMMORTAL_FLAG) {
            return IMMORTAL_FLAG;
        }
        lifespan = lifespan - Time.currentTimeMillis();
        if (lifespan <= 0) {
            return ENTRY_EXPIRED_FLAG;
        }
        return lifespan;
    }

    /**
     * 获取用户会话的最大空闲时间（毫秒）。
     * <p>
     * 返回值作为 put/replace 操作的 maxIdleTime 参数。
     *
     * @param realm
     * @param client
     * @param userSessionEntity
     * @return 剩余 maxIdle（毫秒），或 {@link #ENTRY_EXPIRED_FLAG}
     */
    public static long getUserSessionMaxIdleMs(RealmModel realm, ClientModel client, UserSessionEntity userSessionEntity) {
        return getUserSessionMaxIdleMs(realm, false, userSessionEntity.isRememberMe(), userSessionEntity.getLastSessionRefresh());
    }

    public static long getUserSessionMaxIdleMs(RealmModel realm, boolean offline, boolean rememberMe, int lastSessionRefresh) {
        long idle = SessionExpirationUtils.calculateUserSessionIdleTimestamp(offline, rememberMe, TimeUnit.SECONDS.toMillis(lastSessionRefresh), realm);
        idle = idle - Time.currentTimeMillis();
        if (idle <= 0) {
            return ENTRY_EXPIRED_FLAG;
        }
        return idle;
    }


    /**
     * 获取客户端会话在 Infinispan 缓存中的最大存活时间（毫秒）。
     *
     * @param realm
     * @param client
     * @param clientSessionEntity
     * @return 剩余 lifespan（毫秒）
     */
    public static long getClientSessionLifespanMs(RealmModel realm, ClientModel client, AuthenticatedClientSessionEntity clientSessionEntity) {
        return getClientSessionLifespanMs(realm, client, false, clientSessionEntity.isUserSessionRememberMe(), clientSessionEntity.getStarted(), clientSessionEntity.getUserSessionStarted());
    }

    public static long getClientSessionLifespanMs(RealmModel realm, ClientModel client, boolean offline, boolean isUserSessionRememberMe, int started, int userSessionStarted) {
        long lifespan = SessionExpirationUtils.calculateClientSessionMaxLifespanTimestamp(offline, isUserSessionRememberMe,
                TimeUnit.SECONDS.toMillis(started), TimeUnit.SECONDS.toMillis(userSessionStarted), realm, client);
        if (offline && lifespan == IMMORTAL_FLAG) {
            return IMMORTAL_FLAG;
        }
        lifespan = lifespan - Time.currentTimeMillis();
        if (lifespan <= 0) {
            return ENTRY_EXPIRED_FLAG;
        }
        return lifespan;
    }


    /**
     * 获取客户端会话的最大空闲时间（毫秒）。
     *
     * @param realm
     * @param client
     * @param clientSessionEntity
     * @return 剩余 maxIdle（毫秒）
     */
    public static long getClientSessionMaxIdleMs(RealmModel realm, ClientModel client, AuthenticatedClientSessionEntity clientSessionEntity) {
        return getClientSessionMaxIdleMs(realm, client, false, clientSessionEntity.isUserSessionRememberMe(), clientSessionEntity.getTimestamp());
    }

    public static long getClientSessionMaxIdleMs(RealmModel realm, ClientModel client, boolean offline, boolean isUserSessionRememberMe, int timestamp) {
        long idle = SessionExpirationUtils.calculateClientSessionIdleTimestamp(offline, isUserSessionRememberMe,
                TimeUnit.SECONDS.toMillis(timestamp), realm, client);
        idle = idle - Time.currentTimeMillis();
        if (idle <= 0) {
            return ENTRY_EXPIRED_FLAG;
        }
        return idle;
    }


    /**
     * 获取离线用户会话的最大存活时间（毫秒）。
     *
     * @param realm
     * @param client
     * @param userSessionEntity
     * @return 剩余 lifespan（毫秒）
     */
    public static long getOfflineSessionLifespanMs(RealmModel realm, ClientModel client, UserSessionEntity userSessionEntity) {
        return getUserSessionLifespanMs(realm, true, userSessionEntity.isRememberMe(), userSessionEntity.getStarted());
    }


    /**
     * 获取离线用户会话的最大空闲时间（毫秒）。
     *
     * @param realm
     * @param client
     * @param userSessionEntity
     * @return 剩余 maxIdle（毫秒）
     */
    public static long getOfflineSessionMaxIdleMs(RealmModel realm, ClientModel client, UserSessionEntity userSessionEntity) {
        return getUserSessionMaxIdleMs(realm, true, userSessionEntity.isRememberMe(), userSessionEntity.getLastSessionRefresh());
    }

    /**
     * 获取离线客户端会话的最大存活时间（毫秒）。
     *
     * @param realm
     * @param client
     * @param authenticatedClientSessionEntity
     * @return 剩余 lifespan（毫秒）
     */
    public static long getOfflineClientSessionLifespanMs(RealmModel realm, ClientModel client, AuthenticatedClientSessionEntity authenticatedClientSessionEntity) {
        return getClientSessionLifespanMs(realm, client, true, authenticatedClientSessionEntity.isUserSessionRememberMe(), authenticatedClientSessionEntity.getStarted(), authenticatedClientSessionEntity.getUserSessionStarted());
    }

    /**
     * 获取离线客户端会话的最大空闲时间（毫秒）。
     *
     * @param realm
     * @param client
     * @param authenticatedClientSessionEntity
     * @return 剩余 maxIdle（毫秒）
     */
    public static long getOfflineClientSessionMaxIdleMs(RealmModel realm, ClientModel client, AuthenticatedClientSessionEntity authenticatedClientSessionEntity) {
        return getClientSessionMaxIdleMs(realm, client, true, authenticatedClientSessionEntity.isUserSessionRememberMe(), authenticatedClientSessionEntity.getTimestamp());
    }


    /**
     * 计算独立登录失败条目的 lifespan（不使用 maxIdle，与旧版后台清理线程行为兼容）。
     *
     * @param realm
     * @param client
     * @param loginFailureEntity
     * @return 剩余存活毫秒数，或 {@link #IMMORTAL_FLAG}
     */
    public static long getLoginFailuresLifespanMs(RealmModel realm, ClientModel client, LoginFailureEntity loginFailureEntity) {
        return getLoginFailuresLifespanMs(realm.isPermanentLockout(), realm.getMaxTemporaryLockouts(), realm.getMaxDeltaTimeSeconds() * 1000L, loginFailureEntity);
    }

    public static long getLoginFailuresLifespanMs(boolean isPermanentLockout, int maxTemporaryLockouts, long maxDeltaTimeMillis, LoginFailureEntity loginFailureEntity) {
        if (loginFailureEntity.getLastFailure() == 0) {
            // 登录失败计数已重置，立即过期该条目
            return 0;
        } else if (isPermanentLockout && maxTemporaryLockouts == 0) {
            // 纯永久锁定模式下无失败重置时间，登录失败记录永不过期
            return IMMORTAL_FLAG;
        } else {
            // 使用 realm 暴力破解检测中的“失败重置时间”：
            // 两次失败间隔超过该值则计数器重置，故可安全在此时间后从缓存驱逐
            return Math.max(0, maxDeltaTimeMillis - (Time.currentTimeMillis() - loginFailureEntity.getLastFailure()));
        }
    }


    /**
     * 独立登录失败条目不使用 maxIdle（与旧版后台清理线程行为兼容）。
     *
     * @param realm
     * @param client
     * @param loginFailureEntity
     * @return 恒为 {@link #IMMORTAL_FLAG}
     */
    public static long getLoginFailuresMaxIdleMs(RealmModel realm, ClientModel client, LoginFailureEntity loginFailureEntity) {
        return IMMORTAL_FLAG;
    }

    /** 计算根认证会话的 lifespan（毫秒）。 */
    public static long getAuthSessionLifespanMS(RealmModel realm, ClientModel client, RootAuthenticationSessionEntity entity) {
        return (entity.getTimestamp() - Time.currentTime() + SessionExpiration.getAuthSessionLifespan(realm)) * 1000L;
    }

    /** 根认证会话不使用 maxIdle。 */
    public static long getAuthSessionMaxIdleMS(RealmModel realm, ClientModel client, RootAuthenticationSessionEntity entity) {
        return IMMORTAL_FLAG;
    }

    /**
     * 计算写入 Infinispan 缓存时使用的有效 lifespan。
     * <p>
     * 因 Infinispan max-idle 实现开销较大（需跟踪最后访问时间），本方法将 maxIdle 融入 lifespan：
     * 取两者较小值作为实际 lifespan，在不启用 max-idle 跟踪的情况下保证正确过期。
     *
     * @param maxIdle  最大空闲毫秒数，或 {@link #IMMORTAL_FLAG} 表示无空闲限制
     * @param lifespan 最大寿命毫秒数，或 {@link #IMMORTAL_FLAG} 表示无寿命限制
     * @return 有效 lifespan：maxIdle 为 {@link #IMMORTAL_FLAG} 时返回 lifespan；
     *         lifespan 为 {@link #IMMORTAL_FLAG} 时返回 maxIdle；否则返回 {@code min(maxIdle, lifespan)}
     */
    public static long calculateEffectiveSessionLifespan(long maxIdle, long lifespan) {
        // currently, max-idle is never IMMORTAL_FLAG; the jvm should be able to remove the check.
        // keep it to be future-proof.
        if (maxIdle == IMMORTAL_FLAG) {
            return lifespan;
        }
        if (lifespan == IMMORTAL_FLAG) {
            return maxIdle;
        }
        return Math.min(maxIdle, lifespan);
    }
}
