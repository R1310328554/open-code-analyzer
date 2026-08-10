/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.rar;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 授权详情处理器 SPI：按 RAR 规范注册 {@code authorization_details} 处理器。
 * <p>内部 SPI，名称 {@code authorization-details-processor}。</p>
 *
 * @author <a href="mailto:Forkim.Akwichek@adorsys.com">Forkim Akwichek</a>
 */
public class AuthorizationDetailsProcessorSpi implements Spi {

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code authorization-details-processor} */
    @Override
    public String getName() {
        return "authorization-details-processor";
    }

    /** @return 提供者接口 {@link AuthorizationDetailsProcessor} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return AuthorizationDetailsProcessor.class;
    }

    /** @return 工厂接口 {@link AuthorizationDetailsProcessorFactory} */
    @Override
    public Class<? extends ProviderFactory<AuthorizationDetailsProcessor<?>>> getProviderFactoryClass() {
        return AuthorizationDetailsProcessorFactory.class;
    }
}
