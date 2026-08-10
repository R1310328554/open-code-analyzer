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

package org.keycloak.protocol.oidc.par.endpoints;

import jakarta.ws.rs.Path;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.ext.OIDCExtProvider;
import org.keycloak.protocol.oidc.ext.OIDCExtProviderFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * PAR OIDC 扩展根端点。
 * <p>注册 {@code par} 扩展并在启用 PAR 特性时挂载 {@link ParEndpoint} 子资源。</p>
 */
public class ParRootEndpoint implements OIDCExtProvider, OIDCExtProviderFactory, EnvironmentDependentProviderFactory {

    /** OIDC 扩展提供方标识 */
    public static final String PROVIDER_ID = "par";

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 事件构建器（由框架注入） */
    private EventBuilder event;

    /** 无参构造，供反射实例化 */
    public ParRootEndpoint() {
        // 供反射调用
        this(null);
    }

    /** @param session Keycloak 会话 */
    public ParRootEndpoint(KeycloakSession session) {
        this.session = session;
    }

    /** @return PAR 请求子端点 */
    @Path("/request")
    public ParEndpoint request() {
        return new ParEndpoint(session, event);
    }

    /** @param session Keycloak 会话 @return PAR 扩展提供者实例 */
    @Override
    public OIDCExtProvider create(KeycloakSession session) {
        return new ParRootEndpoint(session);
    }

    /** @return 扩展标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 是否支持：需启用 PAR 特性 @param config 配置作用域 @return 是否可用 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.PAR);
    }

    /** 注入事件构建器 @param event 事件构建器 */
    @Override
    public void setEvent(EventBuilder event) {
        this.event = event;
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {
    }

}
