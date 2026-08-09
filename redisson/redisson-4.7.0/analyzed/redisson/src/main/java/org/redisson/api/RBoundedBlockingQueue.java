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

/**
 * 已废弃：请改用带队列大小限制功能的 {@link RReliableQueue}。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
@Deprecated
public interface RBoundedBlockingQueue<V> extends RBlockingQueue<V>, RBoundedBlockingQueueAsync<V> {

    /**
     * 仅在尚未设置容量时设置队列容量。
     *
     * @param capacity 队列容量
     * @return 设置成功为 {@code true}，容量已存在为 {@code false}
     */
    boolean trySetCapacity(int capacity);
    
}
