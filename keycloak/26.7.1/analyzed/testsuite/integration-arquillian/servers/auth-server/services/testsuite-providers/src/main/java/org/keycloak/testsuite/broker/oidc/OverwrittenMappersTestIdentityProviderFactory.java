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
 */

package org.keycloak.testsuite.broker.oidc;

import org.keycloak.broker.oidc.KeycloakOIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

/**
 * {@link OverwrittenMappersTestIdentityProvider} 的 SPI 工厂，用于映射器覆盖行为测试。
 *
 * @author Daniel Fesenmeyer <daniel.fesenmeyer@bosch.com>
 */
public class OverwrittenMappersTestIdentityProviderFactory extends OIDCIdentityProviderFactory {

    /** 提供商标识符。 */
    public static final String PROVIDER_ID = "overwritten-mappers-test-id-idp";

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} 作为显示名称。 */
    @Override
    public String getName() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 创建 {@link OverwrittenMappersTestIdentityProvider} 实例。 */
    @Override
    public KeycloakOIDCIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new OverwrittenMappersTestIdentityProvider(session, new OIDCIdentityProviderConfig(model));
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
