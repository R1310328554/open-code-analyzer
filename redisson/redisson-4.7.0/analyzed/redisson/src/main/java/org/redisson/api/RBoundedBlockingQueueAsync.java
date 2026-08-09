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

/**
 * 已废弃：请改用带队列大小限制功能的 {@link RReliableQueue}。
 *
 * @author Nikita Koksharov
 * @param <V> 集合元素类型
 */
@Deprecated
public interface RBoundedBlockingQueueAsync<V> extends RBlockingQueueAsync<V> {

    /**
     * 仅在尚未设置容量时设置队列容量。
     *
     * @param capacity 队列容量
     * @return 设置成功为 {@code true}，容量已存在为 {@code false}
     */
    RFuture<Boolean> trySetCapacityAsync(int capacity);

    /**
     * 异步尝试将元素插入队列；必要时等待至多 {@code timeout} 直至有可用空间。
     *
     * @param e 待插入元素
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 插入成功为 {@code true}，超时为 {@code false}
     * @throws ClassCastException 元素类型不允许加入本队列
     * @throws NullPointerException 元素为 {@code null}
     */
    RFuture<Boolean> offerAsync(V e, long timeout, TimeUnit unit);

}
