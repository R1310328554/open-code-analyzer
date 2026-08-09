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
package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;

/**
 * Pop 消息请求头：长轮询 Pop 消费，支持顺序消费与过滤表达式。
 */
@RocketMQAction(value = RequestCode.POP_MESSAGE, action = Action.SUB)
public class PopMessageRequestHeader extends TopicQueueRequestHeader {
    /** 消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 目标队列 ID。 */
    @CFNotNull
    private int queueId;
    /** 单次 Pop 的最大消息条数。 */
    @CFNotNull
    private int maxMsgNums;
    /** 消息不可见时长（毫秒）。 */
    @CFNotNull
    private long invisibleTime;
    /** 长轮询等待时长（毫秒）。 */
    @CFNotNull
    private long pollTime;
    /** 请求创建时间戳（毫秒）。 */
    @CFNotNull
    private long bornTime;
    /** Pop 初始化模式。 */
    @CFNotNull
    private int initMode;

    /** 过滤表达式类型。 */
    private String expType;
    /** 过滤表达式内容。 */
    private String exp;

    /**
     * 标记为顺序消费；为 true 时：
     * 1. 不提交消费偏移量
     * 2. 不进行 Pop 重试（无重试机制）
     * 3. 不追加检查点（无重试机制）
     */
    private Boolean order = Boolean.FALSE;

    /** Pop 尝试 ID，用于幂等与重试追踪。 */
    private String attemptId;

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public void setInitMode(int initMode) {
        this.initMode = initMode;
    }

    public int getInitMode() {
        return initMode;
    }

    public long getInvisibleTime() {
        return invisibleTime;
    }

    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    public long getPollTime() {
        return pollTime;
    }

    public void setPollTime(long pollTime) {
        this.pollTime = pollTime;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public long getBornTime() {
        return bornTime;
    }

    public void setBornTime(long bornTime) {
        this.bornTime = bornTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回队列 ID，负值归一化为 -1 表示不限定。 */
    public Integer getQueueId() {
        if (queueId < 0) {
            return -1;
        }
        return queueId;
    }

    @Override
    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public int getMaxMsgNums() {
        return maxMsgNums;
    }

    public void setMaxMsgNums(int maxMsgNums) {
        this.maxMsgNums = maxMsgNums;
    }

    /** 判断长轮询是否已超时过多（超过 500ms 余量）。 */
    public boolean isTimeoutTooMuch() {
        return System.currentTimeMillis() - bornTime - pollTime > 500;
    }

    public String getExpType() {
        return expType;
    }

    public void setExpType(String expType) {
        this.expType = expType;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public Boolean getOrder() {
        return order;
    }

    public void setOrder(Boolean order) {
        this.order = order;
    }

    /** 返回是否为顺序消费模式。 */
    public boolean isOrder() {
        return this.order != null && this.order.booleanValue();
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    /** 返回含 Pop 参数的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("topic", topic)
            .add("queueId", queueId)
            .add("maxMsgNums", maxMsgNums)
            .add("invisibleTime", invisibleTime)
            .add("pollTime", pollTime)
            .add("bornTime", bornTime)
            .add("initMode", initMode)
            .add("expType", expType)
            .add("exp", exp)
            .add("order", order)
            .add("attemptId", attemptId)
            .toString();
    }
}
