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
package org.redisson.api.pubsub;

import org.redisson.api.SyncArgs;

/**
 * 消息负向确认（nack）操作的参数接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface MessageNegativeAckArgs extends SyncArgs<MessageNegativeAckArgs> {

    /**
     * 标记客户端处理消息失败，消息将被重新投递。
     *
     * @param ids 消息 ID
     * @return 参数对象
     */
    static FailedAckArgs failed(String... ids) {
        return new MessageNegativeAckParams(ids, true);
    }

    /**
     * 标记客户端已处理消息但业务未接受，消息将被移除；
     * 若已配置死信主题（Dead Letter Topic），则转入死信。
     *
     * @param ids 消息 ID
     * @return 参数对象
     */
    static MessageNegativeAckArgs rejected(String... ids) {
        return new MessageNegativeAckParams(ids, false);
    }

}
