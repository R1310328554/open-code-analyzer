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
package org.redisson.api.listener;

/**
 * Redis Pub/Sub 频道订阅状态变更的基类监听器。
 * <p>
 * 提供 {@link StatusListener} 的空实现，子类可按需覆盖
 * {@link #onSubscribe(String)} 与 {@link #onUnsubscribe(String)}。
 *
 * @author Nikita Koksharov
 *
 * @see org.redisson.api.RTopic
 */
public class BaseStatusListener implements StatusListener {

    /** 频道订阅成功时的回调，默认空实现。 */
    @Override
    public void onSubscribe(String channel) {
    }

    /** 取消频道订阅时的回调，默认空实现。 */
    @Override
    public void onUnsubscribe(String channel) {
    }

}
