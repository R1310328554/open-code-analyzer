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
package org.redisson.rx;

import org.redisson.RedissonSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RSetRx;
import org.redisson.api.RedissonRxClient;

/**
 * Set 多映射（key → Set&lt;V&gt;）的 Rx 门面。
 * <p>
 * {@link #get(K)} 返回指定键下 Redis SET 的 {@link RSetRx} 视图，
 * 通过 {@link RxProxyBuilder} 将 {@link RedissonSetRx} 与异步命令层绑定。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public class RedissonSetMultimapRx<K, V> {

    /** Rx 客户端，供嵌套 Set 获取 per-value 锁。 */
    private final RedissonRxClient redisson;
    /** Rx 命令执行器。 */
    private final CommandRxExecutor commandExecutor;
    /** 底层 SetMultimap。 */
    private final RSetMultimap<K, V> instance;
    
    public RedissonSetMultimapRx(RSetMultimap<K, V> instance, CommandRxExecutor commandExecutor, RedissonRxClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
        this.commandExecutor = commandExecutor;
    }

    /** 获取 multimap 中某键对应集合的 Rx 接口（含迭代、addAll 等）。 */
    public RSetRx<V> get(K key) {
        RedissonSet<V> set = (RedissonSet<V>) instance.get(key);
        return RxProxyBuilder.create(commandExecutor, set, 
                new RedissonSetRx<V>(set, redisson), RSetRx.class);
    }

}
