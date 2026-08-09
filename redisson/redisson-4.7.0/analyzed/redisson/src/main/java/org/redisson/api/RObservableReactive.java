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

import reactor.core.publisher.Mono;

/**
 * 可监听对象事件的 Reactor 响应式 API 接口。
 * <p>各方法返回 {@link Mono}，用于响应式注册/移除事件监听器。
 *
 * @author seakider
 */
public interface RObservableReactive {
    /**
     * 注册对象事件监听器
     *
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    Mono<Integer> addListener(ObjectListener listener);

    /**
     * 移除对象事件监听器
     *
     * @param listenerId 监听器 ID
     * @return 无返回值
     */
    Mono<Void> removeListener(int listenerId);
}
