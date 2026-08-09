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
package org.redisson.api.queue.event;

import org.redisson.api.queue.QueueOperation;

/**
 * 队列操作禁用事件监听器。
 * 当某队列操作切换为禁用状态时触发。
 *
 * @author Nikita Koksharov
 *
 */
public interface DisabledOperationEventListener extends QueueEventListener {

    /**
     * 队列操作被禁用时回调。
     *
     * @param queueName 队列名称
     * @param operation 被禁用的操作类型
     */
    void onDisabled(String queueName, QueueOperation operation);

}
