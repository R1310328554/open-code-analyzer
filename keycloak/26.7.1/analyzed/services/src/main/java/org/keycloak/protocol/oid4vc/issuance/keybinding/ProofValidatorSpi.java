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

package org.keycloak.protocol.oid4vc.issuance.keybinding;

import org.keycloak.provider.Spi;

/**
 * 注册 {@link ProofValidator} 及其工厂的 SPI 实现。
 * <p>Provider 名称为 {@code proofValidator}，标记为内部 SPI。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class ProofValidatorSpi implements Spi {
    /** SPI 注册名称。 */
    private static final String NAME = "proofValidator";

    /** {@inheritDoc} 返回 {@code true}，表示内部 SPI。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** {@inheritDoc} 返回 {@link #NAME}。 */
    @Override
    public String getName() {
        return NAME;
    }

    /** {@inheritDoc} 返回 {@link ProofValidator} 类型。 */
    @Override
    public Class<ProofValidator> getProviderClass() {
        return ProofValidator.class;
    }

    /** {@inheritDoc} 返回 {@link ProofValidatorFactory} 类型。 */
    @Override
    public Class<ProofValidatorFactory> getProviderFactoryClass() {
        return ProofValidatorFactory.class;
    }
}
