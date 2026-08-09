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
 * {@link RedissonListMultimap} 的键迭代器：对每个映射键遍历其 List 元素。
 *
 * @param <K> 映射键类型
 * @param <V> 列表元素类型
 * @param <M> Multimap 实现类型
 */
public class RedissonListMultimapIterator<K, V, M> extends RedissonMultiMapIterator<K, V, M> {

    /** 使用默认 scan 批次大小构造。 */
    public RedissonListMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec) {
        super(map, commandExecutor, codec);
    }

    /** @param count HSCAN 每批返回的键数量 */
    public RedissonListMultimapIterator(RedissonMultimap<K, V> map, CommandAsyncExecutor commandExecutor, Codec codec, int count) {
        super(map, commandExecutor, codec, count);
    }

    @Override
    /** 为指定映射键创建 {@link RedissonList} 本地迭代器。 */
    protected Iterator<V> getIterator(String name, int count) {
        RedissonList<V> set = new RedissonList<V>(codec, commandExecutor, map.getValuesName(name), null);
        return set.iterator();
    }

}
