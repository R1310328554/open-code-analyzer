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
 * 可监听对象事件的异步 API 接口。
 * <p>各方法返回 {@link RFuture}，用于异步注册/移除事件监听器。
 *
 * @author seakider
 */
public interface RObservableAsync {
    /**
     * 注册对象事件监听器
     *
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

    /**
     * 移除对象事件监听器
     *
     * @param listenerId 监听器 ID
     */
    RFuture<Void> removeListenerAsync(int listenerId);
}
