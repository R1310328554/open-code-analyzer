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
package org.keycloak.broker.provider;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 身份提供方 SPI 定义，注册 {@link IdentityProvider} 与 {@link IdentityProviderFactory}。
 *
 * @author Pedro Igor
 */
public class IdentityProviderSpi implements Spi {

    /** SPI 注册名称常量。 */
    public static final String IDENTITY_PROVIDER_SPI_NAME = "identity_provider";

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** 返回 SPI 名称 {@link #IDENTITY_PROVIDER_SPI_NAME}。 */
    @Override
    public String getName() {
        return IDENTITY_PROVIDER_SPI_NAME;
    }

    /** 身份提供方提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return IdentityProvider.class;
    }

    /** 身份提供方工厂接口类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return IdentityProviderFactory.class;
    }
}
