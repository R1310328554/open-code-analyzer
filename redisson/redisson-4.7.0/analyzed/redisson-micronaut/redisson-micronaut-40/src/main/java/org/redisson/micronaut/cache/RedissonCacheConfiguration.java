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
 * {@code redisson.caches.<name>} 命名缓存的 Micronaut 配置绑定。
 * <p>每个属性块对应一个 {@link RedissonSyncCache} Bean。
 *
 * @author Nikita Koksharov
 */
@EachProperty("redisson.caches")
public class RedissonCacheConfiguration extends BaseCacheConfiguration {

    /** @param name 配置键后缀，即缓存逻辑名称 */
    public RedissonCacheConfiguration(@Parameter String name) {
        super(name);
    }

}
