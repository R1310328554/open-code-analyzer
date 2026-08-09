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

import java.util.List;

/**
 * {@link RDeque} 的异步 API。
 * <p>各方法返回 {@link RFuture}。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RDequeAsync<V> extends RQueueAsync<V> {

    /**
     * 在已存在的双端队列队头追加元素。
     *
     * @param elements 元素集合
     * @return 列表长度
     */
    RFuture<Integer> addFirstIfExistsAsync(V... elements);

    /**
     * 在双端队列队头批量追加元素。
     *
     * @param elements 元素集合
     * @return 双端队列长度
     */
    RFuture<Integer> addFirstAsync(V... elements);

    /**
     * 在已存在的双端队列队尾追加元素。
     *
     * @param elements 元素集合
     * @return 列表长度
     */
    RFuture<Integer> addLastIfExistsAsync(V... elements);

    /**
     * 在双端队列队尾批量追加元素。
     *
     * @param elements 元素集合
     * @return 双端队列长度
     */
    RFuture<Integer> addLastAsync(V... elements);

    /**
     * 移除元素最后一次出现的实例。
     * 
     * @param o 待移除元素
     * @return 见方法说明
     */
    RFuture<Boolean> removeLastOccurrenceAsync(Object o);

    /**
     * 移除并返回双端队列最后一个元素。
     * 双端队列为空时返回 {@code null}。
     * 
     * @return 元素
     */
    RFuture<V> removeLastAsync();

    /**
     * 移除并返回双端队列第一个元素。
     * 双端队列为空时返回 {@code null}。
     * 
     * @return 元素
     */
    RFuture<V> removeFirstAsync();

    /**
     * 移除元素第一次出现的实例。
     * 
     * @param o 待移除元素
     * @return 见方法说明
     */
    RFuture<Boolean> removeFirstOccurrenceAsync(Object o);

    /**
     * 在队头添加元素（栈式 push）。
     * 
     * @param e 待添加元素
     * @return void
     */
    RFuture<Void> pushAsync(V e);

    /**
     * 弹出队头元素；空队列返回 null。
     * 双端队列为空时返回 {@code null}。
     * 
     * @return 元素
     */
    RFuture<V> popAsync();

    /**
     * 弹出队尾元素；空队列返回 null。
     * 双端队列为空时返回 {@code null}。
     * 
     * @return 元素
     */
    RFuture<V> pollLastAsync();

    /**
     * Retrieves and removes element at the head of this deque.
     * Returns <code>null</code> if there are no elements in deque.
     * 
     * @return element
     */
    RFuture<V> pollFirstAsync();

    /**
     * 查看队尾元素但不移除。
     * 双端队列为空时返回 {@code null}。
     * 
     * @return 元素
     */
    RFuture<V> peekLastAsync();

    /**
     * 查看队头元素但不移除。
     * 双端队列为空时返回 {@code null}。
     * 
     * @return 元素
     */
    RFuture<V> peekFirstAsync();

    /**
     * 在队尾添加元素。
     * 
     * @param e 待添加元素
     * @return 见方法说明
     */
    RFuture<Boolean> offerLastAsync(V e);

    /**
     * Returns element at the tail of this deque 
     * or <code>null</code> if there are no elements in deque.
     * 
     * @return element
     */
    RFuture<V> getLastAsync();

    /**
     * 在队尾添加元素。
     * 
     * @param e 待添加元素
     * @return void
     */
    RFuture<Void> addLastAsync(V e);

    /**
     * Adds element at the head of this deque.
     * 
     * @param e - element to add
     * @return void
     */
    RFuture<Void> addFirstAsync(V e);

    /**
     * 在队头添加元素（栈式 push）。
     * 
     * @param e 待添加元素
     * @return 见方法说明
     */
    RFuture<Boolean> offerFirstAsync(V e);

    /**
     * 拉取并移除至多 limit 个队头元素。
     * 拉取数量受 {@code limit} 参数限制。
     *
     * @return 队头元素列表
     */
    RFuture<List<V>> pollFirstAsync(int limit);

    /**
     * 拉取并移除至多 limit 个队尾元素。
     * 拉取数量受 {@code limit} 参数限制。
     *
     * @return 队尾元素列表
     */
    RFuture<List<V>> pollLastAsync(int limit);

    /**
     * 将元素从本双端队列原子移动到目标队列并返回被移动元素。
     * 
     * <p>
     * 用法示例：
     * <pre>
     * V element = deque.move(DequeMoveArgs.pollLast()
     *                                 .addFirstTo("deque2"));
     * </pre>
     * <pre>
     * V elements = deque.move(DequeMoveArgs.pollFirst()
     *                                 .addLastTo("deque2"));
     * </pre>
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param args 移动参数
     * @return 被移动的元素
     */
    RFuture<V> moveAsync(DequeMoveArgs args);

    /**
     * 注册双端队列对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.ListAddListener
     * @see org.redisson.api.listener.ListRemoveListener
     * @see org.redisson.api.listener.ListTrimListener
     * @see org.redisson.api.listener.ListSetListener
     * @see org.redisson.api.listener.ListInsertListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * @see org.redisson.api.listener.DequeAddFirstListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

}
