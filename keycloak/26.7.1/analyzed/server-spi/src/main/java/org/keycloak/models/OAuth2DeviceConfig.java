/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.models;

import java.io.Serializable;

/**
 * OAuth 2.0 设备授权流配置：设备码生命周期与轮询间隔（Realm/客户端属性）。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class OAuth2DeviceConfig implements Serializable {

    /** 默认设备码有效期（秒，10 分钟）。 */
    // 10 minutes
    public static final int DEFAULT_OAUTH2_DEVICE_CODE_LIFESPAN = 600;
    /** 默认轮询间隔（秒）。 */
    // 5 seconds
    public static final int DEFAULT_OAUTH2_DEVICE_POLLING_INTERVAL = 5;

    /** Realm 属性：设备码有效期键名。 */
    // realm attribute names
    public static String OAUTH2_DEVICE_CODE_LIFESPAN = "oauth2DeviceCodeLifespan";
    /** Realm 属性：轮询间隔键名。 */
    public static String OAUTH2_DEVICE_POLLING_INTERVAL = "oauth2DevicePollingInterval";

    /** 客户端属性：设备码有效期键名。 */
    // client attribute names
    public static String OAUTH2_DEVICE_CODE_LIFESPAN_PER_CLIENT = "oauth2.device.code.lifespan";
    /** 客户端属性：轮询间隔键名。 */
    public static String OAUTH2_DEVICE_POLLING_INTERVAL_PER_CLIENT = "oauth2.device.polling.interval";
    /** 客户端属性：是否启用设备授权 grant。 */
    public static final String OAUTH2_DEVICE_AUTHORIZATION_GRANT_ENABLED = "oauth2.device.authorization.grant.enabled";

    private int lifespan = DEFAULT_OAUTH2_DEVICE_CODE_LIFESPAN;
    private int poolingInterval = DEFAULT_OAUTH2_DEVICE_POLLING_INTERVAL;

    /** 从 Realm 属性加载设备流配置。
     * @param realm Realm 模型 */
    public OAuth2DeviceConfig(RealmModel realm) {
        String lifespan = realm.getAttribute(OAUTH2_DEVICE_CODE_LIFESPAN);

        if (lifespan != null && !lifespan.trim().isEmpty()) {
            setOAuth2DeviceCodeLifespan(Integer.parseInt(lifespan));
        }

        String pooling = realm.getAttribute(OAUTH2_DEVICE_POLLING_INTERVAL);

        if (pooling != null && !pooling.trim().isEmpty()) {
            setOAuth2DevicePollingInterval(Integer.parseInt(pooling));
        }
    }

    /** @return Realm 级设备码有效期（秒） */
    public int getLifespan() {
        return lifespan;
    }

    /** @param seconds 设备码有效期（秒） */
    public void setOAuth2DeviceCodeLifespan(Integer seconds) {
        setOAuth2DeviceCodeLifespan(null, seconds);
    }

    /** 设置并持久化 Realm 设备码有效期。
     * @param realm Realm（可为 null 仅改内存）
     * @param seconds 秒数 */
    public void setOAuth2DeviceCodeLifespan(RealmModel realm, Integer seconds) {
        if (seconds == null) {
            seconds = DEFAULT_OAUTH2_DEVICE_CODE_LIFESPAN;
        }
        this.lifespan = seconds;
        persistRealmAttribute(realm, OAUTH2_DEVICE_CODE_LIFESPAN, lifespan);
    }

    /** @return Realm 级轮询间隔（秒） */
    public int getPoolingInterval() {
        return poolingInterval;
    }

    /** @param seconds 轮询间隔（秒） */
    public void setOAuth2DevicePollingInterval(Integer seconds) {
        setOAuth2DevicePollingInterval(null, seconds);
    }

    /** 设置并持久化 Realm 轮询间隔。
     * @param realm Realm
     * @param seconds 秒数 */
    public void setOAuth2DevicePollingInterval(RealmModel realm, Integer seconds) {
        if (seconds == null) {
            seconds = DEFAULT_OAUTH2_DEVICE_POLLING_INTERVAL;
        }
        this.poolingInterval = seconds;

        persistRealmAttribute(realm, OAUTH2_DEVICE_POLLING_INTERVAL, poolingInterval);
    }

    /** @param client 客户端（可覆盖 Realm 默认值）
     * @return 有效设备码有效期（秒） */
    public int getLifespan(ClientModel client) {
        String lifespan = client.getAttribute(OAUTH2_DEVICE_CODE_LIFESPAN_PER_CLIENT);

        if (lifespan != null && !lifespan.trim().isEmpty()) {
            return Integer.parseInt(lifespan);
        }

        return getLifespan();
    }

    /** @param client 客户端
     * @return 有效轮询间隔（秒） */
    public int getPoolingInterval(ClientModel client) {
        String interval = client.getAttribute(OAUTH2_DEVICE_POLLING_INTERVAL_PER_CLIENT);

        if (interval != null && !interval.trim().isEmpty()) {
            return Integer.parseInt(interval);
        }

        return getPoolingInterval();
    }

    /** @param client 客户端
     * @return 是否启用 OAuth2 设备授权 grant */
    public boolean isOAuth2DeviceAuthorizationGrantEnabled(ClientModel client) {
        String enabled = client.getAttribute(OAUTH2_DEVICE_AUTHORIZATION_GRANT_ENABLED);
        return Boolean.parseBoolean(enabled);
    }

    private void persistRealmAttribute(RealmModel realm, String name, Integer value) {
        if (realm != null) {
            realm.setAttribute(name, value);
        }
    }
}
