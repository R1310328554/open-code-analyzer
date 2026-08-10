/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models;

import java.util.Map;
import java.util.function.Supplier;

import org.keycloak.utils.StringUtil;

/**
 * PAR（推送授权请求）配置：管理 request_uri 生命周期及客户端 PAR 要求。
 */
public class ParConfig extends AbstractConfig {

    // Realm 属性名
    /** PAR request_uri 生命周期（秒）属性键。 */
    public static final String PAR_REQUEST_URI_LIFESPAN = "parRequestUriLifespan";

    /** PAR request_uri 默认生命周期（秒）。 */
    public static final int DEFAULT_PAR_REQUEST_URI_LIFESPAN = 60; // sec

    private int requestUriLifespan;

    // 客户端属性名
    /** 是否要求推送授权请求的客户端属性键。 */
    public static final String REQUIRE_PUSHED_AUTHORIZATION_REQUESTS = "require.pushed.authorization.requests";

    /**
     * 已弃用：请使用 {@link #fromCache(Supplier, Map)} 或 {@link #fromModel(RealmModel)} 工厂方法。
     * @deprecated use {@link #fromCache(Supplier, Map)} or {@link #fromModel(RealmModel)} factory methods
     */
    @Deprecated(since = "26.6", forRemoval = true)
    public ParConfig(RealmModel realm) {
        this.requestUriLifespan = realm.getAttribute(PAR_REQUEST_URI_LIFESPAN, DEFAULT_PAR_REQUEST_URI_LIFESPAN);

        this.realmForWrite = () -> realm;
    }

    private ParConfig(Supplier<RealmModel> realmForWrite, int requestUriLifespan) {
        this.requestUriLifespan = requestUriLifespan;
        this.realmForWrite = realmForWrite;
    }

    /** @param realm Realm
     * @return 从 Realm 模型构建的 PAR 配置 */
    public static ParConfig fromModel(RealmModel realm) {
        var requestUriLifespan = realm.getAttribute(PAR_REQUEST_URI_LIFESPAN, DEFAULT_PAR_REQUEST_URI_LIFESPAN);
        return new ParConfig(() -> realm, requestUriLifespan);
    }

    /** @param realmForWrite 写操作 Realm 供应器
     * @param realmAttributes Realm 属性缓存
     * @return 从缓存属性构建的 PAR 配置 */
    public static ParConfig fromCache(Supplier<RealmModel> realmForWrite, Map<String, String> realmAttributes) {
        var requestUriLifespan = getIntAttribute(realmAttributes, PAR_REQUEST_URI_LIFESPAN, DEFAULT_PAR_REQUEST_URI_LIFESPAN);
        return new ParConfig(realmForWrite, requestUriLifespan);
    }

    /** @return PAR request_uri 生命周期（秒） */
    public int getRequestUriLifespan() {
        return requestUriLifespan;
    }

    /** @param requestUriLifespan 生命周期字符串 */
    public void setRequestUriLifespan(String requestUriLifespan) {
        if (StringUtil.isBlank(requestUriLifespan)) {
            setRequestUriLifespan((Integer) null);
        } else {
            setRequestUriLifespan(Integer.parseInt(requestUriLifespan));
        }
    }

    /** @param requestUriLifespan 生命周期（秒），null 时使用默认值 */
    public void setRequestUriLifespan(Integer requestUriLifespan) {
        if (requestUriLifespan == null) {
            requestUriLifespan = DEFAULT_PAR_REQUEST_URI_LIFESPAN;
        }
        this.requestUriLifespan = requestUriLifespan;
        persistRealmAttribute(PAR_REQUEST_URI_LIFESPAN, requestUriLifespan);
    }

    /** @param client 客户端
     * @return 是否要求推送授权请求 */
    public boolean isRequirePushedAuthorizationRequests(ClientModel client) {
        String enabled = client.getAttribute(REQUIRE_PUSHED_AUTHORIZATION_REQUESTS);
        return Boolean.parseBoolean(enabled);
    }
}
