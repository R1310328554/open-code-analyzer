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

package org.apache.rocketmq.remoting.protocol.subscription;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.common.base.MoreObjects;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.attribute.LiteSubModel;

import static org.apache.rocketmq.common.SubscriptionGroupAttributes.LITE_SUB_CLIENT_MAX_EVENT_COUNT_ATTRIBUTE;
import static org.apache.rocketmq.common.SubscriptionGroupAttributes.LITE_SUB_CLIENT_QUOTA_ATTRIBUTE;
import static org.apache.rocketmq.common.SubscriptionGroupAttributes.LITE_SUB_MODEL_ATTRIBUTE;
import static org.apache.rocketmq.common.SubscriptionGroupAttributes.LITE_SUB_RESET_OFFSET_EXCLUSIVE_ATTRIBUTE;
import static org.apache.rocketmq.common.SubscriptionGroupAttributes.LITE_BIND_TOPIC_ATTRIBUTE;
import static org.apache.rocketmq.common.SubscriptionGroupAttributes.LITE_SUB_RESET_OFFSET_UNSUBSCRIBE_ATTRIBUTE;
import static org.apache.rocketmq.common.SubscriptionGroupAttributes.LITE_SUB_WILDCARD_ATTRIBUTE;

import static org.apache.rocketmq.common.SubscriptionGroupAttributes.PRIORITY_FACTOR_ATTRIBUTE;

/**
 * 订阅组（消费组）配置：控制消费开关、重试、广播/顺序及 Lite 订阅等 Broker 侧策略。
 */
public class SubscriptionGroupConfig {

    /** 消费组名称。 */
    private String groupName;

    /** 是否允许该组消费。 */
    private boolean consumeEnable = true;
    /** 是否从最小 offset 开始消费。 */
    private boolean consumeFromMinEnable = true;
    /** 是否允许广播消费。 */
    private boolean consumeBroadcastEnable = true;
    /** 是否顺序消费。 */
    private boolean consumeMessageOrderly = false;

    /** 重试 Topic 队列数量。 */
    private int retryQueueNums = 1;

    /** 最大重试次数。 */
    private int retryMaxTimes = 16;
    /** 组级重试策略。 */
    private GroupRetryPolicy groupRetryPolicy = new GroupRetryPolicy();

    /** 关联 Broker 实例 ID（默认 Master）。 */
    private long brokerId = MixAll.MASTER_ID;

    /** 消费过慢时路由到的 Broker ID。 */
    private long whichBrokerWhenConsumeSlowly = 1;

    /** 是否在 Consumer 实例变更时通知客户端。 */
    private boolean notifyConsumerIdsChangedEnable = true;

    /** 订阅组系统标志位。 */
    private int groupSysFlag = 0;

    /** 消费超时分钟数（仅 Push 消费者有效）。 */
    private int consumeTimeoutMinute = 15;

    /** 组内各 Topic 订阅数据集合。 */
    private Set<SimpleSubscriptionData> subscriptionDataSet;

    /** 扩展属性键值（Lite 订阅、优先级等）。 */
    private Map<String, String> attributes = new HashMap<>();

    /** 返回消费组名称。 */
    public String getGroupName() {
        return groupName;
    }

    /** 设置消费组名称。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /** 是否启用消费。 */
    public boolean isConsumeEnable() {
        return consumeEnable;
    }

    /** 设置是否启用消费。 */
    public void setConsumeEnable(boolean consumeEnable) {
        this.consumeEnable = consumeEnable;
    }

    public boolean isConsumeFromMinEnable() {
        return consumeFromMinEnable;
    }

    public void setConsumeFromMinEnable(boolean consumeFromMinEnable) {
        this.consumeFromMinEnable = consumeFromMinEnable;
    }

    public boolean isConsumeBroadcastEnable() {
        return consumeBroadcastEnable;
    }

    public void setConsumeBroadcastEnable(boolean consumeBroadcastEnable) {
        this.consumeBroadcastEnable = consumeBroadcastEnable;
    }

    public boolean isConsumeMessageOrderly() {
        return consumeMessageOrderly;
    }

    public void setConsumeMessageOrderly(boolean consumeMessageOrderly) {
        this.consumeMessageOrderly = consumeMessageOrderly;
    }

    public int getRetryQueueNums() {
        return retryQueueNums;
    }

    public void setRetryQueueNums(int retryQueueNums) {
        this.retryQueueNums = retryQueueNums;
    }

    /** 返回最大重试次数。 */
    public int getRetryMaxTimes() {
        return retryMaxTimes;
    }

    /** 设置最大重试次数。 */
    public void setRetryMaxTimes(int retryMaxTimes) {
        this.retryMaxTimes = retryMaxTimes;
    }

    /** 返回组重试策略。 */
    public GroupRetryPolicy getGroupRetryPolicy() {
        return groupRetryPolicy;
    }

    /** 设置组重试策略。 */
    public void setGroupRetryPolicy(GroupRetryPolicy groupRetryPolicy) {
        this.groupRetryPolicy = groupRetryPolicy;
    }

    public long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }

    public long getWhichBrokerWhenConsumeSlowly() {
        return whichBrokerWhenConsumeSlowly;
    }

    public void setWhichBrokerWhenConsumeSlowly(long whichBrokerWhenConsumeSlowly) {
        this.whichBrokerWhenConsumeSlowly = whichBrokerWhenConsumeSlowly;
    }

    public boolean isNotifyConsumerIdsChangedEnable() {
        return notifyConsumerIdsChangedEnable;
    }

    public void setNotifyConsumerIdsChangedEnable(final boolean notifyConsumerIdsChangedEnable) {
        this.notifyConsumerIdsChangedEnable = notifyConsumerIdsChangedEnable;
    }

    public int getGroupSysFlag() {
        return groupSysFlag;
    }

    public void setGroupSysFlag(int groupSysFlag) {
        this.groupSysFlag = groupSysFlag;
    }

    public int getConsumeTimeoutMinute() {
        return consumeTimeoutMinute;
    }

    public void setConsumeTimeoutMinute(int consumeTimeoutMinute) {
        this.consumeTimeoutMinute = consumeTimeoutMinute;
    }

    public Set<SimpleSubscriptionData> getSubscriptionDataSet() {
        return subscriptionDataSet;
    }

    public void setSubscriptionDataSet(Set<SimpleSubscriptionData> subscriptionDataSet) {
        this.subscriptionDataSet = subscriptionDataSet;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /** 从扩展属性读取优先级因子。 */
    @JSONField(serialize = false, deserialize = false)
    public long getPriorityFactor() {
        String factorStr = null == attributes ? null : attributes.get(PRIORITY_FACTOR_ATTRIBUTE.getName());
        return NumberUtils.toLong(factorStr, PRIORITY_FACTOR_ATTRIBUTE.getDefaultValue());
    }

    /** 设置 Lite 订阅绑定的 Topic。 */
    @JSONField(serialize = false, deserialize = false)
    public void setLiteBindTopic(String liteBindTopic) {
        if (liteBindTopic != null) {
            attributes.put(LITE_BIND_TOPIC_ATTRIBUTE.getName(), liteBindTopic);
        }
    }

    /** 返回 Lite 订阅绑定的 Topic。 */
    @JSONField(serialize = false, deserialize = false)
    public String getLiteBindTopic() {
        return attributes.get(LITE_BIND_TOPIC_ATTRIBUTE.getName());
    }

    /** 返回 Lite 订阅客户端配额。 */
    @JSONField(serialize = false, deserialize = false)
    public int getLiteSubClientQuota() {
        long quota = LITE_SUB_CLIENT_QUOTA_ATTRIBUTE.getDefaultValue();
        String quotaStr = attributes.get(LITE_SUB_CLIENT_QUOTA_ATTRIBUTE.getName());
        if (quotaStr != null) {
            quota = Long.parseLong(quotaStr);
        }
        return Math.toIntExact(quota);
    }

    /** 设置 Lite 订阅为独占模式。 */
    @JSONField(serialize = false, deserialize = false)
    public void setLiteSubExclusive(boolean liteSubExclusive) {
        if (liteSubExclusive) {
            attributes.put(LITE_SUB_MODEL_ATTRIBUTE.getName(), LiteSubModel.Exclusive.name());
        }
    }

    /** 是否为 Lite 独占订阅。 */
    @JSONField(serialize = false, deserialize = false)
    public boolean isLiteSubExclusive() {
        String subLiteModel = attributes.get(LITE_SUB_MODEL_ATTRIBUTE.getName());
        return Objects.equals(LiteSubModel.Exclusive.name(), subLiteModel);
    }

    /** 独占模式下是否在特定场景重置 offset。 */
    /** 独占模式是否重置 offset。 */
    @JSONField(serialize = false, deserialize = false)
    public boolean isResetOffsetInExclusiveMode() {
        String boolStr = attributes.get(LITE_SUB_RESET_OFFSET_EXCLUSIVE_ATTRIBUTE.getName());
        return Boolean.parseBoolean(boolStr);
    }

    /** 取消订阅时是否重置 offset。 */
    @JSONField(serialize = false, deserialize = false)
    public boolean isResetOffsetOnUnsubscribe() {
        String boolStr = attributes.get(LITE_SUB_RESET_OFFSET_UNSUBSCRIBE_ATTRIBUTE.getName());
        return Boolean.parseBoolean(boolStr);
    }

    /** 返回 Lite 客户端最大事件数（-1 表示未设置）。 */
    @JSONField(serialize = false, deserialize = false)
    public int getMaxClientEventCount() {
        String content = attributes.get(LITE_SUB_CLIENT_MAX_EVENT_COUNT_ATTRIBUTE.getName());
        if (content == null) {
            return -1;
        }
        return NumberUtils.toInt(content, -1);
    }

    /** 标记为通配 Lite 消费组。 */
    @JSONField(serialize = false, deserialize = false)
    public void setWildcardLiteGroup(boolean wildcard) {
        if (wildcard) {
            attributes.put(LITE_SUB_WILDCARD_ATTRIBUTE.getName(), "true");
        }
    }

    /** 是否为通配 Lite 消费组。 */
    @JSONField(serialize = false, deserialize = false)
    public boolean isWildcardLiteGroup() {
        return attributes.containsKey(LITE_SUB_WILDCARD_ATTRIBUTE.getName());
    }

    /** 计算配置哈希。 */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (brokerId ^ (brokerId >>> 32));
        result = prime * result + (consumeBroadcastEnable ? 1231 : 1237);
        result = prime * result + (consumeEnable ? 1231 : 1237);
        result = prime * result + (consumeFromMinEnable ? 1231 : 1237);
        result = prime * result + (notifyConsumerIdsChangedEnable ? 1231 : 1237);
        result = prime * result + (consumeMessageOrderly ? 1231 : 1237);
        result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result + retryMaxTimes;
        result = prime * result + retryQueueNums;
        result =
            prime * result + (int) (whichBrokerWhenConsumeSlowly ^ (whichBrokerWhenConsumeSlowly >>> 32));
        result = prime * result + groupSysFlag;
        result = prime * result + consumeTimeoutMinute;
        result = prime * result + ((subscriptionDataSet == null) ? 0 : subscriptionDataSet.hashCode());
        result = prime * result + attributes.hashCode();
        return result;
    }

    /** 比较两组配置是否等价。 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubscriptionGroupConfig other = (SubscriptionGroupConfig) obj;
        return new EqualsBuilder()
            .append(groupName, other.groupName)
            .append(consumeEnable, other.consumeEnable)
            .append(consumeFromMinEnable, other.consumeFromMinEnable)
            .append(consumeBroadcastEnable, other.consumeBroadcastEnable)
            .append(consumeMessageOrderly, other.consumeMessageOrderly)
            .append(retryQueueNums, other.retryQueueNums)
            .append(retryMaxTimes, other.retryMaxTimes)
            .append(whichBrokerWhenConsumeSlowly, other.whichBrokerWhenConsumeSlowly)
            .append(notifyConsumerIdsChangedEnable, other.notifyConsumerIdsChangedEnable)
            .append(groupSysFlag, other.groupSysFlag)
            .append(consumeTimeoutMinute, other.consumeTimeoutMinute)
            .append(subscriptionDataSet, other.subscriptionDataSet)
            .append(attributes, other.attributes)
            .isEquals();
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("groupName", groupName)
            .add("consumeEnable", consumeEnable)
            .add("consumeFromMinEnable", consumeFromMinEnable)
            .add("consumeBroadcastEnable", consumeBroadcastEnable)
            .add("consumeMessageOrderly", consumeMessageOrderly)
            .add("retryQueueNums", retryQueueNums)
            .add("retryMaxTimes", retryMaxTimes)
            .add("groupRetryPolicy", groupRetryPolicy)
            .add("brokerId", brokerId)
            .add("whichBrokerWhenConsumeSlowly", whichBrokerWhenConsumeSlowly)
            .add("notifyConsumerIdsChangedEnable", notifyConsumerIdsChangedEnable)
            .add("groupSysFlag", groupSysFlag)
            .add("consumeTimeoutMinute", consumeTimeoutMinute)
            .add("subscriptionDataSet", subscriptionDataSet)
            .add("attributes", attributes)
            .toString();
    }
}
