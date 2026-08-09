/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.cache.redisson.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 将构建期缓存名集合与 {@link RedissonCachesConfig} 合并为 {@link RedissonCacheInfo} 集合。
 * <p>命名缓存配置优先于 default 配置；未指定字段保持 {@link Optional#empty()}。
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheInfoBuilder {

    /** 为每个构建期注册的缓存名生成运行时 {@link RedissonCacheInfo}。 */
    public static Set<RedissonCacheInfo> build(Set<String> cacheNames,
                                               RedissonCachesConfig runtimeConfig) {
        if (cacheNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<RedissonCacheInfo> result = new HashSet<>(cacheNames.size());
        for (String cacheName : cacheNames) {

            RedissonCacheInfo cacheInfo = new RedissonCacheInfo();
            cacheInfo.name = cacheName;

            RedissonCachesConfig.RedissonCacheRuntimeConfig defaultRuntimeConfig = runtimeConfig.defaultConfig();
            RedissonCachesConfig.RedissonCacheRuntimeConfig namedRuntimeConfig = runtimeConfig.cachesConfig().get(cacheInfo.name);

            // 命名缓存的 implementation 优先于全局 default。
            if (namedRuntimeConfig != null && namedRuntimeConfig.implementation().isPresent()) {
                cacheInfo.implementation = namedRuntimeConfig.implementation();
            } else if (defaultRuntimeConfig.implementation().isPresent()) {
                cacheInfo.implementation = defaultRuntimeConfig.implementation();
            }

            if (namedRuntimeConfig != null && namedRuntimeConfig.expireAfterAccess().isPresent()) {
                cacheInfo.expireAfterAccess = namedRuntimeConfig.expireAfterAccess();
            } else if (defaultRuntimeConfig.expireAfterAccess().isPresent()) {
                cacheInfo.expireAfterAccess = defaultRuntimeConfig.expireAfterAccess();
            }

            if (namedRuntimeConfig != null && namedRuntimeConfig.expireAfterWrite().isPresent()) {
                cacheInfo.expireAfterWrite = namedRuntimeConfig.expireAfterWrite();
            } else if (defaultRuntimeConfig.expireAfterWrite().isPresent()) {
                cacheInfo.expireAfterWrite = defaultRuntimeConfig.expireAfterWrite();
            }

            if (namedRuntimeConfig != null && namedRuntimeConfig.maxSize().isPresent()) {
                cacheInfo.maxSize = namedRuntimeConfig.maxSize();
            } else if (defaultRuntimeConfig.maxSize().isPresent()) {
                cacheInfo.maxSize = defaultRuntimeConfig.maxSize();
            }

            result.add(cacheInfo);
        }
        return result;
    }
}
