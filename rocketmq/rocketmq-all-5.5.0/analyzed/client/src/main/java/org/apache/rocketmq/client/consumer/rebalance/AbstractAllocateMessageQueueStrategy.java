/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.client.consumer.rebalance;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 消息队列分配策略抽象基类：提供 consumerId 与队列列表的前置校验。
 */
public abstract class AbstractAllocateMessageQueueStrategy implements AllocateMessageQueueStrategy {

    private static final Logger log = LoggerFactory.getLogger(AbstractAllocateMessageQueueStrategy.class);

    /**
     * 校验分配参数：currentCID 非空、队列与消费者列表非空，且 currentCID 在 cidAll 中。
     *
     * @return 参数合法且 currentCID 在列表中返回 true，否则 false
     */
    public boolean check(String consumerGroup, String currentCID, List<MessageQueue> mqAll,
        List<String> cidAll) {
        if (StringUtils.isEmpty(currentCID)) {
            throw new IllegalArgumentException("currentCID is empty");
        }
        if (CollectionUtils.isEmpty(mqAll)) {
            throw new IllegalArgumentException("mqAll is null or mqAll empty");
        }
        if (CollectionUtils.isEmpty(cidAll)) {
            throw new IllegalArgumentException("cidAll is null or cidAll empty");
        }

        if (!cidAll.contains(currentCID)) {
            // 当前 consumerId 不在在线列表中，可能是 rebalance 时序问题
            log.info("[BUG] ConsumerGroup: {} The consumerId: {} not in cidAll: {}",
                consumerGroup,
                currentCID,
                cidAll);
            return false;
        }

        return true;
    }
}
