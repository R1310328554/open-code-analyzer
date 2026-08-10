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
package org.keycloak.securityprofile;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 安全配置文件 SPI：注册 {@link SecurityProfileProvider} 及工厂。
 * <p>内部 SPI，名称 {@code security-profile}。</p>
 *
 * @author rmartinc
 */
public class SecurityProfileSpi implements Spi {

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code security-profile} */
    @Override
    public String getName() {
        return "security-profile";
    }

    /** @return 提供者接口 {@link SecurityProfileProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return SecurityProfileProvider.class;
    }

    /** @return 工厂接口 {@link SecurityProfileProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SecurityProfileProviderFactory.class;
    }
}
