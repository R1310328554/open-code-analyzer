/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.utils;

import java.util.concurrent.TimeUnit;

import org.keycloak.models.RealmModel;

/**
 * 封装 {@link RealmModel} 的会话过期配置（在线/离线、Remember Me）。
 *
 * @param lifespan 常规用户会话最大生命周期（秒）
 * @param maxIdle 常规用户会话最大空闲（秒）
 * @param offlineLifespan 离线会话最大生命周期（秒，-1 表示不限）
 * @param offlineMaxIdle 离线会话最大空闲（秒）
 * @param rememberMeLifespan Remember Me 下的常规会话生命周期（秒）
 * @param rememberMeMaxIdle Remember Me 下的常规会话最大空闲（秒）
 */
public record RealmExpiration(int lifespan,
                              int maxIdle,
                              int offlineLifespan,
                              int offlineMaxIdle,
                              int rememberMeLifespan,
                              int rememberMeMaxIdle) {

    /**
     * 返回常规会话生命周期（秒）。
     *
     * @param rememberMe 是否启用 Remember Me
     * @return 生命周期秒数
     */
    public int getLifespan(boolean rememberMe) {
        return rememberMe ? rememberMeLifespan : lifespan;
    }

    /**
     * 返回常规会话最大空闲（秒）。
     *
     * @param rememberMe 是否启用 Remember Me
     * @return 最大空闲秒数
     */
    public int getMaxIdle(boolean rememberMe) {
        return rememberMe ? rememberMeMaxIdle : maxIdle;
    }

    /**
     * 计算离线会话因最大生命周期而失效的时间戳（毫秒）。
     *
     * @param created 会话创建时间戳（毫秒）
     * @return 失效时间戳；{@code -1} 表示永不过期
     */
    public long calculateOfflineLifespanTimestamp(long created) {
        return offlineLifespan == -1 ? -1 : created + TimeUnit.SECONDS.toMillis(offlineLifespan);
    }

    /**
     * 计算常规会话因最大生命周期而失效的时间戳（毫秒）。
     *
     * @param created 会话创建时间戳（毫秒）
     * @param rememberMe 是否 Remember Me
     * @return 失效时间戳（毫秒）
     */
    public long calculateRegularLifespanTimestamp(long created, boolean rememberMe) {
        return created + TimeUnit.SECONDS.toMillis(getLifespan(rememberMe));
    }

    /**
     * 计算离线会话因最大空闲而失效的时间戳（毫秒）。
     *
     * @param lastRefresh 上次刷新时间戳（毫秒）
     * @return 失效时间戳（毫秒）
     */
    public long calculateOfflineMaxIdleTimestamp(long lastRefresh) {
        return lastRefresh + TimeUnit.SECONDS.toMillis(offlineMaxIdle);
    }

    /**
     * 计算常规会话因最大空闲而失效的时间戳（毫秒）。
     *
     * @param lastRefresh 上次刷新时间戳（毫秒）
     * @param rememberMe 是否 Remember Me
     * @return 失效时间戳（毫秒）
     */
    public long calculateRegularMaxIdleTimestamp(long lastRefresh, boolean rememberMe) {
        return lastRefresh + TimeUnit.SECONDS.toMillis(getMaxIdle(rememberMe));
    }

    /**
     * 从 {@link RealmModel} 读取过期配置并构造 {@link RealmExpiration}。
     *
     * @param realm realm
     * @return 新的 {@link RealmExpiration}
     */
    public static RealmExpiration fromRealm(RealmModel realm) {
        int offlineMaxIdle = SessionExpirationUtils.getOfflineSessionIdleTimeout(realm);
        int offlineLifespan = realm.isOfflineSessionMaxLifespanEnabled() ? SessionExpirationUtils.getOfflineSessionMaxLifespan(realm) : -1;
        int maxIdle = SessionExpirationUtils.getSsoSessionIdleTimeout(realm);
        int lifespan = SessionExpirationUtils.getSsoSessionMaxLifespan(realm);
        int maxIdleRememberMe = Math.max(maxIdle, realm.getSsoSessionIdleTimeoutRememberMe());
        int lifespanRememberMe = Math.max(lifespan, realm.getSsoSessionMaxLifespanRememberMe());
        return new RealmExpiration(lifespan, maxIdle, offlineLifespan, offlineMaxIdle, lifespanRememberMe, maxIdleRememberMe);
    }

}
