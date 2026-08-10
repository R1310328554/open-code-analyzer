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

package org.keycloak.protocol.oidc.grants.ciba;

import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.grants.OAuth2GrantType;
import org.keycloak.protocol.oidc.grants.OAuth2GrantTypeFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * OpenID Connect CIBA 模式工厂。
 * <p>仅在 {@link Profile.Feature#CIBA} 特性启用时加载。</p>
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public class CibaGrantTypeFactory implements OAuth2GrantTypeFactory, EnvironmentDependentProviderFactory {

    /** 授权类型快捷标识 {@code ci} */
    public static final String GRANT_SHORTCUT = "ci";

    /** @return grant_type 值 {@link OAuth2Constants#CIBA_GRANT_TYPE} */
    @Override
    public String getId() {
        return OAuth2Constants.CIBA_GRANT_TYPE;
    }

    /** @return 快捷标识 {@link #GRANT_SHORTCUT} */
    @Override
    public String getShortcut() {
        return GRANT_SHORTCUT;
    }

    /** @param session Keycloak 会话 @return 新的 {@link CibaGrantType} 实例 */
    @Override
    public OAuth2GrantType create(KeycloakSession session) {
        return new CibaGrantType();
    }

    /** @return 是否启用 CIBA 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CIBA);
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
