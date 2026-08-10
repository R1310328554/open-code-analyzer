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

package org.keycloak.protocol.oidc.grants;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * OAuth 2.0 授权类型 SPI：在 Token 端点注册可插拔的 grant 实现。
 * <p>内部 SPI，名称 {@link #SPI_NAME}（{@code oauth2-grant-type}）。</p>
 *
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public class OAuth2GrantTypeSpi implements Spi {

    /** SPI 名称常量：{@code oauth2-grant-type}。 */
    public static final String SPI_NAME = "oauth2-grant-type";

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@link #SPI_NAME} */
    @Override
    public String getName() {
        return SPI_NAME;
    }

    /** @return 提供者接口 {@link OAuth2GrantType} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return OAuth2GrantType.class;
    }

    /** @return 工厂接口 {@link OAuth2GrantTypeFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return OAuth2GrantTypeFactory.class;
    }

}
