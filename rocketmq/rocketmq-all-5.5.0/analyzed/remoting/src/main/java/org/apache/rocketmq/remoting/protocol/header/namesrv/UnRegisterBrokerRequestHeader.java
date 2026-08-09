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

/**
 * $Id: UnRegisterBrokerRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header.namesrv;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 从 NameServer 注销 Broker 的请求头：携带集群名、Broker 组名、地址与 brokerId。
 */
@RocketMQAction(value = RequestCode.UNREGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class UnRegisterBrokerRequestHeader implements CommandCustomHeader {
    /** Broker 组名称。 */
    @CFNotNull
    private String brokerName;
    /** 待注销 Broker 实例地址。 */
    @CFNotNull
    private String brokerAddr;
    /** 所属集群名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;
    /** Broker 实例 ID（0 为 Master）。 */
    @CFNotNull
    private Long brokerId;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回 Broker 组名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
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

    /** 返回 brokerId。 */
    public Long getBrokerId() {
        return brokerId;
    }

    /** 设置 brokerId。 */
    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }
}
