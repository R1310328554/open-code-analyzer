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

package org.keycloak.broker.trust;

import java.util.Map;

import org.keycloak.Config;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * 默认信任材料 IdP 工厂：在 OID4VC VCI 或 CLIENT_AUTH_ABCA 特性启用时注册。
 */
public class DefaultTrustIdentityProviderFactory extends AbstractIdentityProviderFactory<DefaultTrustIdentityProvider> implements EnvironmentDependentProviderFactory {

    /** 默认信任 IdP provider id。 */
    public static final String PROVIDER_ID = "default-trust";

    /** @return 控制台显示名称 Default Trust */
    @Override
    public String getName() {
        return "Default Trust";
    }

    /** 创建 {@link DefaultTrustIdentityProvider} 实例。 */
    @Override
    public DefaultTrustIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new DefaultTrustIdentityProvider(session, new DefaultTrustIdentityProviderConfig(model));
    }

    /** 不支持字符串配置解析。 */
    @Override
    public Map<String, String> parseConfig(KeycloakSession session, String config) {
        throw new UnsupportedOperationException();
    }

    /** @return 空 {@link DefaultTrustIdentityProviderConfig} */
    @Override
    public IdentityProviderModel createConfig() {
        return new DefaultTrustIdentityProviderConfig();
    }

    /** @return provider id {@value #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** OID4VC VCI 或 ABCA 任一特性启用时可用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.OID4VC_VCI) || Profile.isFeatureEnabled(Profile.Feature.CLIENT_AUTH_ABCA);
    }
}
