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
package org.keycloak.protocol.oid4vc.issuance.credentialoffer;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * {@link DefaultCredentialOfferProvider} 的 Provider 工厂。
 * <p>提供 {@link CredentialOfferProvider} 的默认实现，Provider ID 为 {@code default}。</p>
 *
 * @author <a href="mailto:tdiesler@proton.me">Thomas Diesler</a>
 */
public class DefaultCredentialOfferProviderFactory implements CredentialOfferProviderFactory {

    /** {@inheritDoc} 创建默认凭证发放 Provider。 */
    @Override
    public CredentialOfferProvider create(KeycloakSession session) {
        return new DefaultCredentialOfferProvider(session);
    }

    /** {@inheritDoc} 无额外初始化。 */
    @Override
    public void init(Config.Scope config) {
    }

    /** {@inheritDoc} 无后置初始化。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 返回 {@code default}。 */
    @Override
    public String getId() {
        return "default";
    }
}
