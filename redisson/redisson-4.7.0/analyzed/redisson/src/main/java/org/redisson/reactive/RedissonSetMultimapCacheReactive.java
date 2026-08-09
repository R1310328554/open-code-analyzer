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

import org.redisson.api.RSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetReactive;
import org.redisson.api.RedissonReactiveClient;

/**
 * 带过期策略的 {@link RSetMultimap} 响应式视图：
 * 按 key 获取对应 {@link RSetReactive} 子集合。
 * <p>
 * 每个 key 映射一个 Redis Set；底层通过 {@link ReactiveProxyBuilder}
 * 将同步 Set 异步方法适配为 Publisher。
 *
 * @author Nikita Koksharov
 *
 * @param <K> Multimap 键类型
 * @param <V> Set 元素类型
 */
public class RedissonSetMultimapCacheReactive<K, V> {

    /** 底层带缓存的 Set Multimap。 */
    private final RSetMultimap<K, V> instance;
    /** 响应式命令执行器。 */
    private final CommandReactiveExecutor commandExecutor;
    /** 响应式客户端。 */
    private final RedissonReactiveClient redisson;

    /** @param instance 同步 Multimap @param commandExecutor 执行器 @param redisson 客户端 */
    public RedissonSetMultimapCacheReactive(RSetMultimap<K, V> instance, CommandReactiveExecutor commandExecutor,
                                            RedissonReactiveClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
        this.commandExecutor = commandExecutor;
    }

    /** 获取指定 key 对应的响应式 Set 视图。 */
    public RSetReactive<V> get(K key) {
        RSet<V> set = instance.get(key);
        return ReactiveProxyBuilder.create(commandExecutor, set, new RedissonSetReactive<>(set, redisson), RSetReactive.class);
    }
}
