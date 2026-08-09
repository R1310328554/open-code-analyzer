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

import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link RBlockingDeque} 的 Reactor 风格 API 接口。
 * <p>阻塞双端操作以 {@link Mono} 或 {@link Flux} 形式暴露。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
public interface RBlockingDequeReactive<V> extends RDequeReactive<V>, RBlockingQueueReactive<V> {

    /**
     * 响应式地从指定队列集合（含自身）中拉取首个可用<b>队头</b>元素并移除。
     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。
     *
     * @param queueNames 候选队列名列表
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 取到的元素；超时为 {@code null}
     */
    Mono<V> pollFirstFromAny(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 响应式地从指定队列集合（含自身）中拉取首个可用<b>队尾</b>元素并移除。
     * <p>在 {@code timeout} 内无元素可用则结果为 {@code null}。
     *
     * @param queueNames 候选队列名列表
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 取到的元素；超时为 {@code null}
     */
    Mono<V> pollLastFromAny(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 阻塞地将元素插入队头。
     *
     * @param e 待插入元素
     */
    Mono<Void> putFirst(V e);

    /**
     * 阻塞地将元素插入队尾。
     *
     * @param e 待插入元素
     */
    Mono<Void> putLast(V e);

    /**
     * 从队尾取出并移除元素；必要时阻塞等待至多 {@code timeout}。
     *
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队尾元素；超时为 {@code null}
     */
    Mono<V> pollLast(long timeout, TimeUnit unit);

    /**
     * 阻塞地从队尾取出并移除元素，直至有元素可用。
     *
     * @return 队尾元素
     */
    Mono<V> takeLast();

    /**
     * 从队头取出并移除元素；必要时阻塞等待至多 {@code timeout}。
     *
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队头元素；超时为 {@code null}
     */
    Mono<V> pollFirst(long timeout, TimeUnit unit);

    /**
     * 阻塞地从队头取出并移除元素，直至有元素可用。
     *
     * @return 队头元素
     */
    Mono<V> takeFirst();

    /**
     * 持续从队头阻塞取元素并移除，形成元素流。
     * <p>每次等待下一个元素可用后再发射。
     *
     * @return 队头元素流
     */
    Flux<V> takeFirstElements();
    
    /**
     * 持续从队尾阻塞取元素并移除，形成元素流。
     * <p>每次等待下一个元素可用后再发射。
     *
     * @return 队尾元素流
     */
    Flux<V> takeLastElements();
    
}
