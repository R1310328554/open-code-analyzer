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

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * {@link BlockingQueue} 的 RxJava 响应式 API。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
public interface RBlockingQueueRx<V> extends RQueueRx<V> {

    /**
     * 响应式地从指定队列集合（含自身）中拉取首个可用队头元素并移除。
     *
     * @param queueNames 候选队列名列表
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队头元素；超时为 {@code null}
     */
    Maybe<V> pollFromAny(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 从指定队列集合（含自身）中阻塞拉取首个可用队头元素，并返回元素及其来源队列名。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    Maybe<Entry<String, V>> pollFromAnyWithName(Duration timeout, String... queueNames) throws InterruptedException;

    /**
     * 从指定队列集合（含自身）中阻塞拉取首个可用队尾元素，并返回元素及其来源队列名。
     *
     * @param queueNames 候选队列名（自身始终参与）
     * @param timeout 最长等待时间
     * @return 队列名与元素的 {@link Entry}；超时为 {@code null}
     * @throws InterruptedException 等待被中断时
     */
    Maybe<Entry<String, V>> pollLastFromAnyWithName(Duration timeout, String... queueNames) throws InterruptedException;

    /**
     * 从多个队列（含自身）批量拉取队头元素。
     * <p>需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count 每个队列最多拉取数量
     * @param queueNames 候选队列名
     * @return 队列名到元素列表的映射
     */
    Maybe<Map<String, List<V>>> pollFirstFromAny(Duration duration, int count, String... queueNames);

    /**
     * 从多个队列（含自身）批量拉取队尾元素。
     * <p>需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 最长等待时间
     * @param count 每个队列最多拉取数量
     * @param queueNames 候选队列名
     * @return 队列名到元素列表的映射
     */
    Maybe<Map<String, List<V>>> pollLastFromAny(Duration duration, int count, String... queueNames);

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
    Single<Integer> drainTo(Collection<? super V> c, int maxElements);

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
    Single<Integer> drainTo(Collection<? super V> c);

    /**
     * 从本队列队尾取出元素并插入目标队列队头；必要时阻塞等待。
     *
     * @param queueName 目标队列名
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 被移动的元素；超时为 {@code null}
     */
    Maybe<V> pollLastAndOfferFirstTo(String queueName, long timeout, TimeUnit unit);

    /**
     * 异步阻塞地从队头取出并移除元素；必要时等待至多 {@code timeout}。
     *
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 队头元素；超时为 {@code null}
     */
    Maybe<V> poll(long timeout, TimeUnit unit);

    /**
     * 异步阻塞地从队头取出并移除元素，直至有元素可用。
     *
     * @return 队头元素
     */
    Single<V> take();
    
    /**
     * 从候选队列（含自身）中阻塞取出队尾元素并插入目标队列队头。
     *
     * @param queueName 目标队列名
     * @return 被移动的元素
     */
    Single<V> takeLastAndOfferFirstTo(String queueName);

    /**
     * 异步阻塞地将元素插入队列；必要时等待直至有可用空间。
     *
     * @param e 待插入元素
     * @throws ClassCastException 元素类型不允许加入本队列
     * @throws NullPointerException 元素为 {@code null}
     * @throws IllegalArgumentException 元素属性不允许加入本队列
     */
    Completable put(V e);

    /**
     * 持续从队头阻塞取元素并移除，形成元素流。
     * <p>每次等待下一个元素可用后再发射。
     *
     * @return 元素流
     */
    Flowable<V> takeElements();

}
