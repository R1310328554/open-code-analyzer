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
package org.redisson.api.queue;

import org.redisson.api.SyncArgs;

/**
 * 定义队列消息负确认（NACK）操作的参数。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueNegativeAckArgs extends SyncArgs<QueueNegativeAckArgs> {

    /**
     * 标记客户端处理失败，消息将被重新投递。
     *
     * @param ids 消息 ID
     * @return 参数对象
     */
    static FailedAckArgs failed(String... ids) {
        return new QueueNegativeAckParams(ids, true);
    }

    /**
     * 标记客户端已处理但业务拒绝该消息。
     * 消息将被移除；若已配置死信队列（DLQ）则转入 DLQ。
     *
     * @param ids 消息 ID
     * @return 参数对象
     */
    static QueueNegativeAckArgs rejected(String... ids) {
        return new QueueNegativeAckParams(ids, false);
    }

}
