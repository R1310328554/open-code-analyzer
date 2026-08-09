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

import static com.google.common.collect.Sets.newHashSet;

import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.common.attribute.Attribute;
import org.apache.rocketmq.common.attribute.BooleanAttribute;
import org.apache.rocketmq.common.attribute.EnumAttribute;
import org.apache.rocketmq.common.attribute.LongRangeAttribute;
import org.apache.rocketmq.common.attribute.StringAttribute;
import org.apache.rocketmq.common.attribute.LiteSubModel;

/**
 * 消费组可配置属性定义：优先级因子、Lite 订阅模型/配额/通配符等。
 * 所有属性注册在 {@link #ALL} 供 Broker 校验与默认值查询。
 */
public class SubscriptionGroupAttributes {

    /** 属性名 → {@link Attribute} 定义的全局注册表。 */
    public static final Map<String, Attribute> ALL;
    /** 优先级因子：0 关闭优先级模式，1–100 启用并调节权重。 */
    public static final LongRangeAttribute PRIORITY_FACTOR_ATTRIBUTE = new LongRangeAttribute(
        "priority.factor",
        true,
        0, // 0 表示关闭优先级模式
        100, // 100 表示完全启用优先级模式
        100
    );

    /** Lite 订阅绑定的 Topic 名称。 */
    public static final StringAttribute LITE_BIND_TOPIC_ATTRIBUTE = new StringAttribute(
        "lite.bind.topic",
        true
    );

    /** Lite 订阅模型：Shared（共享）或 Exclusive（独占）。 */
    public static final EnumAttribute LITE_SUB_MODEL_ATTRIBUTE = new EnumAttribute(
        "lite.sub.model",
        true,
        newHashSet(LiteSubModel.Shared.name(), LiteSubModel.Exclusive.name()),
        LiteSubModel.Shared.name()
    );

    /** 独占 Lite 订阅是否在 reset offset 时生效。 */
    public static final BooleanAttribute LITE_SUB_RESET_OFFSET_EXCLUSIVE_ATTRIBUTE = new BooleanAttribute(
        "lite.sub.reset.offset.exclusive",
        true,
        false
    );

    /** 取消订阅时是否 reset Lite 订阅 offset。 */
    public static final BooleanAttribute LITE_SUB_RESET_OFFSET_UNSUBSCRIBE_ATTRIBUTE = new BooleanAttribute(
        "lite.sub.reset.offset.unsubscribe",
        true,
        false
    );

    /** 客户端 Lite 订阅配额上限（-1 表示不限制）。 */
    public static final LongRangeAttribute LITE_SUB_CLIENT_QUOTA_ATTRIBUTE = new LongRangeAttribute(
        "lite.sub.client.quota",
        true,
        -1,
        Long.MAX_VALUE,
        2000
    );

    /** 客户端 Lite 订阅最大事件缓存条数。 */
    public static final LongRangeAttribute LITE_SUB_CLIENT_MAX_EVENT_COUNT_ATTRIBUTE = new LongRangeAttribute(
        "lite.sub.client.max.event.cnt",
        true,
        10,
        Long.MAX_VALUE,
        400
    );

    /** Lite 订阅通配符表达式。 */
    public static final StringAttribute LITE_SUB_WILDCARD_ATTRIBUTE = new StringAttribute(
        "lite.sub.wildcard",
        true
    );

    static {
        ALL = new HashMap<>();
        ALL.put(PRIORITY_FACTOR_ATTRIBUTE.getName(), PRIORITY_FACTOR_ATTRIBUTE);
        ALL.put(LITE_BIND_TOPIC_ATTRIBUTE.getName(), LITE_BIND_TOPIC_ATTRIBUTE);
        ALL.put(LITE_SUB_CLIENT_QUOTA_ATTRIBUTE.getName(), LITE_SUB_CLIENT_QUOTA_ATTRIBUTE);
        ALL.put(LITE_SUB_MODEL_ATTRIBUTE.getName(), LITE_SUB_MODEL_ATTRIBUTE);
        ALL.put(LITE_SUB_RESET_OFFSET_EXCLUSIVE_ATTRIBUTE.getName(), LITE_SUB_RESET_OFFSET_EXCLUSIVE_ATTRIBUTE);
        ALL.put(LITE_SUB_RESET_OFFSET_UNSUBSCRIBE_ATTRIBUTE.getName(), LITE_SUB_RESET_OFFSET_UNSUBSCRIBE_ATTRIBUTE);
        ALL.put(LITE_SUB_CLIENT_MAX_EVENT_COUNT_ATTRIBUTE.getName(), LITE_SUB_CLIENT_MAX_EVENT_COUNT_ATTRIBUTE);
        ALL.put(LITE_SUB_WILDCARD_ATTRIBUTE.getName(), LITE_SUB_WILDCARD_ATTRIBUTE);
    }
}