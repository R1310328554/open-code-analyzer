/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.utils.StringUtil;

/**
 * 用户/客户端会话过期与空闲时间计算的共享工具。
 * <p>综合 realm 与客户端 OIDC 属性及 {@link RealmExpiration} 计算失效时间戳。</p>
 *
 * @author rmartinc
 */
public class SessionExpirationUtils {

    /**
     * 按最大生命周期配置计算用户会话失效时间戳（毫秒）。
     * @param offline 是否离线会话
     * @param isRememberMe 是否 Remember Me
     * @param created 创建时间戳
     * @param realm realm
     * @return 失效时间戳；{@code -1} 表示不过期
     */
    public static long calculateUserSessionMaxLifespanTimestamp(boolean offline, boolean isRememberMe, long created, RealmModel realm) {
        RealmExpiration expiration = RealmExpiration.fromRealm(realm);
        return offline ?
                expiration.calculateOfflineLifespanTimestamp(created) :
                expiration.calculateRegularLifespanTimestamp(created, isRememberMe);
    }

    /**
     * 按空闲配置计算用户会话因空闲而失效的时间戳（毫秒）。
     * @param offline 是否离线会话
     * @param isRememberMe 是否 Remember Me
     * @param lastRefreshed 上次刷新时间
     * @param realm realm
     * @return 空闲失效时间戳
     */
    public static long calculateUserSessionIdleTimestamp(boolean offline, boolean isRememberMe, long lastRefreshed, RealmModel realm) {
        RealmExpiration expiration = RealmExpiration.fromRealm(realm);
        return offline ?
                expiration.calculateOfflineMaxIdleTimestamp(lastRefreshed) :
                expiration.calculateRegularMaxIdleTimestamp(lastRefreshed, isRememberMe);
    }

    /**
     * Calculates the time in which the client session is expired via lifespan
     * configuration in the realm and client.
     * @param offline is the session offline?
     * @param isRememberMe is the session remember me?
     * @param clientSessionCreated timestamp when the client session was created
     * @param userSessionCreated timestamp when the user session was created
     * @param realm The realm model
     * @param client The client model
     * @return The time when the client session is expired or -1 if does not expire
     */
    public static long calculateClientSessionMaxLifespanTimestamp(boolean offline, boolean isRememberMe,
            long clientSessionCreated, long userSessionCreated, RealmModel realm, ClientModel client) {
        long timestamp = -1;
        if (offline) {
            long clientOfflineSessionMaxLifespan = getClientAttributeTimeout(client, OIDCConfigAttributes.CLIENT_OFFLINE_SESSION_MAX_LIFESPAN);
            if (realm.isOfflineSessionMaxLifespanEnabled() || clientOfflineSessionMaxLifespan > 0) {
                if (clientOfflineSessionMaxLifespan > 0) {
                    clientOfflineSessionMaxLifespan = TimeUnit.SECONDS.toMillis(clientOfflineSessionMaxLifespan);
                } else if (realm.getClientOfflineSessionMaxLifespan() > 0) {
                    clientOfflineSessionMaxLifespan = TimeUnit.SECONDS.toMillis(realm.getClientOfflineSessionMaxLifespan());
                } else {
                    clientOfflineSessionMaxLifespan = TimeUnit.SECONDS.toMillis(getOfflineSessionMaxLifespan(realm));
                }
                timestamp = clientSessionCreated + clientOfflineSessionMaxLifespan;

                long userSessionExpires = calculateUserSessionMaxLifespanTimestamp(offline, isRememberMe, userSessionCreated, realm);

                timestamp = userSessionExpires > 0? Math.min(timestamp, userSessionExpires) : timestamp;
            }
        } else {
            long clientSessionMaxLifespan = TimeUnit.SECONDS.toMillis(getSsoSessionMaxLifespan(realm));
            if (isRememberMe) {
                clientSessionMaxLifespan = Math.max(clientSessionMaxLifespan, TimeUnit.SECONDS.toMillis(realm.getSsoSessionMaxLifespanRememberMe()));
            }
            long clientSessionMaxLifespanPerClient = getClientAttributeTimeout(client, OIDCConfigAttributes.CLIENT_SESSION_MAX_LIFESPAN);
            if (clientSessionMaxLifespanPerClient > 0) {
                clientSessionMaxLifespan = TimeUnit.SECONDS.toMillis(clientSessionMaxLifespanPerClient);
            } else if (realm.getClientSessionMaxLifespan() > 0) {
                clientSessionMaxLifespan = TimeUnit.SECONDS.toMillis(realm.getClientSessionMaxLifespan());
            }

            timestamp = clientSessionCreated + clientSessionMaxLifespan;

            long userSessionExpires = calculateUserSessionMaxLifespanTimestamp(offline, isRememberMe, userSessionCreated, realm);

            timestamp = Math.min(timestamp, userSessionExpires);
        }
        return timestamp;
    }

    /**
     * Calculates the time in which the user session is expired via the idle
     * configuration in the realm and client.
     * @param offline is the session offline?
     * @param isRememberMe is the session remember me?
     * @param lastRefreshed the last time the client session was refreshed
     * @param realm the realm model
     * @param client the client model
     * @return The time in which the client session is expired by idle timeout
     */
    public static long calculateClientSessionIdleTimestamp(boolean offline, boolean isRememberMe, long lastRefreshed,
            RealmModel realm, ClientModel client) {
        long timestamp;
        if (offline) {
            long clientOfflineSessionIdleTimeout = TimeUnit.SECONDS.toMillis(getOfflineSessionIdleTimeout(realm));
            long clientOfflineSessionIdleTimeoutPerClient = getClientAttributeTimeout(client, OIDCConfigAttributes.CLIENT_OFFLINE_SESSION_IDLE_TIMEOUT);
            if (clientOfflineSessionIdleTimeoutPerClient > 0) {
                clientOfflineSessionIdleTimeout = TimeUnit.SECONDS.toMillis(clientOfflineSessionIdleTimeoutPerClient);
            } else if (realm.getClientOfflineSessionIdleTimeout() > 0) {
                clientOfflineSessionIdleTimeout = TimeUnit.SECONDS.toMillis(realm.getClientOfflineSessionIdleTimeout());
            }

            timestamp = lastRefreshed + clientOfflineSessionIdleTimeout;
        } else {
            long clientSessionIdleTimeout = TimeUnit.SECONDS.toMillis(getSsoSessionIdleTimeout(realm));
            if (isRememberMe) {
                clientSessionIdleTimeout = Math.max(clientSessionIdleTimeout, TimeUnit.SECONDS.toMillis(realm.getSsoSessionIdleTimeoutRememberMe()));
            }
            long clientSessionIdleTimeoutPerClient = getClientAttributeTimeout(client, OIDCConfigAttributes.CLIENT_SESSION_IDLE_TIMEOUT);
            if (clientSessionIdleTimeoutPerClient > 0) {
                clientSessionIdleTimeout = TimeUnit.SECONDS.toMillis(clientSessionIdleTimeoutPerClient);
            } else if (realm.getClientSessionIdleTimeout() > 0){
                clientSessionIdleTimeout = TimeUnit.SECONDS.toMillis(realm.getClientSessionIdleTimeout());
            }

            timestamp = lastRefreshed + clientSessionIdleTimeout;
        }
        return timestamp;
    }

    /** 获取 SSO 会话最大生命周期（秒），未配置时用默认值。 */
    public static int getSsoSessionMaxLifespan(RealmModel realm) {
        int lifespan = realm.getSsoSessionMaxLifespan();
        if (lifespan <= 0) {
            lifespan = Constants.DEFAULT_SESSION_MAX_LIFESPAN;
        }
        return lifespan;
    }

    /** 获取离线会话最大生命周期（秒），未配置时用默认值。 */
    public static int getOfflineSessionMaxLifespan(RealmModel realm) {
        int lifespan = realm.getOfflineSessionMaxLifespan();
        if (lifespan <= 0) {
            lifespan = Constants.DEFAULT_OFFLINE_SESSION_MAX_LIFESPAN;
        }
        return lifespan;
    }

    /** 获取 SSO 会话最大空闲（秒），未配置时用默认值。 */
    public static int getSsoSessionIdleTimeout(RealmModel realm) {
        int idle = realm.getSsoSessionIdleTimeout();
        if (idle <= 0) {
            idle = Constants.DEFAULT_SESSION_IDLE_TIMEOUT;
        }
        return idle;
    }

    /** 获取离线会话最大空闲（秒），未配置时用默认值。 */
    public static int getOfflineSessionIdleTimeout(RealmModel realm) {
        int idle = realm.getOfflineSessionIdleTimeout();
        if (idle <= 0) {
            idle = Constants.DEFAULT_OFFLINE_SESSION_IDLE_TIMEOUT;
        }
        return idle;
    }

    private static long getClientAttributeTimeout(ClientModel client, String attr) {
        if (client != null) {
            final String value = client.getAttribute(attr);
            if (StringUtil.isNotBlank(value)) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    // no-op
                }
            }
        }
        return -1;
    }
}
