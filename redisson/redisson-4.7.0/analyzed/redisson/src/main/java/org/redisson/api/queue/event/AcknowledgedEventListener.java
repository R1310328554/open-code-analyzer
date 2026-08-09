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

import java.util.List;

/**
 * 队列消息确认（Ack）事件监听器。
 * 当消费者成功确认队列中的消息时触发。
 *
 * @author Nikita Koksharov
 *
 */
public interface AcknowledgedEventListener extends QueueEventListener {

    /**
     * 消息被消费者确认时回调。
     *
     * @param ids 已确认的消息 ID 列表
     */
    void onAcknowledged(List<String> ids);

}
