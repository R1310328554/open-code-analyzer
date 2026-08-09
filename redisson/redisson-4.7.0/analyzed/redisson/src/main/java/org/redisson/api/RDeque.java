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

import java.util.Deque;
import java.util.List;

/**
 * {@link java.util.Deque} 的 Redis 分布式实现。
 * <p>支持双端插入/弹出、批量 poll 及跨队列原子移动（Redis 6.2+）。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RDeque<V> extends Deque<V>, RQueue<V>, RDequeAsync<V> {

    /**
     * 在已存在的双端队列队头追加元素。
     *
     * @param elements 元素集合
     * @return 列表长度
     */
    int addFirstIfExists(V... elements);

    /**
     * 在双端队列队头批量追加元素。
     *
     * @param elements 元素集合
     * @return 双端队列长度
     */
    int addFirst(V... elements);

    /**
     * 在已存在的双端队列队尾追加元素。
     *
     * @param elements 元素集合
     * @return 列表长度
     */
    int addLastIfExists(V... elements);

    /**
     * 在双端队列队尾批量追加元素。
     *
     * @param elements 元素集合
     * @return 双端队列长度
     */
    int addLast(V... elements);

    /**
     * 拉取并移除至多 limit 个队尾元素。
     * 拉取数量受 {@code limit} 参数限制。
     *
     * @return 队尾元素列表
     */
    List<V> pollLast(int limit);

    /**
     * 拉取并移除至多 limit 个队头元素。
     * 拉取数量受 {@code limit} 参数限制。
     *
     * @return 队头元素列表
     */
    List<V> pollFirst(int limit);

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
    V move(DequeMoveArgs args);

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
    int addListener(ObjectListener listener);

}
