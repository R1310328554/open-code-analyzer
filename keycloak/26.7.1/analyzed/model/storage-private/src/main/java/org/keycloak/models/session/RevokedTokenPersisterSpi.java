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

package org.keycloak.models.session;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 已吊销令牌持久化 SPI：向 Keycloak 注册 {@link RevokedTokenPersisterProvider} 及其工厂实现。
 */
public class RevokedTokenPersisterSpi implements Spi {

    /** 内部 SPI，不对外暴露给扩展模块配置。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称，对应配置键 {@code revokedTokenPersister}。 */
    @Override
    public String getName() {
        return "revokedTokenPersister";
    }

    /** 本 SPI 提供的 Provider 接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return RevokedTokenPersisterProvider.class;
    }

    /** 本 SPI 对应的 ProviderFactory 实现类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return RevokedTokensPersisterProviderFactory.class;
    }
}
