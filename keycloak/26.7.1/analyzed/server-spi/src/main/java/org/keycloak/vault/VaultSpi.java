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

package org.keycloak.vault;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 底层保险库访问 SPI。
 *
 * SPI for a low-level vault access.
 */
public class VaultSpi implements Spi {

    @Override
    /** @return 是否为内部 SPI */
    public boolean isInternal() {
        return true;
    }

    @Override
    /** @return SPI 名称 {@code vault} */
    public String getName() {
        return "vault";
    }

    @Override
    /** @return Provider 类型 {@link VaultProvider} */
    public Class<? extends Provider> getProviderClass() {
        return VaultProvider.class;
    }

    @Override
    /** @return 工厂类型 {@link VaultProviderFactory} */
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return VaultProviderFactory.class;
    }
}
