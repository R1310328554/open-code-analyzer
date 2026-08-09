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

import org.redisson.RedissonScoredSortedSet;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;
import reactor.core.publisher.Flux;


/**
 * {@link RScoredSortedSet} 的 Reactor 响应式辅助类：
 * 提供阻塞式 take 流、SCAN 迭代及带分数条目的迭代。
 * <p>
 * 底层 Redis ZSET 通过 {@link SetReactiveIterator} 分页扫描，
 * 避免一次性加载全部成员。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 成员值类型
 */
public class RedissonScoredSortedSetReactive<V>  {

    /** 底层异步有序集合。 */
    private final RScoredSortedSetAsync<V> instance;
    
    /** 使用默认编解码创建响应式有序集合。 */
    public RedissonScoredSortedSetReactive(CommandReactiveExecutor commandExecutor, String name) {
        this(commandExecutor, name, new RedissonScoredSortedSet<V>(commandExecutor, name, null));
    }

    /** 内部构造：直接绑定已有异步实例。 */
    private RedissonScoredSortedSetReactive(CommandReactiveExecutor commandExecutor, String name, RScoredSortedSetAsync<V> instance) {
        this.instance = instance;
    }
    
    /** 指定 {@link Codec} 创建响应式有序集合。 */
    public RedissonScoredSortedSetReactive(Codec codec, CommandReactiveExecutor commandExecutor, String name) {
        this(codec, commandExecutor, name, new RedissonScoredSortedSet<V>(codec, commandExecutor, name, null));
    }

    private RedissonScoredSortedSetReactive(Codec codec, CommandReactiveExecutor commandExecutor, String name, RScoredSortedSetAsync<V> instance) {
        this.instance = instance;
    }
    
    /** 持续阻塞取出分值最小的元素，转为 Flux 流。 */
    public Flux<V> takeFirstElements() {
        return ElementsStream.takeElements(() -> instance.takeFirstAsync());
    }
    
    /** 持续阻塞取出分值最大的元素，转为 Flux 流。 */
    public Flux<V> takeLastElements() {
        return ElementsStream.takeElements(() -> instance.takeLastAsync());
    }

    /** 基于 SCAN 的成员值迭代，可选模式匹配与每批数量。 */
    private Flux<V> scanIteratorReactive(String pattern, int count) {
        return Flux.create(new SetReactiveIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonScoredSortedSet<V>) instance).scanIteratorAsync(client, nextIterPos, pattern, count);
            }
        });
    }

    /** @return Redis 键名 */
    public String getName() {
        return ((RedissonScoredSortedSet<V>) instance).getRawName();
    }
    
    /** 默认每批 10 条的全量 SCAN 迭代。 */
    public Flux<V> iterator() {
        return scanIteratorReactive(null, 10);
    }

    /** 带 Glob 模式匹配的 SCAN 迭代。 */
    public Flux<V> iterator(String pattern) {
        return scanIteratorReactive(pattern, 10);
    }

    /** 指定每批 SCAN 数量的迭代。 */
    public Flux<V> iterator(int count) {
        return scanIteratorReactive(null, count);
    }

    /** 同时指定模式与每批数量的 SCAN 迭代。 */
    public Flux<V> iterator(String pattern, int count) {
        return scanIteratorReactive(pattern, count);
    }

    /** 扫描带分数的 {@link ScoredEntry} 条目。 */
    private Flux<ScoredEntry<V>> entryScanIteratorReactive(String pattern, int count) {
        return Flux.create(new SetReactiveIterator<ScoredEntry<V>>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonScoredSortedSet<V>) instance).entryScanIteratorAsync(client, nextIterPos, pattern, count);
            }
        });
    }

    /** 默认参数的分数条目 SCAN 迭代。 */
    public Flux<ScoredEntry<V>> entryIterator() {
        return entryScanIteratorReactive(null, 10);
    }

    /** 带模式匹配的分数条目迭代。 */
    public Flux<ScoredEntry<V>> entryIterator(String pattern) {
        return entryScanIteratorReactive(pattern, 10);
    }

    /** 指定批量的分数条目迭代。 */
    public Flux<ScoredEntry<V>> entryIterator(int count) {
        return entryScanIteratorReactive(null, count);
    }

    /** 同时指定模式与批量的分数条目迭代。 */
    public Flux<ScoredEntry<V>> entryIterator(String pattern, int count) {
        return entryScanIteratorReactive(pattern, count);
    }

}
