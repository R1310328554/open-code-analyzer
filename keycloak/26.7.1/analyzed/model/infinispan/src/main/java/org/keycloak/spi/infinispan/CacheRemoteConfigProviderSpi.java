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

package org.keycloak.spi.infinispan;

import org.keycloak.provider.Spi;

/**
 * Hot Rod 远程 Infinispan 客户端配置 SPI。
 * <p>
 * 注册 {@link CacheRemoteConfigProviderFactory} 与 {@link CacheRemoteConfigProvider}，
 * 用于生成连接外部 Infinispan 集群的 Hot Rod 客户端配置。
 */
public class CacheRemoteConfigProviderSpi implements Spi {

    /** SPI 名称，服务加载标识为 {@code cacheRemote}。 */
    public static final String SPI_NAME = "cacheRemote";

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return SPI_NAME;
    }

    @Override
    public Class<CacheRemoteConfigProvider> getProviderClass() {
        return CacheRemoteConfigProvider.class;
    }

    @Override
    public Class<CacheRemoteConfigProviderFactory> getProviderFactoryClass() {
        return CacheRemoteConfigProviderFactory.class;
    }
}
