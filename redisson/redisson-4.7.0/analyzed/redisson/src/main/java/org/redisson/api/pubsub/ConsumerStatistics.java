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

import java.io.Serializable;

/**
 * 可靠 PubSub 消费者的统计信息接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface ConsumerStatistics extends Serializable {

    /**
     * 返回消费者名称。
     *
     * @return 名称
     */
    String getConsumerName();

    /**
     * 返回尚未确认、等待 ack 的消息数量。
     *
     * @return 未确认消息数
     */
    long getUnacknowledgedMessagesCount();

    /**
     * 返回消费者已成功确认的消息总数。
     *
     * @return 已确认消息数
     */
    long getAcknowledgedMessagesCount();

    /**
     * 返回消费者负向确认（nack）的消息总数。
     *
     * @return 负向确认消息数
     */
    long getNegativelyAcknowledgedMessagesCount();

}
