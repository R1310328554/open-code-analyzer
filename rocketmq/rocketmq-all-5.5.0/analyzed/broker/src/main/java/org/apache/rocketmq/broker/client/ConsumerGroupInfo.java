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
package org.apache.rocketmq.broker.client;

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 单个消费者组的运行时视图：维护组内通道、订阅关系及消费模式等元数据。
 */
public class ConsumerGroupInfo {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    private final String groupName;
    private final ConcurrentMap<String/* Topic */, SubscriptionData> subscriptionTable =
        new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, ClientChannelInfo> channelInfoTable =
        new ConcurrentHashMap<>(16);
    private volatile ConsumeType consumeType;
    private volatile MessageModel messageModel;
    private volatile ConsumeFromWhere consumeFromWhere;
    private volatile long lastUpdateTimestamp = System.currentTimeMillis();

    /** 创建带完整消费参数的消费者组信息。 */
    public ConsumerGroupInfo(String groupName, ConsumeType consumeType, MessageModel messageModel,
        ConsumeFromWhere consumeFromWhere) {
        this.groupName = groupName;
        this.consumeType = consumeType;
        this.messageModel = messageModel;
        this.consumeFromWhere = consumeFromWhere;
    }

    /** 仅指定组名，消费参数后续由 {@link #updateChannel} 填充。 */
    public ConsumerGroupInfo(String groupName) {
        this.groupName = groupName;
    }

    /** 按 clientId 查找组内通道信息。 */
    public ClientChannelInfo findChannel(final String clientId) {
        Iterator<Entry<Channel, ClientChannelInfo>> it = this.channelInfoTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Channel, ClientChannelInfo> next = it.next();
            if (next.getValue().getClientId().equals(clientId)) {
                return next.getValue();
            }
        }

        return null;
    }

    /** 返回 topic → {@link SubscriptionData} 订阅表。 */
    public ConcurrentMap<String, SubscriptionData> getSubscriptionTable() {
        return subscriptionTable;
    }

    /** 按 Netty 通道查找客户端信息。 */
    public ClientChannelInfo findChannel(final Channel channel) {
        return this.channelInfoTable.get(channel);
    }

    /** 返回通道 → 客户端信息的并发映射表。 */
    public ConcurrentMap<Channel, ClientChannelInfo> getChannelInfoTable() {
        return channelInfoTable;
    }

    /** 返回组内全部 Netty 通道列表。 */
    public List<Channel> getAllChannel() {
        List<Channel> result = new ArrayList<>();

        result.addAll(this.channelInfoTable.keySet());

        return result;
    }

    /** 返回组内全部 clientId 列表。 */
    public List<String> getAllClientId() {
        List<String> result = new ArrayList<>();

        Iterator<Entry<Channel, ClientChannelInfo>> it = this.channelInfoTable.entrySet().iterator();

        while (it.hasNext()) {
            Entry<Channel, ClientChannelInfo> entry = it.next();
            ClientChannelInfo clientChannelInfo = entry.getValue();
            result.add(clientChannelInfo.getClientId());
        }

        return result;
    }

    /** 主动注销指定客户端通道；成功移除时返回 true。 */
    public boolean unregisterChannel(final ClientChannelInfo clientChannelInfo) {
        ClientChannelInfo old = this.channelInfoTable.remove(clientChannelInfo.getChannel());
        if (old != null) {
            log.info("unregister a consumer[{}] from consumerGroupInfo {}", this.groupName, old.toString());
            return true;
        }
        return false;
    }

    /** Netty 连接关闭/异常时移除通道并记录日志。 */
    public ClientChannelInfo doChannelCloseEvent(final String remoteAddr, final Channel channel) {
        final ClientChannelInfo info = this.channelInfoTable.remove(channel);
        if (info != null) {
            log.warn(
                "NETTY EVENT: remove not active channel[{}] from ConsumerGroupInfo groupChannelTable, consumer group: {}",
                info.toString(), groupName);
        }

        return info;
    }

    /**
     * 更新 {@link #channelInfoTable} 中的客户端通道，并同步组级消费参数。
     *
     * @param infoNew 新客户端的通道信息
     * @param consumeType 新客户端的消费类型
     * @param messageModel 新客户端的消息模式（CLUSTERING/BROADCASTING）
     * @param consumeFromWhere 客户端首次消费时的起始位点策略
     * @return 是否新增了连接（true 表示有新客户端接入）
     */
    public boolean updateChannel(final ClientChannelInfo infoNew, ConsumeType consumeType,
        MessageModel messageModel, ConsumeFromWhere consumeFromWhere) {
        boolean updated = false;
        this.consumeType = consumeType;
        this.messageModel = messageModel;
        this.consumeFromWhere = consumeFromWhere;

        ClientChannelInfo infoOld = this.channelInfoTable.get(infoNew.getChannel());
        if (null == infoOld) {
            ClientChannelInfo prev = this.channelInfoTable.put(infoNew.getChannel(), infoNew);
            if (null == prev) {
                log.info("new consumer connected, group: {} {} {} channel: {}", this.groupName, consumeType,
                    messageModel, infoNew.toString());
                updated = true;
            }

            infoOld = infoNew;
        } else {
            if (!infoOld.getClientId().equals(infoNew.getClientId())) {
                log.error(
                    "ConsumerGroupInfo: consumer channel exists in broker, but clientId is not the same one, "
                        + "group={}, old clientChannelInfo={}, new clientChannelInfo={}", groupName, infoOld.toString(),
                    infoNew.toString());
                this.channelInfoTable.put(infoNew.getChannel(), infoNew);
            }
        }

        this.lastUpdateTimestamp = System.currentTimeMillis();
        infoOld.setLastUpdateTimestamp(this.lastUpdateTimestamp);

        return updated;
    }

    /**
     * 批量更新订阅关系：新增、升级版本或移除不再订阅的 topic。
     *
     * @param subList {@link SubscriptionData} 集合
     * @return 订阅表是否发生变更
     */
    public boolean updateSubscription(final Set<SubscriptionData> subList) {
        boolean updated = false;
        Set<String> topicSet = new HashSet<>();
        for (SubscriptionData sub : subList) {
            SubscriptionData old = this.subscriptionTable.get(sub.getTopic());
            if (old == null) {
                SubscriptionData prev = this.subscriptionTable.putIfAbsent(sub.getTopic(), sub);
                if (null == prev) {
                    updated = true;
                    log.info("subscription changed, add new topic, group: {} {}",
                        this.groupName,
                        sub.toString());
                }
            } else if (sub.getSubVersion() > old.getSubVersion()) {
                if (this.consumeType == ConsumeType.CONSUME_PASSIVELY) {
                    log.info("subscription changed, group: {} OLD: {} NEW: {}",
                        this.groupName,
                        old.toString(),
                        sub.toString()
                    );
                }

                this.subscriptionTable.put(sub.getTopic(), sub);
            }
            // Add all new topics to the HashSet
            topicSet.add(sub.getTopic());
        }

        Iterator<Entry<String, SubscriptionData>> it = this.subscriptionTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, SubscriptionData> next = it.next();
            String oldTopic = next.getKey();
            // Check HashSet with O(1) time complexity
            if (!topicSet.contains(oldTopic)) {
                log.warn("subscription changed, group: {} remove topic {} {}",
                    this.groupName,
                    oldTopic,
                    next.getValue().toString()
                );

                it.remove();
                updated = true;
            }
        }

        this.lastUpdateTimestamp = System.currentTimeMillis();

        return updated;
    }

    /** 返回当前已订阅的全部 topic 名称。 */
    public Set<String> getSubscribeTopics() {
        return subscriptionTable.keySet();
    }

    /** 按 topic 查找订阅详情。 */
    public SubscriptionData findSubscriptionData(final String topic) {
        return this.subscriptionTable.get(topic);
    }

    /** 返回组级消费类型。 */
    public ConsumeType getConsumeType() {
        return consumeType;
    }

    /** 设置组级消费类型。 */
    public void setConsumeType(ConsumeType consumeType) {
        this.consumeType = consumeType;
    }

    /** 返回组级消息模式。 */
    public MessageModel getMessageModel() {
        return messageModel;
    }

    /** 设置组级消息模式。 */
    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    /** 返回消费者组名。 */
    public String getGroupName() {
        return groupName;
    }

    /** 返回组信息最近更新时间戳。 */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /** 设置组信息最近更新时间戳。 */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /** 返回首次消费起始位点策略。 */
    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    /** 设置首次消费起始位点策略。 */
    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }
}
