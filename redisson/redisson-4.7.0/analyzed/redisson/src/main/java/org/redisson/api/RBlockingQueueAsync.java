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

import org.redisson.api.queue.QueueMoveElementsArgs;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@link BlockingQueue} 的分布式异步 API。
 * <p>提供阻塞 poll/take/put、跨队列拉取及批量迁移等异步操作。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
public interface RBlockingQueueAsync<V> extends RQueueAsync<V> {

    /**
     * 异步地从指定队列集合（含自身）中拉取首个可用队头元素并移除。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队头元素；超时为 {@code null}
     */
    RFuture<V> pollFromAnyAsync(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 异步地从指定队列集合（含自身）中拉取首个可用队头元素，并返回元素及其来源队列名。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}
     */
    RFuture<Entry<String, V>> pollFromAnyWithNameAsync(Duration timeout, String... queueNames);

    /**
     * 从多个队列（含自身）批量拉取队头元素。
     * <p>需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count 每个队列最多拉取数量
     * @param queueNames 候选队列名
     * @return 队列名到元素列表的映射
     */
    RFuture<Map<String, List<V>>> pollFirstFromAnyAsync(Duration duration, int count, String... queueNames);

    /**
     * 从多个队列（含自身）批量拉取队尾元素。
     * <p>需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count 每个队列最多拉取数量
     * @param queueNames 候选队列名
     * @return 队列名到元素列表的映射
     */
    RFuture<Map<String, List<V>>> pollLastFromAnyAsync(Duration duration, int count, String... queueNames);

    /**
     * 异步地从指定队列集合（含自身）中拉取首个可用队尾元素，并返回元素及其来源队列名。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}
     */
    RFuture<Entry<String, V>> pollLastFromAnyWithNameAsync(Duration timeout, String... queueNames);

    /**
     * 异步地将本队列中至多 {@code maxElements} 个可用元素移除并转入集合 {@code c}。
     * <p>向目标集合添加失败时，元素可能留在原队列、目标集合或两者中；
     * 不可将队列导入自身（否则抛出 {@code IllegalArgumentException}）；
     * 操作进行中修改目标集合的行为未定义。
     *
     * @param c 目标集合
     * @param maxElements 最多转移的元素数量
     * @return 实际转移的元素数量
     * @throws UnsupportedOperationException 目标集合不支持添加元素
     * @throws ClassCastException 元素类型无法加入目标集合
     * @throws NullPointerException 目标集合为 {@code null}
     * @throws IllegalArgumentException 目标集合为本队列或元素属性不允许加入
     */
    RFuture<Integer> drainToAsync(Collection<? super V> c, int maxElements);

    /**
     * 异步地将本队列全部可用元素移除并转入集合 {@code c}；通常比循环 poll 更高效。
     * <p>向目标集合添加失败时，元素可能留在原队列、目标集合或两者中；
     * 不可将队列导入自身；操作进行中修改目标集合的行为未定义。
     *
     * @param c 目标集合
     * @return 实际转移的元素数量
     * @throws UnsupportedOperationException 目标集合不支持添加元素
     * @throws ClassCastException 元素类型无法加入目标集合
     * @throws NullPointerException 目标集合为 {@code null}
     * @throws IllegalArgumentException 目标集合为本队列或元素属性不允许加入
     */
    RFuture<Integer> drainToAsync(Collection<? super V> c);

    /**
     * 从本队列队尾取出元素并插入目标队列队头；必要时阻塞等待。
     *
     * @param queueName 目标队列名
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 被移动的元素；超时为 {@code null}
     */
    RFuture<V> pollLastAndOfferFirstToAsync(String queueName, long timeout, TimeUnit unit);
    
    /**
     * 从候选队列（含自身）中阻塞取出队尾元素并插入目标队列队头。
     *
     * @param queueName 目标队列名
     * @return 被移动的元素
     */
    RFuture<V> takeLastAndOfferFirstToAsync(String queueName);

    /**
     * 异步阻塞地从队头取出并移除元素；必要时等待至多 {@code timeout}。
     *
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队头元素；超时为 {@code null}
     */
    RFuture<V> pollAsync(long timeout, TimeUnit unit);

    /**
     * 异步阻塞地从队头取出并移除元素，直至有元素可用。
     *
     * @return 队头元素
     */
    RFuture<V> takeAsync();

    /**
     * 异步阻塞地将元素插入队列；必要时等待直至有可用空间。
     *
     * @param e 待插入元素
     * @throws ClassCastException 元素类型不允许加入本队列
     * @throws NullPointerException 元素为 {@code null}
     * @throws IllegalArgumentException 元素属性不允许加入本队列
     */
    RFuture<Void> putAsync(V e);

    /**
     * 将本队列队头元素批量迁移至目标队列队尾。
     * <p>必要时阻塞等待至多 {@code timeout}；返回已迁移元素列表。
     * <p>需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param timeout 最长等待时间
     * @param args 迁移参数
     * @return 已迁移元素；超时返回空列表
     */
    RFuture<List<V>> moveAsync(Duration timeout, QueueMoveElementsArgs args);

}
