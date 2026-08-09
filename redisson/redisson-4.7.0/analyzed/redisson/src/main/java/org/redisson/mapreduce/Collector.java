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
package org.redisson.mapreduce;

import io.netty.buffer.ByteBuf;
import org.redisson.api.RListMultimap;
import org.redisson.api.RedissonClient;
import org.redisson.api.mapreduce.RCollector;
import org.redisson.client.codec.Codec;
import org.redisson.misc.Hash;

import java.io.IOException;
import java.time.Duration;
import java.util.BitSet;

/**
 * MapReduce 中间结果收集器，实现 {@link org.redisson.api.mapreduce.RCollector}。
 * <p>
 * 对 key 做 64 位 hash 后取模，将 (key, value) 写入对应分片的
 * {@link org.redisson.api.RListMultimap}；首次写入分片时按 timeout 设置过期。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key
 * @param <V> value
 */
public class Collector<K, V> implements RCollector<K, V> {

    /** Redisson 客户端，用于访问分区 multimap。 */
    private RedissonClient client;
    /** Collector 根名称，实际 Redis key 为 name:part。 */
    private String name;
    /** 分区数量，与 MapReduce Worker 数一致。 */
    private int parts;
    /** 中间结果的编解码器。 */
    private Codec codec;
    /** 中间数据 TTL（毫秒），0 表示不过期。 */
    private long timeout;
    /** 记录已为哪些分片设置过 expire，避免重复调用。 */
    private BitSet expirationsBitSet = new BitSet();
    
    /** 构造 Collector，parts 通常等于活跃 Worker 数量。 */
    public Collector(Codec codec, RedissonClient client, String name, int parts, long timeout) {
        super();
        this.client = client;
        this.name = name;
        this.parts = parts;
        this.codec = codec;
        this.timeout = timeout;
        expirationsBitSet = new BitSet(parts);
    }

    /**
     * 发射一条中间键值对：hash(key) % parts 决定目标分片，
     * 写入 RListMultimap 后按需为分片设置过期时间。
     */
    @Override
    public void emit(K key, V value) {
        try {
            // 编码 key 并计算分片索引
            ByteBuf encodedKey = codec.getValueEncoder().encode(key);
            long hash = Hash.hash64(encodedKey);
            encodedKey.release();
            int part = (int) Math.abs(hash % parts);
            String partName = name + ":" + part;
            
            RListMultimap<K, V> multimap = client.getListMultimap(partName, codec);
            multimap.put(key, value);
            // 每个分片仅设置一次 TTL
            if (timeout > 0 && !expirationsBitSet.get(part)) {
                multimap.expire(Duration.ofMillis(timeout));
                expirationsBitSet.set(part);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
