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
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 由 Redis 列表实现的分布式 {@link BlockingQueue}。
 * <p>支持阻塞取元素、跨队列 poll、批量迁移及元素订阅。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
public interface RBlockingQueue<V> extends BlockingQueue<V>, RQueue<V>, RBlockingQueueAsync<V> {

    /**
     * 从指定队列集合（含自身）中阻塞拉取首个可用队头元素并移除。
     * <p>当前队列名始终包含在候选集合中。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 取到的元素；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    V pollFromAny(long timeout, TimeUnit unit, String... queueNames) throws InterruptedException;

    /**
     * 从指定队列集合（含自身）中阻塞拉取首个可用队头元素，并返回元素及其来源队列名。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    Entry<String, V> pollFromAnyWithName(Duration timeout, String... queueNames) throws InterruptedException;

    /**
     * 从多个队列（含自身）批量拉取队头元素。
     * <p>需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count 每个队列最多拉取数量
     * @param queueNames 候选队列名
     * @return 队列名到元素列表的映射
     * @throws InterruptedException 等待被中断时
     */
    Map<String, List<V>> pollFirstFromAny(Duration duration, int count, String... queueNames) throws InterruptedException;

    /**
     * 从多个队列（含自身）批量拉取队尾元素。
     * <p>需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count 每个队列最多拉取数量
     * @param queueNames 候选队列名
     * @return 队列名到元素列表的映射
     * @throws InterruptedException 等待被中断时
     */
    Map<String, List<V>> pollLastFromAny(Duration duration, int count, String... queueNames) throws InterruptedException;

    /**
     * 从指定队列集合（含自身）中阻塞拉取首个可用队尾元素，并返回元素及其来源队列名。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    Entry<String, V> pollLastFromAnyWithName(Duration timeout, String... queueNames) throws InterruptedException;

    /**
     * 从本队列队尾取出元素并插入目标队列队头；必要时阻塞等待。
     *
     * @param queueName 目标队列名
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 被移动的元素；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    V pollLastAndOfferFirstTo(String queueName, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 从候选队列（含自身）中阻塞取出队尾元素并插入目标队列队头。
     *
     * @param queueName 目标队列名
     * @return 被移动的元素
     * @throws InterruptedException 等待被中断时
     */
    V takeLastAndOfferFirstTo(String queueName) throws InterruptedException;

    /**
     * 将本队列队头元素批量迁移至目标队列队尾。
     * <p>必要时阻塞等待至多 {@code timeout}；返回已迁移元素列表。
     * <p>示例：
     * <pre>
     * List&lt;V&gt; elements = queue.move(Duration.ofSeconds(10),
     *                                QueueMoveElementsArgs.to("myQueue")
     *                                                     .count(10));
     * </pre>
     * <p>需要 <b>Redis 8.10.0 及以上</b>。
     *
     * @param timeout 最长等待时间
     * @param args 迁移参数
     * @return 已迁移元素；超时返回空列表
     */
    List<V> move(Duration timeout, QueueMoveElementsArgs args);

    /**
     * 已废弃，请改用 {@link #subscribeOnElements(Function)}。
     *
     * @param consumer 队列元素监听器
     * @return 监听器 ID
     */
    @Deprecated
    int subscribeOnElements(Consumer<V> consumer);

    /**
     * 订阅队列新元素；内部循环调用 {@link #takeAsync()} 取元素。
     * <p>注意：监听器内勿调用阻塞方法。
     *
     * @param consumer 异步元素处理器
     * @return 监听器 ID
     */
    int subscribeOnElements(Function<V, CompletionStage<Void>> consumer);

    /**
     * 取消指定 ID 的元素订阅。
     *
     * @param listenerId 监听器 ID
     */
    void unsubscribe(int listenerId);

}
