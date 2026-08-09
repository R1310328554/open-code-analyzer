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

import org.redisson.RedissonList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RListRx;

/**
 * List 多值映射 {@link org.redisson.api.RListMultimapRx} 的 Rx 辅助：按 key 取 {@link RListRx}。
 * <p>
 * {@link #get} 从 multimap 取出 {@link RedissonList} 并用 {@link RxProxyBuilder} 包装为 Rx 列表视图。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public class RedissonListMultimapRx<K, V> {

    /** Rx 命令执行器，供 get(key) 构建的 RListRx 使用。 */
    private final CommandRxExecutor commandExecutor;
    /** 底层 RListMultimap 实例。 */
    private final RListMultimap<K, V> instance;
    
    public RedissonListMultimapRx(RListMultimap<K, V> instance, CommandRxExecutor commandExecutor) {
        this.instance = instance;
        this.commandExecutor = commandExecutor;
    }

    /** 返回指定 key 对应列表的 Rx 视图（非阻塞 async 方法经代理映射）。 */
    public RListRx<V> get(K key) {
        RedissonList<V> list = (RedissonList<V>) instance.get(key);
        return RxProxyBuilder.create(commandExecutor, list,
                new RedissonListRx<V>(list), RListRx.class);
    }

}
