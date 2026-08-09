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
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.rpc.RpcRequestHeader;

/**
 * Lite Pop 消息请求头：轻量级 Pop 消费，指定 Topic 与不可见时间等参数。
 */
public class PopLiteMessageRequestHeader extends RpcRequestHeader {

    /** Consumer 客户端 ID。 */
    @CFNotNull
    private String clientId;
    /** 消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 单次 Pop 的最大消息条数。 */
    @CFNotNull
    private int maxMsgNum;
    /** 消息不可见时长（毫秒）。 */
    @CFNotNull
    private long invisibleTime;
    /** 长轮询等待时长（毫秒）。 */
    @CFNotNull
    private long pollTime;
    /** 请求创建时间戳（毫秒）。 */
    @CFNotNull
    private long bornTime;

    /** Pop 尝试 ID，用于幂等与重试追踪。 */
    private String attemptId;

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getMaxMsgNum() {
        return maxMsgNum;
    }

    public void setMaxMsgNum(int maxMsgNum) {
        this.maxMsgNum = maxMsgNum;
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

    public long getBornTime() {
        return bornTime;
    }

    public void setBornTime(long bornTime) {
        this.bornTime = bornTime;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    /** 判断长轮询是否已超时过多（超过 500ms 余量）。 */
    public boolean isTimeoutTooMuch() {
        return System.currentTimeMillis() - bornTime - pollTime > 500;
    }

    /** 返回含 Pop 参数的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consumerGroup", consumerGroup)
            .add("topic", topic)
            .add("maxMsgNum", maxMsgNum)
            .add("invisibleTime", invisibleTime)
            .add("pollTime", pollTime)
            .add("bornTime", bornTime)
            .add("attemptId", attemptId)
            .add("clientId", clientId)
            .toString();
    }
}
