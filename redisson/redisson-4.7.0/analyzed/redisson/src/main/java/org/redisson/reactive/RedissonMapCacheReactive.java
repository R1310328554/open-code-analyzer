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
package org.redisson.reactive;

import org.redisson.api.RMap;

/**
 * 带 per-entry TTL 的 {@link RMapCache} Reactor 实现。
 * <p>
 * 继承 {@link RedissonMapReactive} 的 HSCAN 迭代能力；
 * 其余 CRUD/TTL 方法仍由 {@link ReactiveProxyBuilder} 映射到 {@code *Async}。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class RedissonMapCacheReactive<K, V> extends RedissonMapReactive<K, V> {

    /** 包装 MapCache 实例并绑定 Reactor 执行器。 */
    public RedissonMapCacheReactive(RMap<K, V> map, CommandReactiveExecutor commandExecutor) {
        super(map, commandExecutor);
    }

}
