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

package org.keycloak.sessions;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 认证会话 SPI：注册 {@link AuthenticationSessionProvider} 及工厂。
 * <p>内部 SPI，名称 {@code authenticationSessions}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthenticationSessionSpi implements Spi {

    /** SPI 提供者标识符 {@code authenticationSessions} */
    public static final String PROVIDER_ID = "authenticationSessions";

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称，等于 {@link #PROVIDER_ID} */
    @Override
    public String getName() {
        return PROVIDER_ID;
    }

    /** @return 提供者接口 {@link AuthenticationSessionProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return AuthenticationSessionProvider.class;
    }

    /** @return 工厂接口 {@link AuthenticationSessionProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AuthenticationSessionProviderFactory.class;
    }

}
