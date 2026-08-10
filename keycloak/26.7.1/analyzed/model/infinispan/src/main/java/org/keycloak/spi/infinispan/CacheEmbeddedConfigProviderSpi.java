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

import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * 嵌入式 Infinispan 缓存配置 SPI。
 * <p>
 * 注册 {@link CacheEmbeddedConfigProviderFactory} 与 {@link CacheEmbeddedConfigProvider}，
 * 用于向 Keycloak 提供 {@link ConfigurationBuilderHolder} 以配置 {@link EmbeddedCacheManager}。
 */
public class CacheEmbeddedConfigProviderSpi implements Spi {

    /** SPI 名称，服务加载标识为 {@code cacheEmbedded}。 */
    public static final String SPI_NAME = "cacheEmbedded";

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return SPI_NAME;
    }

    @Override
    public Class<CacheEmbeddedConfigProvider> getProviderClass() {
        return CacheEmbeddedConfigProvider.class;
    }

    @Override
    public Class<CacheEmbeddedConfigProviderFactory> getProviderFactoryClass() {
        return CacheEmbeddedConfigProviderFactory.class;
    }
}
