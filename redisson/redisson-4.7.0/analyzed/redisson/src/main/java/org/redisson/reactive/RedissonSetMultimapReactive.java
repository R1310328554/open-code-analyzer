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

import org.redisson.RedissonSetMultimap;
import org.redisson.api.RSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;

/**
 * {@link RSetMultimap} 的 Reactor 响应式实现：
 * 一个 key 对应一个 Set 值集合，{@link #get(Object)} 返回
 * 该 key 下 Set 的 {@link RSetReactive} 视图。
 *
 * @author Nikita Koksharov
 *
 * @param <K> Multimap 键类型
 * @param <V> Set 元素类型
 */
public class RedissonSetMultimapReactive<K, V> {

    /** 响应式客户端。 */
    private final RedissonReactiveClient redisson;
    /** 响应式命令执行器。 */
    private final CommandReactiveExecutor commandExecutor;
    /** 底层同步 Set Multimap。 */
    private final RedissonSetMultimap<K, V> instance;
    
    /** 使用默认编解码按名称创建 Multimap。 */
    public RedissonSetMultimapReactive(CommandReactiveExecutor commandExecutor, String name, RedissonReactiveClient redisson) {
        this.instance = new RedissonSetMultimap<>(commandExecutor, name);
        this.redisson = redisson;
        this.commandExecutor = commandExecutor;
    }

    /** 指定 {@link Codec} 创建 Multimap。 */
    public RedissonSetMultimapReactive(Codec codec, CommandReactiveExecutor commandExecutor, String name, RedissonReactiveClient redisson) {
        this.instance = new RedissonSetMultimap<>(codec, commandExecutor, name);
        this.redisson = redisson;
        this.commandExecutor = commandExecutor;
    }

    /** 返回 key 对应 Set 的响应式代理。 */
    public RSetReactive<V> get(K key) {
        RSet<V> set = ((RSetMultimap<K, V>) instance).get(key);
        return ReactiveProxyBuilder.create(commandExecutor, set, 
                new RedissonSetReactive<V>(set, redisson), RSetReactive.class);
    }

}
