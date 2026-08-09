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
package org.redisson.micronaut.cache;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

/**
 * {@code redisson.caches-native.<name>} Native Map 缓存的 Micronaut 配置绑定。
 * <p>使用 {@link RMapCacheNative}，适合服务端原生过期语义。
 *
 * @author Nikita Koksharov
 */
@EachProperty("redisson.caches-native")
public class RedissonCacheNativeConfiguration extends BaseCacheConfiguration {

    /** @param name 配置键后缀，即 Native 缓存逻辑名称 */
    public RedissonCacheNativeConfiguration(@Parameter String name) {
        super(name);
    }

}
