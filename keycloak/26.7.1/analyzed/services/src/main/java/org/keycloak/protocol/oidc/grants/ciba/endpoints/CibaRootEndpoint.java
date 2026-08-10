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
 *
 */
package org.keycloak.protocol.oidc.grants.ciba.endpoints;

import jakarta.ws.rs.Path;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.ext.OIDCExtProvider;
import org.keycloak.protocol.oidc.ext.OIDCExtProviderFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * CIBA OIDC 扩展根端点。
 * <p>挂载后台认证与认证回调子路径，仅在 {@link Profile.Feature#CIBA} 启用时可用。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class CibaRootEndpoint implements OIDCExtProvider, OIDCExtProviderFactory, EnvironmentDependentProviderFactory {

    /** OIDC 扩展提供方标识 */
    public static final String PROVIDER_ID = "ciba";

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 事件构建器 */
    private EventBuilder event;

    /** 无参构造，供反射使用 */
    public CibaRootEndpoint() {
        // 供反射实例化
        this(null);
    }

    /** @param session Keycloak 会话 */
    public CibaRootEndpoint(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 后台认证端点：消费设备通过此路径向最终用户发起后台认证。
     * @return {@link BackchannelAuthenticationEndpoint} 实例
     */
    @Path("/auth")
    public BackchannelAuthenticationEndpoint authorize() {
        return new BackchannelAuthenticationEndpoint(session, event);
    }

    /**
     * 认证回调端点：认证设备通过此路径通知 Keycloak 用户认证结果。
     * @return {@link BackchannelAuthenticationCallbackEndpoint} 实例
     */
    @Path("/auth/callback")
    public BackchannelAuthenticationCallbackEndpoint authenticate() {
        return new BackchannelAuthenticationCallbackEndpoint(session, event);
    }

    /** @param session Keycloak 会话 @return 新的 CIBA 根端点实例 */
    @Override
    public OIDCExtProvider create(KeycloakSession session) {
        return new CibaRootEndpoint(session);
    }

    /** @return 提供方标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 注入事件构建器供子端点使用 @param event 事件构建器 */
    @Override
    public void setEvent(EventBuilder event) {
        this.event = event;
    }

    @Override
    public void close() {

    }

    /** @return 是否启用 CIBA 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CIBA);
    }

}
