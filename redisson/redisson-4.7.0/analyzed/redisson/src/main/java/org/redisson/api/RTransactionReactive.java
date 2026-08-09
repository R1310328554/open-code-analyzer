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
package org.redisson.api;

import org.redisson.client.codec.Codec;

import reactor.core.publisher.Mono;

/**
 * 事务 Reactor 响应式 API；写操作加锁，
 * 并在提交/回滚前维护数据修改操作列表。
 * <p>
 * 事务隔离级别：<b>READ_COMMITTED</b>（读已提交）
 *
 * @author Nikita Koksharov
 *
 */
public interface RTransactionReactive {

    /**
     * 按名称返回事务性 Bucket 实例。
     *
     * @param <V> 值类型
     * @param name 对象名称
     * @return Bucket 实例
     */
    <V> RBucketReactive<V> getBucket(String name);
    
    /**
     * 按名称返回事务性 Bucket 实例，并使用指定编解码器。
     *
     * @param <V> 值类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return Bucket 实例
     */
    <V> RBucketReactive<V> getBucket(String name, Codec codec);

    /**
     * 按名称返回事务性 Map 实例。
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param name 对象名称
     * @return Map 实例
     */
    <K, V> RMapReactive<K, V> getMap(String name);

    /**
     * 按名称返回事务性 Map 实例，键值均使用指定编解码器。
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param name 对象名称
     * @param codec 键值编解码器
     * @return Map 实例
     */
    <K, V> RMapReactive<K, V> getMap(String name, Codec codec);
    
    /**
     * 按名称返回事务性 Set 实例。
     * 
     * @param <V> 值类型
     * @param name 对象名称
     * @return Set 实例
     */
    <V> RSetReactive<V> getSet(String name);
    
    /**
     * 按名称返回事务性 Set 实例，并使用指定编解码器。
     * 
     * @param <V> 值类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return Set 实例
     */
    <V> RSetReactive<V> getSet(String name, Codec codec);
    
    /**
     * 按名称返回事务性 Set 缓存实例，支持为元素设置 TTL 逐出。
     *
     * <p>若不需要逐出机制，建议使用普通 Set {@link #getSet(String)}。</p>
     * 
     * @param <V> 值类型
     * @param name 对象名称
     * @return SetCache 实例
     */
    <V> RSetCacheReactive<V> getSetCache(String name);
    
    /**
     * 按名称返回事务性 Set 缓存实例，支持为元素设置 TTL 逐出。
     *
     * <p>若不需要逐出机制，建议使用普通 Set {@link #getSet(String, Codec)}。</p>
     * 
     * @param <V> 值类型
     * @param name 对象名称
     * @param codec 值编解码器
     * @return SetCache 实例
     */
    <V> RSetCacheReactive<V> getSetCache(String name, Codec codec);
    
    /**
     * 按名称返回事务性 Map 缓存实例，支持 MaxIdleTime 与 TTL 条目逐出。
     * <p>
     * 若不需要逐出机制，建议使用普通 Map {@link #getMap(String)}。</p>
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param name 对象名称
     * @return MapCache 实例
     */
    <K, V> RMapCacheReactive<K, V> getMapCache(String name);

    /**
     * 按名称返回事务性 Map 缓存实例，键值使用指定编解码器，支持 MaxIdleTime 与 TTL 条目逐出。
     * <p>
     * 若不需要逐出机制，建议使用普通 Map {@link #getMap(String, Codec)}。
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param name 对象名称
     * @param codec 键值编解码器
     * @return MapCache 实例
     */
    <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec);
    
    /**
     * 提交本事务的全部变更。
     * 
     * @return 无返回值
     */
    Mono<Void> commit();
    
    /**
     * 回滚本事务的全部变更。
     * @return 无返回值
     */
    Mono<Void> rollback();

}
