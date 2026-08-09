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

package org.apache.rocketmq.common.attribute;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.common.message.MessageConst;

/**
 * Topic 消息类型：用于区分普通、顺序、延迟、事务、优先级、Lite 等语义。
 */
public enum TopicMessageType {
    /** 未指定类型。 */
    UNSPECIFIED("UNSPECIFIED"),
    /** 普通消息。 */
    NORMAL("NORMAL"),
    /** 顺序（FIFO）消息。 */
    FIFO("FIFO"),
    /** 延迟/定时消息。 */
    DELAY("DELAY"),
    /** 事务消息。 */
    TRANSACTION("TRANSACTION"),
    /** 优先级消息。 */
    PRIORITY("PRIORITY"),
    /** Lite Topic 消息。 */
    LITE("LITE"),
    /** 混合类型 Topic。 */
    MIXED("MIXED");

    /** 属性/协议中使用的字符串值。 */
    private final String value;

    TopicMessageType(String value) {
        this.value = value;
    }

    /** 返回全部消息类型的字符串值集合。 */
    public static Set<String> topicMessageTypeSet() {
        return Sets.newHashSet(UNSPECIFIED.value, NORMAL.value, FIFO.value, DELAY.value, TRANSACTION.value,
            PRIORITY.value, LITE.value, MIXED.value);
    }

    /** 返回类型字符串值。 */
    public String getValue() {
        return value;
    }

    /**
     * 根据消息 UserProperty 推断消息类型；解析顺序保证各类型互斥。
     *
     * @param messageProperty 消息属性 Map
     * @return 推断出的 {@link TopicMessageType}
     */
    public static TopicMessageType parseFromMessageProperty(Map<String, String> messageProperty) {
        // 解析顺序保证各消息类型互斥
        if (Boolean.parseBoolean(messageProperty.get(MessageConst.PROPERTY_TRANSACTION_PREPARED))) {
            return TopicMessageType.TRANSACTION;
        } else if (messageProperty.get(MessageConst.PROPERTY_DELAY_TIME_LEVEL) != null
            || messageProperty.get(MessageConst.PROPERTY_TIMER_DELIVER_MS) != null
            || messageProperty.get(MessageConst.PROPERTY_TIMER_DELAY_SEC) != null
            || messageProperty.get(MessageConst.PROPERTY_TIMER_DELAY_MS) != null) {
            return TopicMessageType.DELAY;
        } else if (messageProperty.get(MessageConst.PROPERTY_SHARDING_KEY) != null) {
            return TopicMessageType.FIFO;
        } else if (messageProperty.get(MessageConst.PROPERTY_PRIORITY) != null) {
            return TopicMessageType.PRIORITY;
        } else if (messageProperty.get(MessageConst.PROPERTY_LITE_TOPIC) != null) {
            return TopicMessageType.LITE;
        }
        return TopicMessageType.NORMAL;
    }

    /** 返回用于指标上报的小写类型名。 */
    public String getMetricsValue() {
        return value.toLowerCase();
    }
}
