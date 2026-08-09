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

import org.redisson.api.RBucketReactive;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RMapReactive;
import org.redisson.api.RSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RSetCacheReactive;
import org.redisson.api.RSetReactive;
import org.redisson.api.RTransaction;
import org.redisson.api.RTransactionReactive;
import org.redisson.api.TransactionOptions;
import org.redisson.client.codec.Codec;
import org.redisson.transaction.RedissonTransaction;

import reactor.core.publisher.Mono;

/**
 * {@link RTransaction} 的 Reactor 响应式实现：
 * 在 Redis MULTI/EXEC 事务内获取 Bucket、Map、Set 等
 * 响应式视图，并暴露 {@link Mono} 形式的 commit/rollback。
 * <p>
 * 事务内对象操作仍走异步命令，提交时一次性 EXEC。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonTransactionReactive implements RTransactionReactive {

    /** 底层同步事务对象。 */
    private final RTransaction transaction;
    /** 响应式命令执行器。 */
    private final CommandReactiveExecutor executorService;
    
    /** @param executorService 执行器 @param options 事务超时/重试等选项 */
    public RedissonTransactionReactive(CommandReactiveExecutor executorService, TransactionOptions options) {
        this.transaction = new RedissonTransaction(executorService, options);
        this.executorService = executorService;
    }

    /** 在事务内获取字符串 Bucket 的响应式视图。 */
    @Override
    public <V> RBucketReactive<V> getBucket(String name) {
        return ReactiveProxyBuilder.create(executorService, transaction.<V>getBucket(name), RBucketReactive.class);
    }

    /** 指定编解码获取事务内 Bucket。 */
    @Override
    public <V> RBucketReactive<V> getBucket(String name, Codec codec) {
        return ReactiveProxyBuilder.create(executorService, transaction.<V>getBucket(name, codec), RBucketReactive.class);
    }

    /** 在事务内获取 Map 响应式视图。 */
    @Override
    public <K, V> RMapReactive<K, V> getMap(String name) {
        RMap<K, V> map = transaction.<K, V>getMap(name);
        return ReactiveProxyBuilder.create(executorService, map, 
                new RedissonMapReactive<K, V>(map, null), RMapReactive.class);
    }

    /** 指定编解码获取事务内 Map。 */
    @Override
    public <K, V> RMapReactive<K, V> getMap(String name, Codec codec) {
        RMap<K, V> map = transaction.getMap(name, codec);
        return ReactiveProxyBuilder.create(executorService, map,
                new RedissonMapReactive<>(map, null), RMapReactive.class);
    }

    /** 指定编解码获取带 TTL 的 MapCache。 */
    @Override
    public <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec) {
        RMapCache<K, V> map = transaction.getMapCache(name, codec);
        return ReactiveProxyBuilder.create(executorService, map,
                new RedissonMapCacheReactive<>(map, executorService), RMapCacheReactive.class);
    }

    /** 获取事务内 MapCache 响应式视图。 */
    @Override
    public <K, V> RMapCacheReactive<K, V> getMapCache(String name) {
        RMapCache<K, V> map = transaction.getMapCache(name);
        return ReactiveProxyBuilder.create(executorService, map,
                new RedissonMapCacheReactive<>(map, executorService), RMapCacheReactive.class);
    }

    /** 在事务内获取 Set 响应式视图。 */
    @Override
    public <V> RSetReactive<V> getSet(String name) {
        RSet<V> set = transaction.<V>getSet(name);
        return ReactiveProxyBuilder.create(executorService, set, 
                new RedissonSetReactive<V>(set, null), RSetReactive.class);
    }

    /** 指定编解码获取事务内 Set。 */
    @Override
    public <V> RSetReactive<V> getSet(String name, Codec codec) {
        RSet<V> set = transaction.<V>getSet(name, codec);
        return ReactiveProxyBuilder.create(executorService, set, 
                new RedissonSetReactive<V>(set, null), RSetReactive.class);
    }

    /** 获取事务内带过期 Set 的响应式视图。 */
    @Override
    public <V> RSetCacheReactive<V> getSetCache(String name) {
        RSetCache<V> set = transaction.<V>getSetCache(name);
        return ReactiveProxyBuilder.create(executorService, set, 
                new RedissonSetCacheReactive<V>(set, null), RSetCacheReactive.class);
    }

    /** 指定编解码获取事务内 SetCache。 */
    @Override
    public <V> RSetCacheReactive<V> getSetCache(String name, Codec codec) {
        RSetCache<V> set = transaction.<V>getSetCache(name, codec);
        return ReactiveProxyBuilder.create(executorService, set, 
                new RedissonSetCacheReactive<V>(set, null), RSetCacheReactive.class);
    }

    /** 异步提交事务（EXEC），成功返回空 Mono。 */
    @Override
    public Mono<Void> commit() {
        return executorService.reactive(() -> transaction.commitAsync());
    }

    /** 异步回滚事务（DISCARD）。 */
    @Override
    public Mono<Void> rollback() {
        return executorService.reactive(() -> transaction.rollbackAsync());
    }
    
}
