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
package org.apache.rocketmq.common;

import org.apache.rocketmq.common.topic.TopicValidator;

/**
 * Pop 消费 Ack/Checkpoint 相关常量：Ack 间隔、锁时长、Revive 主题/组名及 CK/ACK 消息 Tag。
 * 用于 Pop 模式下的可见性超时、重试与死信复活链路。
 */
public class PopAckConstants {
    /** Ack 时间间隔（毫秒），默认 1 秒。 */
    public static long ackTimeInterval = 1000;
    /** 一秒对应的毫秒数。 */
    public static final long SECOND = 1000;

    /** Pop 消息锁定时长（毫秒），默认 5 秒。 */
    public static long lockTime = 5000;
    /** Pop 重试队列数量。 */
    public static int retryQueueNum = 1;

    /** Revive 消费组名（系统前缀 + REVIVE_GROUP）。 */
    public static final String REVIVE_GROUP = MixAll.CID_RMQ_SYS_PREFIX + "REVIVE_GROUP";
    /** 本地主机占位地址。 */
    public static final String LOCAL_HOST = "127.0.0.1";
    /** Revive 日志 Topic 前缀（系统 Topic 前缀 + REVIVE_LOG_）。 */
    public static final String REVIVE_TOPIC = TopicValidator.SYSTEM_TOPIC_PREFIX + "REVIVE_LOG_";
    /** Checkpoint 消息 Tag。 */
    public static final String CK_TAG = "ck";
    /** 单条 Ack 消息 Tag。 */
    public static final String ACK_TAG = "ack";
    /** 批量 Ack 消息 Tag。 */
    public static final String BATCH_ACK_TAG = "bAck";
    /** Revive/Ack 载荷字段分隔符。 */
    public static final String SPLIT = "@";

    /**
     * 构造集群级 Revive Topic 名称。
     *
     * @param clusterName 集群名
     * @return Revive Topic 全名
     */
    public static String buildClusterReviveTopic(String clusterName) {
        return PopAckConstants.REVIVE_TOPIC + clusterName;
    }

    /** 判断 Topic 是否以 Revive 前缀开头。 */
    public static boolean isStartWithRevivePrefix(String topicName) {
        return topicName != null && topicName.startsWith(REVIVE_TOPIC);
    }
}
