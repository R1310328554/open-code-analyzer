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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.common.attribute.Attribute;
import org.apache.rocketmq.common.attribute.EnumAttribute;
import org.apache.rocketmq.common.attribute.LongRangeAttribute;
import org.apache.rocketmq.common.attribute.TopicMessageType;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Topic 级可配置属性：队列类型、清理策略、消息类型、保留/过期时间等。
 * 定义注册在 {@link #ALL} 供创建与更新 Topic 时校验。
 */
public class TopicAttributes {
    /** 队列实现类型：BatchCQ 或 SimpleCQ。 */
    public static final EnumAttribute QUEUE_TYPE_ATTRIBUTE = new EnumAttribute(
        "queue.type",
        false,
        newHashSet("BatchCQ", "SimpleCQ"),
        "SimpleCQ"
    );
    /** 清理策略：DELETE（删除）或 COMPACTION（压缩）。 */
    public static final EnumAttribute CLEANUP_POLICY_ATTRIBUTE = new EnumAttribute(
        "cleanup.policy",
        false,
        newHashSet("DELETE", "COMPACTION"),
        "DELETE"
    );
    /** Topic 消息类型（NORMAL、FIFO、DELAY、TRANSACTION、LITE 等）。 */
    public static final EnumAttribute TOPIC_MESSAGE_TYPE_ATTRIBUTE = new EnumAttribute(
        "message.type",
        true,
        TopicMessageType.topicMessageTypeSet(),
        TopicMessageType.NORMAL.getValue()
    );
    /** Topic 消息保留时间（分钟，-1 表示使用 Broker 默认）。 */
    public static final LongRangeAttribute TOPIC_RESERVE_TIME_ATTRIBUTE = new LongRangeAttribute(
        "reserve.time",
        true,
        -1,
        Long.MAX_VALUE,
        -1
    );

    /** Lite Topic 过期时间（分钟，上限 30 天，-1 表示不限制）。 */
    public static final LongRangeAttribute LITE_EXPIRATION_ATTRIBUTE = new LongRangeAttribute(
        "lite.topic.expiration",
        true,
        -1,
        TimeUnit.DAYS.toMinutes(30),
        -1
    );

    /** 属性名 → {@link Attribute} 定义的全局注册表。 */
    public static final Map<String, Attribute> ALL;

    static {
        ALL = new HashMap<>();
        ALL.put(QUEUE_TYPE_ATTRIBUTE.getName(), QUEUE_TYPE_ATTRIBUTE);
        ALL.put(CLEANUP_POLICY_ATTRIBUTE.getName(), CLEANUP_POLICY_ATTRIBUTE);
        ALL.put(TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName(), TOPIC_MESSAGE_TYPE_ATTRIBUTE);
        ALL.put(TOPIC_RESERVE_TIME_ATTRIBUTE.getName(), TOPIC_RESERVE_TIME_ATTRIBUTE);
        ALL.put(LITE_EXPIRATION_ATTRIBUTE.getName(), LITE_EXPIRATION_ATTRIBUTE);
    }
}
