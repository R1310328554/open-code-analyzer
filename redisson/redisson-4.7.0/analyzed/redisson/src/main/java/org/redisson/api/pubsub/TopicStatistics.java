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
 * 可靠 PubSub 主题的统计信息接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface TopicStatistics extends Serializable {

    /**
     * 返回主题名称。
     *
     * @return name
     */
    String getTopicName();

    /**
     * 返回主题中延迟投递的消息数量。
     * <p>
     * 延迟消息已排期在未来投递，当前尚不可消费。
     *
     * @return 延迟消息数
     */
    long getDelayedMessagesCount();

    /**
     * 返回本主题下现有订阅的数量。
     *
     * @return 订阅数量
     */
    long getSubscriptionsCount();

    /**
     * 返回本主题已发布的消息总数。
     *
     * @return 已发布消息数
     */
    long getPublishedMessagesCount();

}
