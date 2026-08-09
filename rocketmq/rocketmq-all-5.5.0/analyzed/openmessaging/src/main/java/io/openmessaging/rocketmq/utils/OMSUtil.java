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
package io.openmessaging.rocketmq.utils;

import io.openmessaging.BytesMessage;
import io.openmessaging.KeyValue;
import io.openmessaging.Message.BuiltinKeys;
import io.openmessaging.OMS;
import io.openmessaging.producer.SendResult;
import io.openmessaging.rocketmq.domain.BytesMessageImpl;
import io.openmessaging.rocketmq.domain.RocketMQConstants;
import io.openmessaging.rocketmq.domain.SendResultImpl;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageAccessor;

/**
 * OMS 与 RocketMQ 消息/结果转换及通用工具方法。
 */
public class OMSUtil {

    /**
     * 构建 OMS 客户端实例名（PID + 纳秒时间戳）。
     *
     * @return 唯一实例名
     */
    public static String buildInstanceName() {
        return Integer.toString(UtilAll.getPid()) + "%OpenMessaging" + "%" + System.nanoTime();
    }

    /** 将 OMS {@link BytesMessage} 转为 RocketMQ {@link org.apache.rocketmq.common.message.Message}。 */
    public static org.apache.rocketmq.common.message.Message msgConvert(BytesMessage omsMessage) {
        org.apache.rocketmq.common.message.Message rmqMessage = new org.apache.rocketmq.common.message.Message();
        rmqMessage.setBody(omsMessage.getBody(byte[].class));

        KeyValue sysHeaders = omsMessage.sysHeaders();
        KeyValue userHeaders = omsMessage.userHeaders();

        // RocketMQ 中目标统一映射为 Topic
        rmqMessage.setTopic(sysHeaders.getString(BuiltinKeys.DESTINATION));

        if (sysHeaders.containsKey(BuiltinKeys.START_TIME)) {
            long deliverTime = sysHeaders.getLong(BuiltinKeys.START_TIME, 0);
            if (deliverTime > 0) {
                rmqMessage.putUserProperty(RocketMQConstants.START_DELIVER_TIME, String.valueOf(deliverTime));
            }
        }

        for (String key : userHeaders.keySet()) {
            MessageAccessor.putProperty(rmqMessage, key, userHeaders.getString(key));
        }

        // 系统头优先级高于用户头
        for (String key : sysHeaders.keySet()) {
            MessageAccessor.putProperty(rmqMessage, key, sysHeaders.getString(key));
        }

        return rmqMessage;
    }

    /** 将 RocketMQ 消息扩展体转为 OMS {@link BytesMessage}。 */
    public static BytesMessage msgConvert(org.apache.rocketmq.common.message.MessageExt rmqMsg) {
        BytesMessage omsMsg = new BytesMessageImpl();
        omsMsg.setBody(rmqMsg.getBody());

        KeyValue headers = omsMsg.sysHeaders();
        KeyValue properties = omsMsg.userHeaders();

        final Set<Map.Entry<String, String>> entries = rmqMsg.getProperties().entrySet();

        for (final Map.Entry<String, String> entry : entries) {
            if (isOMSHeader(entry.getKey())) {
                headers.put(entry.getKey(), entry.getValue());
            } else {
                properties.put(entry.getKey(), entry.getValue());
            }
        }

        omsMsg.putSysHeaders(BuiltinKeys.MESSAGE_ID, rmqMsg.getMsgId());

        omsMsg.putSysHeaders(BuiltinKeys.DESTINATION, rmqMsg.getTopic());

        omsMsg.putSysHeaders(BuiltinKeys.SEARCH_KEYS, rmqMsg.getKeys());
        omsMsg.putSysHeaders(BuiltinKeys.BORN_HOST, String.valueOf(rmqMsg.getBornHost()));
        omsMsg.putSysHeaders(BuiltinKeys.BORN_TIMESTAMP, rmqMsg.getBornTimestamp());
        omsMsg.putSysHeaders(BuiltinKeys.STORE_HOST, String.valueOf(rmqMsg.getStoreHost()));
        omsMsg.putSysHeaders(BuiltinKeys.STORE_TIMESTAMP, rmqMsg.getStoreTimestamp());
        return omsMsg;
    }

    /** 判断属性键是否为 OMS 内置系统头字段。 */
    public static boolean isOMSHeader(String value) {
        for (Field field : BuiltinKeys.class.getDeclaredFields()) {
            try {
                if (field.get(BuiltinKeys.class).equals(value)) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        return false;
    }

    /** 将 RocketMQ SEND_OK 发送结果转为 OMS {@link SendResult}。 */
    public static SendResult sendResultConvert(org.apache.rocketmq.client.producer.SendResult rmqResult) {
        assert rmqResult.getSendStatus().equals(SendStatus.SEND_OK);
        return new SendResultImpl(rmqResult.getMsgId(), OMS.newKeyValue());
    }

    /** 合并多个 {@link KeyValue} 为单一键值对象。 */
    public static KeyValue buildKeyValue(KeyValue... keyValues) {
        KeyValue keyValue = OMS.newKeyValue();
        for (KeyValue properties : keyValues) {
            for (String key : properties.keySet()) {
                keyValue.put(key, properties.getString(key));
            }
        }
        return keyValue;
    }

    /** 返回对 {@code Iterable} 元素无限循环的迭代器。 */
    public static <T> Iterator<T> cycle(final Iterable<T> iterable) {
        return new Iterator<T>() {
            Iterator<T> iterator = new Iterator<T>() {
                @Override
                public synchronized boolean hasNext() {
                    return false;
                }

                @Override
                public synchronized T next() {
                    throw new NoSuchElementException();
                }

                @Override
                public synchronized void remove() {
                    // 占位迭代器不支持 remove
                }
            };

            @Override
            public synchronized boolean hasNext() {
                return iterator.hasNext() || iterable.iterator().hasNext();
            }

            @Override
            public synchronized T next() {
                if (!iterator.hasNext()) {
                    iterator = iterable.iterator();
                    if (!iterator.hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                return iterator.next();
            }

            @Override
            public synchronized void remove() {
                iterator.remove();
            }
        };
    }
}
