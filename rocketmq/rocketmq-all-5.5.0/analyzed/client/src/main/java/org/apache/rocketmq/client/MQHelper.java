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
package org.apache.rocketmq.client;

import java.util.Set;
import java.util.TreeSet;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * MQ 运维辅助工具：提供按时间戳重置消费位点等便捷方法。
 */
public class MQHelper {
    private static final Logger log = LoggerFactory.getLogger(MQHelper.class);

    /** @deprecated 请使用带 instanceName 的重载方法。 */
    @Deprecated
    public static void resetOffsetByTimestamp(
        final MessageModel messageModel,
        final String consumerGroup,
        final String topic,
        final long timestamp) throws Exception {
        resetOffsetByTimestamp(messageModel, "DEFAULT", consumerGroup, topic, timestamp);
    }

    /**
     * 按时间戳重置指定消费组在各队列上的消费位点。
     *
     * @param messageModel 集群/广播消费模式
     * @param instanceName 客户端实例名
     * @param consumerGroup 消费组
     * @param topic Topic
     * @param timestamp 目标时间戳（毫秒）
     */
    public static void resetOffsetByTimestamp(
        final MessageModel messageModel,
        final String instanceName,
        final String consumerGroup,
        final String topic,
        final long timestamp) throws Exception {

        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(consumerGroup);
        consumer.setInstanceName(instanceName);
        consumer.setMessageModel(messageModel);
        consumer.start();

        Set<MessageQueue> mqs = null;
        try {
            mqs = consumer.fetchSubscribeMessageQueues(topic);
            if (mqs != null && !mqs.isEmpty()) {
                TreeSet<MessageQueue> mqsNew = new TreeSet<>(mqs);
                for (MessageQueue mq : mqsNew) {
                    long offset = consumer.searchOffset(mq, timestamp);
                    if (offset >= 0) {
                        consumer.updateConsumeOffset(mq, offset);
                        log.info("resetOffsetByTimestamp updateConsumeOffset success, {} {} {}",
                            consumerGroup, offset, mq);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("resetOffsetByTimestamp Exception", e);
            throw e;
        } finally {
            if (mqs != null) {
                consumer.getDefaultMQPullConsumerImpl().getOffsetStore().persistAll(mqs);
            }
            consumer.shutdown();
        }
    }
}
