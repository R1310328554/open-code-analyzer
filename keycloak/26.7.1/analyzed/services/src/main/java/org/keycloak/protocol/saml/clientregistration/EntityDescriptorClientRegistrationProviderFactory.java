/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.saml.clientregistration;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.ClientRegistrationProviderFactory;

/**
 * {@link EntityDescriptorClientRegistrationProvider} 的 SPI 工厂。
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EntityDescriptorClientRegistrationProviderFactory implements ClientRegistrationProviderFactory {

    /** SPI 提供者 ID：saml2-entity-descriptor */
    public static final String ID = "saml2-entity-descriptor";

    /** {@inheritDoc} 创建 EntityDescriptor 注册提供者实例 */
    @Override
    public ClientRegistrationProvider create(KeycloakSession session) {
        return new EntityDescriptorClientRegistrationProvider(session);
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

    /** {@inheritDoc} 返回 {@link #ID} */
    @Override
    public String getId() {
        return ID;
    }

}
