/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.grants.device;


import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.grants.OAuth2GrantType;
import org.keycloak.protocol.oidc.grants.OAuth2GrantTypeFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * OAuth 2.0 设备授权许可（Device Authorization Grant）工厂。
 * <p>在 DEVICE_FLOW 特性启用时注册 {@link DeviceGrantType} 为 OAuth2 授权类型。</p>
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public class DeviceGrantTypeFactory implements OAuth2GrantTypeFactory, EnvironmentDependentProviderFactory {

    /** 授权类型快捷标识 */
    public static final String GRANT_SHORTCUT = "dg";

    /** {@inheritDoc} 返回 urn:ietf:params:oauth:grant-type:device_code */
    @Override
    public String getId() {
        return OAuth2Constants.DEVICE_CODE_GRANT_TYPE;
    }

    /** {@inheritDoc} 返回 {@link #GRANT_SHORTCUT} */
    @Override
    public String getShortcut() {
        return GRANT_SHORTCUT;
    }

    /** {@inheritDoc} 创建设备授权许可处理器 */
    @Override
    public OAuth2GrantType create(KeycloakSession session) {
        return new DeviceGrantType();
    }

    /** {@inheritDoc} 需启用 DEVICE_FLOW 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.DEVICE_FLOW);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}
