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

package org.keycloak.wellknown;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * Well-Known 元数据的 Keycloak {@link Spi} 定义。
 * <p>注册 {@link WellKnownProvider} 与 {@link WellKnownProviderFactory}，SPI 名称为 {@code well-known}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class WellKnownSpi implements Spi {

    /** @return 是否为内部 SPI（不对外暴露扩展点文档） */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code well-known} */
    @Override
    public String getName() {
        return "well-known";
    }

    /** @return 提供者接口类 {@link WellKnownProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return WellKnownProvider.class;
    }

    /** @return 提供者工厂接口类 {@link WellKnownProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return WellKnownProviderFactory.class;
    }

}
