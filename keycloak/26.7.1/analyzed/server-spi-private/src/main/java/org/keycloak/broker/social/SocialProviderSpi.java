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
package org.keycloak.broker.social;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 社交身份提供者 SPI 注册项，将 {@link SocialIdentityProvider} 与工厂绑定到 Keycloak SPI 体系。
 *
 * @author Pedro Igor
 */
public class SocialProviderSpi implements Spi {

    /** SPI 名称常量 {@code social}。 */
    public static final String SOCIAL_SPI_NAME = "social";

    /** 内部 SPI，不对外暴露为可插拔扩展点。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return SOCIAL_SPI_NAME;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return SocialIdentityProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SocialIdentityProviderFactory.class;
    }
}
