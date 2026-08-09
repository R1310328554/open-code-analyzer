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
 * 基于 Redis 实现的优先级阻塞队列。
 * <p>组合 {@link RBlockingQueue} 的阻塞语义与 {@link RPriorityQueue} 的优先级排序能力。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RPriorityBlockingQueue<V> extends RBlockingQueue<V>, RPriorityQueue<V> {

}
