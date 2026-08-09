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
package org.apache.rocketmq.remoting.protocol.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.filter.ExpressionType;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

import java.util.Arrays;

/**
 * 订阅过滤表达式构建工具：将 Topic 与订阅串解析为 {@link SubscriptionData}。
 * 支持 Tag（|| 分隔）与 SQL92 等表达式类型。
 */
public class FilterAPI {

    /** 按 Topic 与 Tag 订阅串构建订阅数据（默认 Tag 类型）。 */
    public static SubscriptionData buildSubscriptionData(String topic, String subString) throws Exception {
        final SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setTopic(topic);
        subscriptionData.setSubString(subString);

        // 空串或 SUB_ALL 表示订阅全部 Tag
        if (StringUtils.isEmpty(subString) || subString.equals(SubscriptionData.SUB_ALL)) {
            subscriptionData.setSubString(SubscriptionData.SUB_ALL);
            return subscriptionData;
        }
        // 以 || 分割多 Tag
        String[] tags = subString.split("\\|\\|");
        if (tags.length > 0) {
            Arrays.stream(tags).map(String::trim).filter(tag -> !tag.isEmpty()).forEach(tag -> {
                subscriptionData.getTagsSet().add(tag);
                subscriptionData.getCodeSet().add(tag.hashCode());
            });
        } else {
            throw new Exception("订阅串分割失败");
        }

        return subscriptionData;
    }

    /** 构建订阅数据并指定表达式类型（Tag/SQL92 等）。 */
    public static SubscriptionData buildSubscriptionData(String topic, String subString, String expressionType) throws Exception {
        final SubscriptionData subscriptionData = buildSubscriptionData(topic, subString);
        if (StringUtils.isNotBlank(expressionType)) {
            subscriptionData.setExpressionType(expressionType);
        }
        return subscriptionData;
    }

    /** 统一入口：按 type 选择 Tag 或通用表达式构建逻辑。 */
    public static SubscriptionData build(final String topic, final String subString,
        final String type) throws Exception {
        if (ExpressionType.TAG.equals(type) || type == null) {
            return buildSubscriptionData(topic, subString);
        }

        if (StringUtils.isEmpty(subString)) {
            throw new IllegalArgumentException("非 Tag 类型时表达式不可为空: " + type);
        }

        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setTopic(topic);
        subscriptionData.setSubString(subString);
        subscriptionData.setExpressionType(type);

        return subscriptionData;
    }
}
