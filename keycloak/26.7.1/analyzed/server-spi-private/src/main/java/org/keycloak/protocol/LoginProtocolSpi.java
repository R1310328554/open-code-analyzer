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

package org.keycloak.protocol;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 登录协议 SPI：注册 {@link LoginProtocol} 与 {@link LoginProtocolFactory}。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoginProtocolSpi implements Spi {

    /** @return 内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code login-protocol} */
    @Override
    public String getName() {
        return "login-protocol";
    }

    /** @return 提供者接口 {@link LoginProtocol} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return LoginProtocol.class;
    }

    /** @return 工厂接口 {@link LoginProtocolFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return LoginProtocolFactory.class;
    }

}
