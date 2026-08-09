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
 * Redis Pub/Sub 模式订阅（pattern subscribe）状态变更的基类监听器。
 * <p>
 * 提供 {@link PatternStatusListener} 的空实现，子类可按需覆盖
 * {@link #onPSubscribe(String)} 与 {@link #onPUnsubscribe(String)}。
 *
 * @author Nikita Koksharov
 *
 * @see org.redisson.api.RTopic
 */
public class BasePatternStatusListener implements PatternStatusListener {

    /** 模式订阅成功时的回调，默认空实现。 */
    @Override
    public void onPSubscribe(String channel) {
    }

    /** 取消模式订阅时的回调，默认空实现。 */
    @Override
    public void onPUnsubscribe(String channel) {
    }

}
