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

package org.apache.rocketmq.remoting.protocol.header.namesrv;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * Broker 向 NameServer 上报心跳的请求头：携带集群、地址、epoch 与消费位点等存活信息。
 */
@RocketMQAction(value = RequestCode.BROKER_HEARTBEAT, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class BrokerHeartbeatRequestHeader implements CommandCustomHeader {
    /** 集群名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;
    /** Broker 对外服务地址。 */
    @CFNotNull
    private String brokerAddr;
    /** Broker 组名称。 */
    @CFNotNull
    private String brokerName;
    /** Broker 的 brokerId，可为空。 */
    @CFNullable
    private Long brokerId;
    /** Master epoch 版本号，可为空。 */
    @CFNullable
    private Integer epoch;
    /** CommitLog 最大物理 offset，可为空。 */
    @CFNullable
    private Long maxOffset;
    /** 已确认刷盘 offset，可为空。 */
    @CFNullable
    private Long confirmOffset;
    /** 心跳超时时间（毫秒），可为空。 */
    @CFNullable
    private Long heartbeatTimeoutMills;
    /** 选主优先级，可为空。 */
    @CFNullable
    private Integer electionPriority;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回 Broker 地址。 */
    public String getBrokerAddr() {
        return brokerAddr;
    }

    /** 设置 Broker 地址。 */
    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /** 返回 Broker 组名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回 Master epoch。 */
    public Integer getEpoch() {
        return epoch;
    }

    /** 设置 Master epoch。 */
    public void setEpoch(Integer epoch) {
        this.epoch = epoch;
    }

    /** 返回最大物理 offset。 */
    public Long getMaxOffset() {
        return maxOffset;
    }

    /** 设置最大物理 offset。 */
    public void setMaxOffset(Long maxOffset) {
        this.maxOffset = maxOffset;
    }

    /** 返回已确认刷盘 offset。 */
    public Long getConfirmOffset() {
        return confirmOffset;
    }

    /** 设置已确认刷盘 offset。 */
    public void setConfirmOffset(Long confirmOffset) {
        this.confirmOffset = confirmOffset;
    }

    /** 返回 brokerId。 */
    public Long getBrokerId() {
        return brokerId;
    }

    /** 设置 brokerId。 */
    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    /** 返回心跳超时时间（毫秒）。 */
    public Long getHeartbeatTimeoutMills() {
        return heartbeatTimeoutMills;
    }

    /** 设置心跳超时时间（毫秒）。 */
    public void setHeartbeatTimeoutMills(Long heartbeatTimeoutMills) {
        this.heartbeatTimeoutMills = heartbeatTimeoutMills;
    }

    /** 返回选主优先级。 */
    public Integer getElectionPriority() {
        return electionPriority;
    }

    /** 设置选主优先级。 */
    public void setElectionPriority(Integer electionPriority) {
        this.electionPriority = electionPriority;
    }
}
