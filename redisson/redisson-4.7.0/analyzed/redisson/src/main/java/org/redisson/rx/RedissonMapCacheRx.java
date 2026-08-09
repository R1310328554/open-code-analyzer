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

import org.redisson.api.RMap;

/**
 * 带 TTL/最大空闲时间驱逐策略的 {@link RMap} 的 Rx 包装。
 * <p>
 * 继承 {@link RedissonMapRx} 的全部键值/条目迭代与 per-key 锁能力；
 * 过期与 LRU 逻辑由底层 {@link RMap} 实例（MapCache）负责，本类不额外扩展 Rx API。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class RedissonMapCacheRx<K, V> extends RedissonMapRx<K, V> {

    public RedissonMapCacheRx(RMap<K, V> instance, CommandRxExecutor executor) {
        super(instance, executor);
    }
}
