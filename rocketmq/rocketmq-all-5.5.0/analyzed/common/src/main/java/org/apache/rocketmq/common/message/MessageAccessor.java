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

package org.apache.rocketmq.common.message;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Message} 属性访问工具：读写系统属性、克隆消息及深拷贝 properties。
 * 供框架内部在不暴露 Message 包级方法时操作消息属性。
 */
public class MessageAccessor {

    /** 清除消息指定属性。 */
    public static void clearProperty(final Message msg, final String name) {
        msg.clearProperty(name);
    }

    /** 整体设置消息 properties Map。 */
    public static void setProperties(final Message msg, Map<String, String> properties) {
        msg.setProperties(properties);
    }

    /** 设置单元化迁移标志。 */
    public static void setTransferFlag(final Message msg, String unit) {
        putProperty(msg, MessageConst.PROPERTY_TRANSFER_FLAG, unit);
    }

    /** 写入任意系统/内部属性。 */
    public static void putProperty(final Message msg, final String name, final String value) {
        msg.putProperty(name, value);
    }

    /** 读取单元化迁移标志。 */
    public static String getTransferFlag(final Message msg) {
        return msg.getProperty(MessageConst.PROPERTY_TRANSFER_FLAG);
    }

    /** 设置消息纠错标志。 */
    public static void setCorrectionFlag(final Message msg, String unit) {
        putProperty(msg, MessageConst.PROPERTY_CORRECTION_FLAG, unit);
    }

    /** 读取消息纠错标志。 */
    public static String getCorrectionFlag(final Message msg) {
        return msg.getProperty(MessageConst.PROPERTY_CORRECTION_FLAG);
    }

    /** 设置原始消息 ID（重试/转发链路）。 */
    public static void setOriginMessageId(final Message msg, String originMessageId) {
        putProperty(msg, MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, originMessageId);
    }

    /** 读取原始消息 ID。 */
    public static String getOriginMessageId(final Message msg) {
        return msg.getProperty(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID);
    }

    /** 设置 MQ2 兼容标志。 */
    public static void setMQ2Flag(final Message msg, String flag) {
        putProperty(msg, MessageConst.PROPERTY_MQ2_FLAG, flag);
    }

    /** 读取 MQ2 兼容标志。 */
    public static String getMQ2Flag(final Message msg) {
        return msg.getProperty(MessageConst.PROPERTY_MQ2_FLAG);
    }

    /** 设置当前重试消费次数。 */
    public static void setReconsumeTime(final Message msg, String reconsumeTimes) {
        putProperty(msg, MessageConst.PROPERTY_RECONSUME_TIME, reconsumeTimes);
    }

    /** 读取当前重试消费次数。 */
    public static String getReconsumeTime(final Message msg) {
        return msg.getProperty(MessageConst.PROPERTY_RECONSUME_TIME);
    }

    /** 设置最大重试消费次数。 */
    public static void setMaxReconsumeTimes(final Message msg, String maxReconsumeTimes) {
        putProperty(msg, MessageConst.PROPERTY_MAX_RECONSUME_TIMES, maxReconsumeTimes);
    }

    /** 读取最大重试消费次数。 */
    public static String getMaxReconsumeTimes(final Message msg) {
        return msg.getProperty(MessageConst.PROPERTY_MAX_RECONSUME_TIMES);
    }

    /** 设置消费开始时间戳属性。 */
    public static void setConsumeStartTimeStamp(final Message msg, String propertyConsumeStartTimeStamp) {
        putProperty(msg, MessageConst.PROPERTY_CONSUME_START_TIMESTAMP, propertyConsumeStartTimeStamp);
    }

    /** 读取消费开始时间戳属性。 */
    public static String getConsumeStartTimeStamp(final Message msg) {
        return msg.getProperty(MessageConst.PROPERTY_CONSUME_START_TIMESTAMP);
    }

    /** 设置消息关联的 Lite Topic 名。 */
    public static void setLiteTopic(final Message msg, String liteTopic) {
        MessageAccessor.putProperty(msg, MessageConst.PROPERTY_LITE_TOPIC, liteTopic);
    }

    /** 浅克隆消息：复制 Topic、body、flag 与 properties。 */
    public static Message cloneMessage(final Message msg) {
        Message newMsg = new Message(msg.getTopic(), msg.getBody());
        newMsg.setFlag(msg.getFlag());
        newMsg.setProperties(msg.getProperties());
        return newMsg;
    }

    /** 深拷贝 properties Map；入参为 null 时返回 null。 */
    public static Map<String, String> deepCopyProperties(Map<String, String> properties) {
        if (properties == null) {
            return null;
        }
        return new HashMap<>(properties);
    }
}
