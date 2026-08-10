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
 * 客户端安装配置 SPI：注册 {@link ClientInstallationProvider} 提供者。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientInstallationSpi implements Spi {

    /** @return 内部 SPI，不对外暴露 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code client-installation} */
    @Override
    public String getName() {
        return "client-installation";
    }

    /** @return 提供者接口 {@link ClientInstallationProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ClientInstallationProvider.class;
    }

    /** @return 工厂接口（与提供者合并） */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ClientInstallationProvider.class;
    }

}
