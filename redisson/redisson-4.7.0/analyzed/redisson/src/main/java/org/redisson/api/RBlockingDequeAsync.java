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
import java.util.concurrent.TimeUnit;

/**
 * {@link RBlockingDeque} 的异步 API 接口。
 * <p>各方法返回 {@link RFuture}，支持双端阻塞操作的非阻塞调用。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
public interface RBlockingDequeAsync<V> extends RDequeAsync<V>, RBlockingQueueAsync<V> {

    /**
     * 异步地从指定队列集合（含自身）中拉取首个可用<b>队头</b>元素并移除。
     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。
     *
     * @param queueNames 候选队列名列表
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 取到的元素；超时为 {@code null}
     */
    RFuture<V> pollFirstFromAnyAsync(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 异步地从指定队列集合（含自身）中拉取首个可用<b>队尾</b>元素并移除。
     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。
     *
     * @param queueNames 候选队列名列表
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 取到的元素；超时为 {@code null}
     */
    RFuture<V> pollLastFromAnyAsync(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 阻塞地将元素插入队头。
     *
     * @param e 待插入元素
     */
    RFuture<Void> putFirstAsync(V e);

    /**
     * 阻塞地将元素插入队尾。
     *
     * @param e 待插入元素
     */
    RFuture<Void> putLastAsync(V e);

    /**
     * 从队尾取出并移除元素；必要时阻塞等待至多 {@code timeout}。
     *
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队尾元素；超时为 {@code null}
     */
    RFuture<V> pollLastAsync(long timeout, TimeUnit unit);
    
    /**
     * 阻塞地从队尾取出并移除元素，直至有元素可用。
     *
     * @return 队尾元素
     */
    RFuture<V> takeLastAsync();

    /**
     * 从队头取出并移除元素；必要时阻塞等待至多 {@code timeout}。
     *
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队头元素；超时为 {@code null}
     */
    RFuture<V> pollFirstAsync(long timeout, TimeUnit unit);

    /**
     * 阻塞地从队头取出并移除元素，直至有元素可用。
     *
     * @return 队头元素
     */
    RFuture<V> takeFirstAsync();

    /**
     * 异步地按 {@link DequeMoveArgs} 在双端队列间迁移元素。
     *
     * @param timeout 最长等待时间
     * @param args 迁移参数
     * @return 迁移结果的 {@link RFuture}
     */
    RFuture<V> moveAsync(Duration timeout, DequeMoveArgs args);

}
