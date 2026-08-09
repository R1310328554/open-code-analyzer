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

package org.apache.rocketmq.broker.offset;

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.pop.orderly.QueueLevelConsumerManager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的 Lite Topic 顺序消费信息管理器。
 * 设计取舍：Lite 场景对严格顺序要求较低；兼容 PushConsumer 时可容忍部分顺序控制失效；
 * 避免持久化 I/O 开销。后续可能进一步优化结构与内存占用。
 */
public class MemoryConsumerOrderInfoManager extends QueueLevelConsumerManager {

    public MemoryConsumerOrderInfoManager(BrokerController brokerController) {
        super(brokerController);
    }

    @Override
    protected void updateLockFreeTimestamp(String topic, String group, int queueId, OrderInfo orderInfo) {
        if (this.getConsumerOrderInfoLockManager() != null) {
            // 取最大 lock-free 时间戳，避免意外长时间阻塞
            this.getConsumerOrderInfoLockManager().updateLockFreeTimestamp(
                topic, group, queueId, orderInfo.getMaxLockFreeTimestamp());
        }
    }

    /** Pop 挂起队列：校验 popTime 后递减 offset 消费计数并刷新不可见时间与 lock-free 戳。 */
    public void suspendQueue(String topic, String group, int queueId, long popTime, long visibilityTimeout) {
        ConcurrentHashMap<Integer, OrderInfo> orderInfoMap = this.getTable().get(buildKey(topic, group));
        if (null == orderInfoMap) {
            return;
        }
        OrderInfo orderInfo = orderInfoMap.get(queueId);
        if (null == orderInfo) {
            return;
        }
        if (popTime != orderInfo.getPopTime()) {
            log.warn("suspendQueue, popTime not match. {}, {}, {}, popTime:{}", topic, group, orderInfo, popTime);
            return;
        }

        if (orderInfo.getOffsetConsumedCount() != null) {
            orderInfo.getOffsetConsumedCount().replaceAll((key, value) -> value > 0 ? value - 1 : value);
        }
        orderInfo.setOffsetNextVisibleTime(null);
        orderInfo.setInvisibleTime(visibilityTimeout - orderInfo.getPopTime());
        updateLockFreeTimestamp(topic, group, queueId, orderInfo);
    }

    @Override
    public void persist() {
        // 纯内存实现，persist 为空操作。
    }
}
