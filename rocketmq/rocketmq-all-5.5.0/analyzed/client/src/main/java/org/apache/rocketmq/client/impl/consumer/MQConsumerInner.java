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
package org.apache.rocketmq.client.impl.consumer;

import java.util.Set;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 消费者内部接口：供 {@link MQClientInstance} 与 rebalance/pull 服务调用。
 */
public interface MQConsumerInner {
    /** 消费组名。 */
    String groupName();

    /** 消息模式（集群/广播）。 */
    MessageModel messageModel();

    /** 消费类型（主动 pull / 被动 push）。 */
    ConsumeType consumeType();

    /** 首次消费起始位置策略。 */
    ConsumeFromWhere consumeFromWhere();

    /** 当前订阅集合。 */
    Set<SubscriptionData> subscriptions();

    /** 执行 rebalance。 */
    void doRebalance();

    /** 尝试 rebalance，返回是否已均衡。 */
    boolean tryRebalance();

    /** 持久化消费位点。 */
    void persistConsumerOffset();

    /** 更新 topic 的路由/队列订阅信息。 */
    void updateTopicSubscribeInfo(final String topic, final Set<MessageQueue> info);

    /** 判断 topic 订阅信息是否需要更新。 */
    boolean isSubscribeTopicNeedUpdate(final String topic);

    /** 是否单元化模式。 */
    boolean isUnitMode();

    /** 返回消费者运行时信息快照。 */
    ConsumerRunningInfo consumerRunningInfo();
}
