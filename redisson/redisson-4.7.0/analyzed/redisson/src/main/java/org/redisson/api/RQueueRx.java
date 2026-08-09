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
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * 队列对象的 RxJava3 API。
 * <p>各方法返回 {@link Maybe}、{@link Single} 等 RxJava 类型。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RQueueRx<V> extends RCollectionRx<V> {

    /**
     * 异步查看队首元素（不移除）。
     * 
     * @return 队首元素；队列为空时为 {@code null}
     */
    Maybe<V> peek();

    /**
     * 异步取出并移除队首元素。
     *
     * @return 队首元素；队列为空时为 {@code null}
     */
    Maybe<V> poll();

    /**
     * 取出并移除队首最多 {@code limit} 个元素。
     *
     * @return 队首元素列表
     */
    Single<List<V>> poll(int limit);

    /**
     * 将指定元素插入队列尾部。
     *
     * @param e 待添加元素
     * @return 成功则为 {@code true}，否则 {@code false}
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     */
    Single<Boolean> offer(V e);

    /**
     * 取出本队列队尾最后一个可用元素，并将其插入 {@code queueName} 队列队首。
     *
     * @param queueName 目标队列名称
     * @return 队尾元素；超时无可用元素时为 {@code null}
     */
    Maybe<V> pollLastAndOfferFirstTo(String queueName);

    /**
     * 一次性读取队列中的全部元素
     * 
     * @return 元素列表
     */
    Single<List<V>> readAll();

    /**
     * 取出队首元素并追加到 {@code queueName} 队尾，返回已移动的元素列表。
     * <p>
     * Requires <b>Redis 8.10.0 and higher.</b>
     *
     * @param args 参数对象
     * @return 已移动的元素列表
     */
    Single<List<V>> move(QueueMoveElementsArgs args);

    /**
     * 注册对象事件监听器
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * @see org.redisson.api.listener.ListAddListener
     * @see org.redisson.api.listener.ListRemoveListener
     *
     * @param listener - object event listener
     * @return 监听器 ID
     */
    Single<Integer> addListener(ObjectListener listener);

    /**
     * 返回 {@code element} 在队列中的索引；未找到则返回 -1
     *
     * @param element 待查找元素
     * @return 元素索引；未找到则为 -1
     */
    Single<Integer> indexOf(V element);


}
