/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.credentialbuilder;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 注册 {@link CredentialBuilder} 提供者类型的 SPI 实现。
 * <p>SPI 名称为 {@code credentialBuilder}，内部 SPI，不对扩展模块公开。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class CredentialBuilderSpi implements Spi {
    /** SPI 注册名称。 */
    private static final String NAME = "credentialBuilder";

    /** {@inheritDoc} 内部 SPI。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** {@inheritDoc} 返回 {@code credentialBuilder}。 */
    @Override
    public String getName() {
        return NAME;
    }

    /** {@inheritDoc} 提供者接口为 {@link CredentialBuilder}。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return CredentialBuilder.class;
    }

    /** {@inheritDoc} 工厂接口为 {@link CredentialBuilderFactory}。 */
    @Override
    public Class<? extends ProviderFactory<CredentialBuilder>> getProviderFactoryClass() {
        return CredentialBuilderFactory.class;
    }
}
