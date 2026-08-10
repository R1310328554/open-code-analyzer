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

package org.keycloak.authentication;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 认证器 SPI 定义，注册 {@link Authenticator} 与 {@link AuthenticatorFactory} 提供者类型。
 * <p>内部 SPI，名称 {@value #SPI_NAME}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AuthenticatorSpi implements Spi {

    /** SPI 标识符。 */
    public static final String SPI_NAME = "authenticator";

    /** 内部 SPI，不对外暴露。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** 返回 SPI 名称 {@value #SPI_NAME}。 */
    @Override
    public String getName() {
        return SPI_NAME;
    }

    /** 提供者接口类型 {@link Authenticator}。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return Authenticator.class;
    }

    /** 工厂接口类型 {@link AuthenticatorFactory}。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AuthenticatorFactory.class;
    }

}
