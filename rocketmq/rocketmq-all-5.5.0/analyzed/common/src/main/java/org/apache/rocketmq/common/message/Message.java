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

import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * RocketMQ 消息体：Topic、标签、键、用户属性、消息体字节及事务 ID 等。
 * 系统属性通过 {@link MessageConst} 键写入 properties Map。
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 8445773977080406428L;

    /** 消息所属 Topic。 */
    private String topic;
    /** 消息标志位（系统/业务自定义）。 */
    private int flag;
    /** 系统属性与用户属性 Map。 */
    private Map<String, String> properties;
    /** 消息体字节数组。 */
    private byte[] body;
    /** 事务消息 ID。 */
    private String transactionId;

    public Message() {
    }

    public Message(String topic, byte[] body) {
        this(topic, "", "", 0, body, true);
    }

    /**
     * 完整构造：设置 Topic、标签、键、标志、消息体及是否等待存储确认。
     */
    public Message(String topic, String tags, String keys, int flag, byte[] body, boolean waitStoreMsgOK) {
        this.topic = topic;
        this.flag = flag;
        this.body = body;

        if (tags != null && tags.length() > 0) {
            this.setTags(tags);
        }

        if (keys != null && keys.length() > 0) {
            this.setKeys(keys);
        }

        this.setWaitStoreMsgOK(waitStoreMsgOK);
    }

    public Message(String topic, String tags, byte[] body) {
        this(topic, tags, "", 0, body, true);
    }

    public Message(String topic, String tags, String keys, byte[] body) {
        this(topic, tags, keys, 0, body, true);
    }

    public void setKeys(String keys) {
        this.putProperty(MessageConst.PROPERTY_KEYS, keys);
    }

    /** 写入系统/内部属性（懒初始化 properties）。 */
    void putProperty(final String name, final String value) {
        if (null == this.properties) {
            this.properties = new HashMap<>();
        }

        this.properties.put(name, value);
    }

    /** 移除指定属性键。 */
    void clearProperty(final String name) {
        if (null != this.properties) {
            this.properties.remove(name);
        }
    }

    /**
     * 设置用户自定义属性；名称不得与 {@link MessageConst} 系统键冲突。
     *
     * @throws RuntimeException 属性名被系统占用
     * @throws IllegalArgumentException 名或值为 null/空白
     */
        if (MessageConst.STRING_HASH_SET.contains(name)) {
            throw new RuntimeException(String.format(
                "The Property<%s> is used by system, input another please", name));
        }

        if (value == null || value.trim().isEmpty()
            || name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "The name or value of property can not be null or blank string!"
            );
        }

        this.putProperty(name, value);
    }

    /** 获取用户属性，等价于 {@link #getProperty(String)}。 */
    public String getUserProperty(final String name) {
        return this.getProperty(name);
    }

    public String getProperty(final String name) {
        if (null == this.properties) {
            this.properties = new HashMap<>();
        }

        return this.properties.get(name);
    }

    public boolean hasProperty(final String name) {
        if (null == this.properties) {
            return false;
        }
        return this.properties.containsKey(name);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTags() {
        return this.getProperty(MessageConst.PROPERTY_TAGS);
    }

    public void setTags(String tags) {
        this.putProperty(MessageConst.PROPERTY_TAGS, tags);
    }

    public String getKeys() {
        return this.getProperty(MessageConst.PROPERTY_KEYS);
    }

    public void setKeys(Collection<String> keyCollection) {
        String keys = String.join(MessageConst.KEY_SEPARATOR, keyCollection);

        this.setKeys(keys);
    }

    /** 返回延迟级别（未设置时为 0）。 */
    public int getDelayTimeLevel() {
        String t = this.getProperty(MessageConst.PROPERTY_DELAY_TIME_LEVEL);
        if (t != null) {
            return Integer.parseInt(t);
        }

        return 0;
    }

    /** 设置延迟消息级别。 */
    public void setDelayTimeLevel(int level) {
        this.putProperty(MessageConst.PROPERTY_DELAY_TIME_LEVEL, String.valueOf(level));
    }

    /** 设置消息优先级，须 >= 0。 */
    public void setPriority(int priority) {
        if (priority < 0) {
            throw new IllegalArgumentException("The priority must be greater than or equal to 0");
        }
        this.putProperty(MessageConst.PROPERTY_PRIORITY, String.valueOf(priority));
    }

    public int getPriority() {
        return NumberUtils.toInt(this.getProperty(MessageConst.PROPERTY_PRIORITY), -1);
    }

    /** 发送时是否等待 Broker 存储确认，默认 true。 */
    public boolean isWaitStoreMsgOK() {
        String result = this.getProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK);
        if (null == result) {
            return true;
        }

        return Boolean.parseBoolean(result);
    }

    public void setWaitStoreMsgOK(boolean waitStoreMsgOK) {
        this.putProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK, Boolean.toString(waitStoreMsgOK));
    }

    /** 设置实例 ID（多实例隔离）。 */
    public void setInstanceId(String instanceId) {
        this.putProperty(MessageConst.PROPERTY_INSTANCE_ID, instanceId);
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    /** 包内可见：整体替换 properties Map。 */
    void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getBuyerId() {
        return getProperty(MessageConst.PROPERTY_BUYER_ID);
    }

    public void setBuyerId(String buyerId) {
        putProperty(MessageConst.PROPERTY_BUYER_ID, buyerId);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "Message{" +
            "topic='" + topic + '\'' +
            ", flag=" + flag +
            ", properties=" + properties +
            ", body=" + Arrays.toString(body) +
            ", transactionId='" + transactionId + '\'' +
            '}';
    }

    /** 设置定时消息延迟秒数。 */
    public void setDelayTimeSec(long sec) {
        this.putProperty(MessageConst.PROPERTY_TIMER_DELAY_SEC, String.valueOf(sec));
    }

    /** 获取定时消息延迟秒数，未设置时为 0。 */
    public long getDelayTimeSec() {
        String t = this.getProperty(MessageConst.PROPERTY_TIMER_DELAY_SEC);
        if (t != null) {
            return Long.parseLong(t);
        }
        return 0;
    }

    /** 设置定时消息延迟毫秒数。 */
    public void setDelayTimeMs(long timeMs) {
        this.putProperty(MessageConst.PROPERTY_TIMER_DELAY_MS, String.valueOf(timeMs));
    }

    /** 获取定时消息延迟毫秒数，未设置时为 0。 */
    public long getDelayTimeMs() {
        String t = this.getProperty(MessageConst.PROPERTY_TIMER_DELAY_MS);
        if (t != null) {
            return Long.parseLong(t);
        }
        return 0;
    }

    /** 设置绝对投递时间戳（毫秒）。 */
    public void setDeliverTimeMs(long timeMs) {
        this.putProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS, String.valueOf(timeMs));
    }

    /** 获取绝对投递时间戳，未设置时为 0。 */
    public long getDeliverTimeMs() {
        String t = this.getProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS);
        if (t != null) {
            return Long.parseLong(t);
        }
        return 0;
    }
}
