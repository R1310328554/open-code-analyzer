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

import org.keycloak.provider.Provider;

import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * 为 {@link EmbeddedCacheManager} 提供嵌入式 Infinispan 配置的 Provider。
 * <p>
 * 返回的 {@link ConfigurationBuilderHolder} 用于构建本地/嵌入式缓存管理器的完整配置。
 */
public interface CacheEmbeddedConfigProvider extends Provider {

    /**
     * 返回包含 {@link EmbeddedCacheManager} 配置的 {@link ConfigurationBuilderHolder}，不得为 {@code null}。
     *
     * @return 嵌入式缓存管理器配置
     */
    ConfigurationBuilderHolder configuration();

    @Override
    default void close() {
        //no-op
    }
}
