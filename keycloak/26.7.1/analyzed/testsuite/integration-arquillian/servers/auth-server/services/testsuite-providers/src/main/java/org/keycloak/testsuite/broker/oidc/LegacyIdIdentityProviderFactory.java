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
 * {@link LegacyIdIdentityProvider} 的 SPI 工厂，用于注册测试用 legacy ID 身份提供者。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class LegacyIdIdentityProviderFactory extends OIDCIdentityProviderFactory {

    /** 提供商标识符，与 {@link #getId()} 返回值一致。 */
    public static final String PROVIDER_ID = "legacy-id-idp";

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} 作为显示名称。 */
    @Override
    public String getName() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 创建 {@link LegacyIdIdentityProvider} 实例。 */
    @Override
    public KeycloakOIDCIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new LegacyIdIdentityProvider(session, new OIDCIdentityProviderConfig(model));
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
