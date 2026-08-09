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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Async interface for 基于 Redis 的 {@link java.util.concurrent.TransferQueue} 同步 API；支持阻塞入队与零缓冲元素转移（生产者直接交给等待中的消费者）。
 *
 * @author Nikita Koksharov
 *
 */
public interface RTransferQueueAsync<V> extends RBlockingQueueAsync<V> {

        /**
     * 尝试将元素转移给正在等待的消费者（调用 {@link #takeAsync} 或 {@link #pollAsync} 的线程）；无等待消费者时立即返回 false。
     *
     * @param e 待转移元素
     * @return 转移成功则为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> tryTransferAsync(V e);

    /**
     * 将元素转移给正在等待的消费者；若无等待者则阻塞直到有消费者就绪。
     *
     * @param e 待转移元素
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    RFuture<Void> transferAsync(V e);

    /**
     * 将元素转移给正在等待的消费者；在指定超时内等待消费者，超时则返回 false。
     *
     * @param e 待转移元素
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 转移成功则为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> tryTransferAsync(V e, long timeout, TimeUnit unit);

    /**
     * 一次性返回队列全部元素
     *
     * @return 队列元素列表
     */
    List<V> readAll();

}
