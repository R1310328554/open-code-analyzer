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

/**
 * POP 消费相关 Topic/Key 构建与解析工具。
 */
public class KeyBuilder {
    /** POP 顺序消息复活队列 ID。 */
    public static final int POP_ORDER_REVIVE_QUEUE = 999;
    /** V1 重试 Topic 分隔符。 */
    private static final char POP_RETRY_SEPARATOR_V1 = '_';
    /** V2 重试 Topic 分隔符。 */
    private static final char POP_RETRY_SEPARATOR_V2 = '+';
    /** V2 分隔符的正则转义形式。 */
    private static final String POP_RETRY_REGEX_SEPARATOR_V2 = "\\+";

    /** 按版本开关构建 POP 重试 Topic。 */
    public static String buildPopRetryTopic(String topic, String cid, boolean enableRetryV2) {
        if (enableRetryV2) {
            return buildPopRetryTopicV2(topic, cid);
        }
        return buildPopRetryTopicV1(topic, cid);
    }

    /** 使用 V1 规则构建 POP 重试 Topic。 */
    public static String buildPopRetryTopic(String topic, String cid) {
        return MixAll.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V1 + topic;
    }

    /** 使用 + 分隔符构建 V2 重试 Topic。 */
    public static String buildPopRetryTopicV2(String topic, String cid) {
        return MixAll.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V2 + topic;
    }

    /** 使用 _ 分隔符构建 V1 重试 Topic。 */
    public static String buildPopRetryTopicV1(String topic, String cid) {
        return MixAll.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V1 + topic;
    }

    /** 从重试 Topic 解析原始 Topic（需消费组 cid）。 */
    public static String parseNormalTopic(String topic, String cid) {
        if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
            if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V2)) {
                return topic.substring((MixAll.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V2).length());
            }
            return topic.substring((MixAll.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V1).length());
        } else {
            return topic;
        }
    }

    /** 从 V2 重试 Topic 解析原始 Topic。 */
    public static String parseNormalTopic(String retryTopic) {
        if (isPopRetryTopicV2(retryTopic)) {
            String[] result = retryTopic.split(POP_RETRY_REGEX_SEPARATOR_V2);
            if (result.length == 2) {
                return result[1];
            }
        }
        return retryTopic;
    }

    /** 从重试 Topic 解析消费组名。 */
    public static String parseGroup(String retryTopic) {
        if (isPopRetryTopicV2(retryTopic)) {
            String[] result = retryTopic.split(POP_RETRY_REGEX_SEPARATOR_V2);
            if (result.length == 2) {
                return result[0].substring(MixAll.RETRY_GROUP_TOPIC_PREFIX.length());
            }
        }
        return retryTopic.substring(MixAll.RETRY_GROUP_TOPIC_PREFIX.length());
    }

    /** 构建 POP 轮询键：topic@cid@queueId。 */
    public static String buildPollingKey(String topic, String cid, int queueId) {
        return topic + PopAckConstants.SPLIT + cid + PopAckConstants.SPLIT + queueId;
    }

    /** 判断是否为 V2 格式 POP 重试 Topic。 */
    public static boolean isPopRetryTopicV2(String retryTopic) {
        return retryTopic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX) && retryTopic.contains(String.valueOf(POP_RETRY_SEPARATOR_V2));
    }

    /** 构建 POP Lite 锁键。 */
    public static String buildPopLiteLockKey(String group, String lmqName) {
        return group + PopAckConstants.SPLIT + lmqName;
    }
}
