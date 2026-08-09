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
package org.apache.rocketmq.proxy.service.route;

import com.google.common.base.MoreObjects;
import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.route.QueueData;

import static org.apache.rocketmq.proxy.service.route.MessageQueuePenalizer.selectLeastPenaltyWithPriority;
import static org.apache.rocketmq.proxy.service.route.MessageQueuePriorityProvider.buildPriorityGroups;

/**
 * 消息队列选择器：基于主题路由构建读/写队列列表，支持轮询、惩罚因子与优先级分组选择。
 */
public class MessageQueueSelector {
    /** Broker 代理队列标识，queueId 为 -1 表示按 Broker 粒度路由。 */
    private static final int BROKER_ACTING_QUEUE_ID = -1;

    // 普通队列：每个 Broker 下 queueId >= 0 的多个队列
    private final List<AddressableMessageQueue> queues = new ArrayList<>();
    // Broker 代理队列：每个 Broker 仅一条 queueId 为 -1 的队列
    private final List<AddressableMessageQueue> brokerActingQueues = new ArrayList<>();
    private final Map<String, AddressableMessageQueue> brokerNameQueueMap = new ConcurrentHashMap<>();
    private final AtomicInteger queueIndex;
    private final AtomicInteger brokerIndex;
    private final List<MessageQueuePenalizer<AddressableMessageQueue>> penalizers = new ArrayList<>();

    // 按优先级升序分组（数值越小优先级越高）
    private final List<List<AddressableMessageQueue>> queuesWithPriority;
    private final List<List<AddressableMessageQueue>> brokerActingQueuesWithPriority;

    /** 以主题路由与读/写模式构造选择器，使用默认优先级提供者。 */
    public MessageQueueSelector(TopicRouteWrapper topicRouteWrapper, boolean read) {
        this(topicRouteWrapper, read, null);
    }

    /**
     * 构造选择器并指定优先级分组策略。
     *
     * @param topicRouteWrapper 主题路由包装
     * @param read 为 true 时构建可读队列，否则构建可写队列
     * @param priorityProvider 队列优先级提供者，可为 null
     */
    public MessageQueueSelector(TopicRouteWrapper topicRouteWrapper, boolean read,
        MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider) {
        if (read) {
            this.queues.addAll(buildRead(topicRouteWrapper));
        } else {
            this.queues.addAll(buildWrite(topicRouteWrapper));
        }
        buildBrokerActingQueues(topicRouteWrapper.getTopicName(), this.queues);
        Random random = new Random();
        this.queueIndex = new AtomicInteger(random.nextInt());
        this.brokerIndex = new AtomicInteger(random.nextInt());

        if (priorityProvider == null) {
            priorityProvider = new DefaultMessageQueuePriorityProvider();
        }
        this.queuesWithPriority = buildPriorityGroups(queues, priorityProvider);
        this.brokerActingQueuesWithPriority = buildPriorityGroups(brokerActingQueues, priorityProvider);
    }

    /** 根据可读权限与主 Broker 地址构建读队列列表。 */
    private static List<AddressableMessageQueue> buildRead(TopicRouteWrapper topicRoute) {
        Set<AddressableMessageQueue> queueSet = new HashSet<>();
        List<QueueData> qds = topicRoute.getQueueDatas();
        if (qds == null) {
            return new ArrayList<>();
        }

        for (QueueData qd : qds) {
            if (PermName.isReadable(qd.getPerm())) {
                String brokerAddr = topicRoute.getMasterAddrPrefer(qd.getBrokerName());
                if (brokerAddr == null) {
                    continue;
                }

                for (int i = 0; i < qd.getReadQueueNums(); i++) {
                    AddressableMessageQueue mq = new AddressableMessageQueue(
                        new MessageQueue(topicRoute.getTopicName(), qd.getBrokerName(), i),
                        brokerAddr);
                    queueSet.add(mq);
                }
            }
        }

        return queueSet.stream().sorted().collect(Collectors.toList());
    }

    /** 根据顺序主题配置或可写权限构建写队列列表。 */
    private static List<AddressableMessageQueue> buildWrite(TopicRouteWrapper topicRoute) {
        Set<AddressableMessageQueue> queueSet = new HashSet<>();
        // 顺序主题：按 orderTopicConf 解析 Broker 与队列数
        if (StringUtils.isNotBlank(topicRoute.getOrderTopicConf())) {
            String[] brokers = topicRoute.getOrderTopicConf().split(";");
            for (String broker : brokers) {
                String[] item = broker.split(":");
                String brokerName = item[0];
                String brokerAddr = topicRoute.getMasterAddr(brokerName);
                if (brokerAddr == null) {
                    continue;
                }

                int nums = Integer.parseInt(item[1]);
                for (int i = 0; i < nums; i++) {
                    AddressableMessageQueue mq = new AddressableMessageQueue(
                        new MessageQueue(topicRoute.getTopicName(), brokerName, i),
                        brokerAddr);
                    queueSet.add(mq);
                }
            }
        } else {
            List<QueueData> qds = topicRoute.getQueueDatas();
            if (qds == null) {
                return new ArrayList<>();
            }

            for (QueueData qd : qds) {
                if (PermName.isWriteable(qd.getPerm())) {
                    String brokerAddr = topicRoute.getMasterAddr(qd.getBrokerName());
                    if (brokerAddr == null) {
                        continue;
                    }

                    for (int i = 0; i < qd.getWriteQueueNums(); i++) {
                        AddressableMessageQueue mq = new AddressableMessageQueue(
                            new MessageQueue(topicRoute.getTopicName(), qd.getBrokerName(), i),
                            brokerAddr);
                        queueSet.add(mq);
                    }
                }
            }
        }

        return queueSet.stream().sorted().collect(Collectors.toList());
    }

    private void buildBrokerActingQueues(String topic, List<AddressableMessageQueue> normalQueues) {
        for (AddressableMessageQueue mq : normalQueues) {
            AddressableMessageQueue brokerActingQueue = new AddressableMessageQueue(
                new MessageQueue(topic, mq.getBrokerName(), BROKER_ACTING_QUEUE_ID),
                mq.getBrokerAddr());

            if (!brokerActingQueues.contains(brokerActingQueue)) {
                brokerActingQueues.add(brokerActingQueue);
                brokerNameQueueMap.put(brokerActingQueue.getBrokerName(), brokerActingQueue);
            }
        }

        Collections.sort(brokerActingQueues);
    }

    /** 按 Broker 名称返回对应的 Broker 代理队列。 */
    public AddressableMessageQueue getQueueByBrokerName(String brokerName) {
        return this.brokerNameQueueMap.get(brokerName);
    }

    /** 轮询选择一条队列；onlyBroker 为 true 时仅从 Broker 代理队列中选择。 */
    public AddressableMessageQueue selectOne(boolean onlyBroker) {
        int nextIndex = onlyBroker ? brokerIndex.getAndIncrement() : queueIndex.getAndIncrement();
        return selectOneByIndex(nextIndex, onlyBroker);
    }

    /** 优先按惩罚因子与优先级选择，无可用结果时回退到轮询。 */
    public AddressableMessageQueue selectOneByPipeline(boolean onlyBroker) {
        if (CollectionUtils.isNotEmpty(penalizers)) {
            Pair<AddressableMessageQueue, Integer> queueAndPenalty;
            if (onlyBroker) {
                queueAndPenalty = selectLeastPenaltyWithPriority(brokerActingQueuesWithPriority, penalizers, brokerIndex);
            } else {
                queueAndPenalty = selectLeastPenaltyWithPriority(queuesWithPriority, penalizers, queueIndex);
            }
            if (queueAndPenalty != null && queueAndPenalty.getLeft() != null) {
                return queueAndPenalty.getLeft();
            }
        }

        // 未启用延迟惩罚或未选出队列时，按索引轮询
        return selectOne(onlyBroker);
    }

    /** 选择与 last 不同的下一条队列，最多尝试 count 次。 */
    public AddressableMessageQueue selectNextOne(AddressableMessageQueue last) {
        boolean onlyBroker = last.getQueueId() < 0;
        AddressableMessageQueue newOne = last;
        int count = onlyBroker ? brokerActingQueues.size() : queues.size();

        for (int i = 0; i < count; i++) {
            newOne = selectOne(onlyBroker);
            if (!newOne.getBrokerName().equals(last.getBrokerName()) || newOne.getQueueId() != last.getQueueId()) {
                break;
            }
        }
        return newOne;
    }

    /** 按 index 取模选择队列。 */
    public AddressableMessageQueue selectOneByIndex(int index, boolean onlyBroker) {
        if (onlyBroker) {
            if (brokerActingQueues.isEmpty()) {
                return null;
            }
            return brokerActingQueues.get(IntMath.mod(index, brokerActingQueues.size()));
        }

        if (queues.isEmpty()) {
            return null;
        }
        return queues.get(IntMath.mod(index, queues.size()));
    }

    public List<AddressableMessageQueue> getQueues() {
        return queues;
    }

    public List<AddressableMessageQueue> getBrokerActingQueues() {
        return brokerActingQueues;
    }

    /** 注册队列惩罚器，用于延迟/故障容忍路由。 */
    public void addPenalizer(MessageQueuePenalizer<AddressableMessageQueue> penalizer) {
        if (penalizer != null) {
            this.penalizers.add(penalizer);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageQueueSelector)) {
            return false;
        }
        MessageQueueSelector queue = (MessageQueueSelector) o;
        return Objects.equals(queues, queue.queues) &&
            Objects.equals(brokerActingQueues, queue.brokerActingQueues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queues, brokerActingQueues);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("queues", queues)
            .add("brokerActingQueues", brokerActingQueues)
            .add("brokerNameQueueMap", brokerNameQueueMap)
            .add("queueIndex", queueIndex)
            .add("brokerIndex", brokerIndex)
            .toString();
    }
}
