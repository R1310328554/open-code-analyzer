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

package org.apache.rocketmq.broker.filter;

import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.rocketmq.common.KeyBuilder;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.filter.ExpressionType;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 重试 Topic 专用表达式过滤器：先解码消息属性以获取原始 Topic，
 * 再使用对应消费者组的 {@link ConsumerFilterData} 求值。
 */
public class ExpressionForRetryMessageFilter extends ExpressionMessageFilter {
    /** 构造重试消息过滤器，继承 {@link ExpressionMessageFilter} 行为。 */
    public ExpressionForRetryMessageFilter(SubscriptionData subscriptionData, ConsumerFilterData consumerFilterData,
        ConsumerFilterManager consumerFilterManager) {
        super(subscriptionData, consumerFilterData, consumerFilterManager);
    }

    /** 对重试 Topic 解析真实 Topic 与消费组后再执行表达式匹配。 */
    @Override
    public boolean isMatchedByCommitLog(ByteBuffer msgBuffer, Map<String, String> properties) {
        if (subscriptionData == null) {
            return true;
        }

        if (subscriptionData.isClassFilterMode()) {
            return true;
        }

        if (ExpressionType.isTagType(subscriptionData.getExpressionType())) {
            return true;
        }

        boolean isRetryTopic = subscriptionData.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX);

        ConsumerFilterData realFilterData = this.consumerFilterData;
        Map<String, String> tempProperties = properties;
        boolean decoded = false;
        if (isRetryTopic) {
            // retry topic, use original filter data.
            // poor performance to support retry filter.
            if (tempProperties == null && msgBuffer != null) {
                decoded = true;
                tempProperties = MessageDecoder.decodeProperties(msgBuffer);
            }
            String realTopic = tempProperties.get(MessageConst.PROPERTY_RETRY_TOPIC);
            String group = KeyBuilder.parseGroup(subscriptionData.getTopic());
            realFilterData = this.consumerFilterManager.get(realTopic, group);
        }

        // no expression
        if (realFilterData == null || realFilterData.getExpression() == null
            || realFilterData.getCompiledExpression() == null) {
            return true;
        }

        if (!decoded && tempProperties == null && msgBuffer != null) {
            tempProperties = MessageDecoder.decodeProperties(msgBuffer);
        }

        Object ret = null;
        try {
            MessageEvaluationContext context = new MessageEvaluationContext(tempProperties);

            ret = realFilterData.getCompiledExpression().evaluate(context);
        } catch (Throwable e) {
            log.error("Message Filter error, " + realFilterData + ", " + tempProperties, e);
        }

        log.debug("Pull eval result: {}, {}, {}", ret, realFilterData, tempProperties);

        if (ret == null || !(ret instanceof Boolean)) {
            return false;
        }

        return (Boolean) ret;
    }
}
