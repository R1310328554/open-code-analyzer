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

import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RListReactive;

/**
 * 带 TTL 的 List Multimap 响应式 {@code get(K)} 工厂：
 * 为每个键返回独立 {@link RListReactive} 代理。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 外层键类型
 * @param <V> 列表元素类型
 */
public class RedissonListMultimapCacheReactive<K, V> {

    /** 底层带缓存的 List Multimap。 */
    private final RListMultimap<K, V> instance;
    /** 创建子列表代理的执行器。 */
    private final CommandReactiveExecutor commandExecutor;

    /** 绑定 multimap 与 Reactor 执行器。 */
    public RedissonListMultimapCacheReactive(RListMultimap<K, V> instance, CommandReactiveExecutor commandExecutor) {
        this.instance = instance;
        this.commandExecutor = commandExecutor;
    }

    /** 获取键对应列表的响应式视图。 */
    public RListReactive<V> get(K key) {
        RList<V> list = instance.get(key);
        return ReactiveProxyBuilder.create(commandExecutor, list, new RedissonListReactive<>(list), RListReactive.class);
    }
}
