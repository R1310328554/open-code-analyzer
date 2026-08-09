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
package org.redisson.api.stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link org.redisson.api.RStream#add} 方法的参数对象。
 * <p>
 * 支持配置待写入条目、裁剪策略及幂等生产选项。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamAddArgs<K, V> {

    /**
     * 若流不存在则不自动创建。
     *
     * @return 参数对象
     */
    StreamAddArgs<K, V> noMakeStream();

    /**
     * 启用严格裁剪（精确达到阈值）。
     *
     * @return 参数对象
     */
    StreamTrimStrategyArgs<StreamAddArgs<K, V>> trim();

    /**
     * 启用非严格裁剪（允许略超阈值）。
     *
     * @return 参数对象
     */
    StreamTrimStrategyArgs<StreamAddArgs<K, V>> trimNonStrict();

    /**
     * 为指定生产者配置幂等消息写入。
     * <p>
     * 网络故障或崩溃后重发时避免产生重复条目。
     * <p>
     * 需要 <b>Redis 8.6.0 及以上版本。</b>
     *
     * @param producerId 唯一生产者标识
     * @return 参数对象
     */
    StreamIdempotentArgs<StreamAddArgs<K, V>> idempotentProducerId(String producerId);

    /**
     * 定义单条待写入条目。
     *
     * @param k1 键
     * @param v1 值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 参数对象
     */
    static <K, V> StreamAddArgs<K, V> entry(K k1, V v1) {
        return entries(Collections.singletonMap(k1, v1));
    }

    /**
     * 定义两条待写入条目。
     *
     * @param k1 第 1 个键
     * @param v1 第 1 个值
     * @param k2 第 2 个键
     * @param v2 第 2 个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 参数对象
     */
    static <K, V> StreamAddArgs<K, V> entries(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return entries(map);
    }

    /**
     * 定义三条待写入条目。
     *
     * @param k1 第 1 个键
     * @param v1 第 1 个值
     * @param k2 第 2 个键
     * @param v2 第 2 个值
     * @param k3 第 3 个键
     * @param v3 第 3 个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 参数对象
     */
    static <K, V> StreamAddArgs<K, V> entries(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return entries(map);
    }

    /**
     * 定义四条待写入条目。
     *
     * @param k1 第 1 个键
     * @param v1 第 1 个值
     * @param k2 第 2 个键
     * @param v2 第 2 个值
     * @param k3 第 3 个键
     * @param v3 第 3 个值
     * @param k4 第 4 个键
     * @param v4 第 4 个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 参数对象
     */
    static <K, V> StreamAddArgs<K, V> entries(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return entries(map);
    }

    /**
     * 定义五条待写入条目。
     *
     * @param k1 第 1 个键
     * @param v1 第 1 个值
     * @param k2 第 2 个键
     * @param v2 第 2 个值
     * @param k3 第 3 个键
     * @param v3 第 3 个值
     * @param k4 第 4 个键
     * @param v4 第 4 个值
     * @param k5 第 5 个键
     * @param v5 第 5 个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 参数对象
     */
    static <K, V> StreamAddArgs<K, V> entries(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return entries(map);
    }

    /**
     * 通过映射定义待写入条目。
     *
     * @param entries 条目映射
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 参数对象
     */
    static <K, V> StreamAddArgs<K, V> entries(Map<K, V> entries) {
        return new StreamAddParams<K, V>(entries);
    }

}
