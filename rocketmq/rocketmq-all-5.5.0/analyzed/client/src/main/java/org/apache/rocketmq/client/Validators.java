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

import java.io.File;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.remoting.protocol.ResponseCode;

import static org.apache.rocketmq.common.topic.TopicValidator.isTopicOrGroupIllegal;

/**
 * 客户端通用校验器：校验 Group、Topic、Message 与 Broker/Topic 权限配置。
 */
public class Validators {
    /** 通用字符串最大长度。 */
    public static final int CHARACTER_MAX_LENGTH = 255;
    /** Topic 名称最大长度。 */
    public static final int TOPIC_MAX_LENGTH = 127;
    /*
     * 消费组名最长 120：需预留 %RETRY%group_topic 等重试 Topic 拼接空间。
     */
    /** 消费组名称最大长度。 */
    public static final int GROUP_MAX_LENGTH = 120;

    /** 校验消费组名非空、长度与字符合法性。 */
    public static void checkGroup(String group) throws MQClientException {
        if (UtilAll.isBlank(group)) {
            throw new MQClientException("the specified group is blank", null);
        }

        if (group.length() > GROUP_MAX_LENGTH) {
            throw new MQClientException(String.format("the specified group[%s] is longer than group max length: %s.", group, GROUP_MAX_LENGTH), null);
        }

        if (isTopicOrGroupIllegal(group)) {
            throw new MQClientException(String.format(
                    "the specified group[%s] contains illegal characters, allowing only %s", group,
                    "^[%|a-zA-Z0-9_-]+$"), null);
        }
    }

    /** 校验消息 Topic、Body 非空且不超过 Producer 最大消息大小。 */
    public static void checkMessage(Message msg, DefaultMQProducer defaultMQProducer) throws MQClientException {
        if (null == msg) {
            throw new MQClientException(ResponseCode.MESSAGE_ILLEGAL, "the message is null");
        }
        // 校验 Topic
        Validators.checkTopic(msg.getTopic());
        Validators.isNotAllowedSendTopic(msg.getTopic());

        // 校验消息体
        if (null == msg.getBody()) {
            throw new MQClientException(ResponseCode.MESSAGE_ILLEGAL, "the message body is null");
        }

        if (0 == msg.getBody().length) {
            throw new MQClientException(ResponseCode.MESSAGE_ILLEGAL, "the message body length is zero");
        }

        if (msg.getBody().length > defaultMQProducer.getMaxMessageSize()) {
            throw new MQClientException(ResponseCode.MESSAGE_ILLEGAL,
                "the message body size over max value, MAX: " + defaultMQProducer.getMaxMessageSize());
        }

        String lmqPath = msg.getUserProperty(MessageConst.PROPERTY_INNER_MULTI_DISPATCH);
        if (StringUtils.contains(lmqPath, File.separator)) {
            throw new MQClientException(ResponseCode.MESSAGE_ILLEGAL,
                "INNER_MULTI_DISPATCH " + lmqPath + " can not contains " + File.separator + " character");
        }
    }

    /** 校验 Topic 非空、长度与字符合法性。 */
    public static void checkTopic(String topic) throws MQClientException {
        if (UtilAll.isBlank(topic)) {
            throw new MQClientException("The specified topic is blank", null);
        }

        if (topic.length() > TOPIC_MAX_LENGTH) {
            throw new MQClientException(
                String.format("The specified topic is longer than topic max length %d.", TOPIC_MAX_LENGTH), null);
        }

        if (isTopicOrGroupIllegal(topic)) {
            throw new MQClientException(String.format(
                    "The specified topic[%s] contains illegal characters, allowing only %s", topic,
                    "^[%|a-zA-Z0-9_-]+$"), null);
        }
    }

    /** 禁止用户使用系统保留 Topic。 */
    public static void isSystemTopic(String topic) throws MQClientException {
        if (TopicValidator.isSystemTopic(topic)) {
            throw new MQClientException(
                    String.format("The topic[%s] is conflict with system topic.", topic), null);
        }
    }

    /** 禁止向不允许发送的 Topic 投递消息。 */
    public static void isNotAllowedSendTopic(String topic) throws MQClientException {
        if (TopicValidator.isNotAllowedSendTopic(topic)) {
            throw new MQClientException(
                    String.format("Sending message to topic[%s] is forbidden.", topic), null);
        }
    }

    /** 校验 Topic 权限位是否合法。 */
    public static void checkTopicConfig(final TopicConfig topicConfig) throws MQClientException {
        if (!PermName.isValid(topicConfig.getPerm())) {
            throw new MQClientException(ResponseCode.NO_PERMISSION,
                String.format("topicPermission value: %s is invalid.", topicConfig.getPerm()));
        }
    }

    /** 校验 Broker 配置中的 brokerPermission 是否合法。 */
    public static void checkBrokerConfig(final Properties brokerConfig) throws MQClientException {
        String brokerPermission = brokerConfig.getProperty("brokerPermission");
        if (brokerPermission != null && !PermName.isValid(brokerPermission)) {
            throw new MQClientException(ResponseCode.NO_PERMISSION,
                    String.format("brokerPermission value: %s is invalid.", brokerPermission));
        }
    }
}
