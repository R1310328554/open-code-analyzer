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

import org.redisson.api.queue.DequeMoveArgs;

import java.time.Duration;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 由 Redis 列表实现的分布式 {@link BlockingDeque}。
 * <p>支持双端阻塞入队/出队、跨队列批量拉取及元素订阅。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
public interface RBlockingDeque<V> extends BlockingDeque<V>, RBlockingQueue<V>, RDeque<V>, RBlockingDequeAsync<V> {

    /**
     * 从指定队列集合（含自身）中阻塞拉取首个可用<b>队头</b>元素并移除。
     * <p>在 {@code timeout} 内无元素可用则返回 {@code null}。
     *
     * @param queueNames 候选队列名列表
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 取到的元素；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    V pollFirstFromAny(long timeout, TimeUnit unit, String... queueNames) throws InterruptedException;

    /**
     * 从指定队列集合（含自身）中阻塞拉取首个可用<b>队尾</b>元素并移除。
     * <p>在 {@code timeout} 内无元素可用则返回 {@code null}。
     *
     * @param queueNames 候选队列名列表
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 取到的元素；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    V pollLastFromAny(long timeout, TimeUnit unit, String... queueNames) throws InterruptedException;

    /**
     * 按 {@link DequeMoveArgs} 在双端队列间原子迁移元素。
     *
     * @param timeout 最长等待时间
     * @param args 迁移参数
     * @return 迁移的元素；超时可能为 {@code null}
     */
    V move(Duration timeout, DequeMoveArgs args);

    /**
     * 已废弃，请改用 {@link #subscribeOnFirstElements(Function)}。
     *
     * @param consumer 队列元素监听器
     * @return 监听器 ID
     */
    @Deprecated
    int subscribeOnFirstElements(Consumer<V> consumer);

    /**
     * 已废弃，请改用 {@link #subscribeOnLastElements(Function)}。
     *
     * @param consumer 队列元素监听器
     * @return 监听器 ID
     */
    @Deprecated
    int subscribeOnLastElements(Consumer<V> consumer);

    /**
     * 订阅队头新元素；内部循环调用 {@link #takeFirstAsync()} 取元素。
     * <p>注意：监听器内勿调用阻塞方法。
     *
     * @param consumer 异步元素处理器
     * @return 监听器 ID
     */
    int subscribeOnFirstElements(Function<V, CompletionStage<Void>> consumer);

    /**
     * 订阅队尾新元素；内部循环调用 {@link #takeLastAsync()} 取元素。
     * <p>注意：监听器内勿调用阻塞方法。
     *
     * @param consumer 异步元素处理器
     * @return 监听器 ID
     */
    int subscribeOnLastElements(Function<V, CompletionStage<Void>> consumer);

}
