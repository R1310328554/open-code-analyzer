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
package org.redisson;

import java.util.Iterator;

import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

/**
 * {@link RedissonSetMultimap} 的键-值集合迭代器。
 * <p>对每个映射键通过 {@link RedissonSet#iterator(int)} 遍历其 Set 值。
 *
 * @param <K> 映射键类型
 * @param <V> 集合元素类型
 * @param <M> 多映射实现类型
 */
public class RedissonSetMultimapIterator<K, V, M> extends RedissonMultiMapIterator<K, V, M> {

    /** 使用默认 HSCAN 批次大小构造。 */
    public RedissonSetMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec) {
        super(map, commandExecutor, codec);
    }

    /** @param count 每次 SCAN 建议返回的键数量 */
    public RedissonSetMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec, int count) {
        super(map, commandExecutor, codec, count);
    }

    @Override
    /** 为给定值 Set 键创建 {@link RedissonSet} 迭代器。 */
    protected Iterator<V> getIterator(String name, int count) {
        RedissonSet<V> set = new RedissonSet<V>(codec, commandExecutor, map.getValuesName(name), null);
        return set.iterator(count);
    }

}
