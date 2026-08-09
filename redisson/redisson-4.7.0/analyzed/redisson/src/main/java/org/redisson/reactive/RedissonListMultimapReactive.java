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

import org.redisson.RedissonListMultimap;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RListReactive;
import org.redisson.client.codec.Codec;

/**
 * List Multimap 的 Reactor {@code get(K)} 实现：
 * 每个键映射为独立 {@link RedissonListReactive} 并经由 {@link ReactiveProxyBuilder} 暴露。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 外层键类型
 * @param <V> 列表元素类型
 */
public class RedissonListMultimapReactive<K, V> {

    /** Reactor 命令执行器。 */
    private final CommandReactiveExecutor commandExecutor;
    /** 底层 List Multimap 实例。 */
    private final RedissonListMultimap<K, V> instance;
    
    /** 按名称构造默认 codec 的 multimap。 */
    public RedissonListMultimapReactive(CommandReactiveExecutor commandExecutor, String name) {
        this.instance = new RedissonListMultimap<K, V>(commandExecutor, name);
        this.commandExecutor = commandExecutor;
    }

    /** 指定 {@link Codec} 构造 multimap。 */
    public RedissonListMultimapReactive(Codec codec, CommandReactiveExecutor commandExecutor, String name) {
        this.instance = new RedissonListMultimap<K, V>(codec, commandExecutor, name);
        this.commandExecutor = commandExecutor;
    }

    /** 返回键对应 Redis List 的响应式代理。 */
    public RListReactive<V> get(K key) {
        RList<V> list = ((RListMultimap<K, V>) instance).get(key);
        return ReactiveProxyBuilder.create(commandExecutor, list,
                new RedissonListReactive<V>(instance.getCodec(), commandExecutor, list.getName()), RListReactive.class);
    }

}
