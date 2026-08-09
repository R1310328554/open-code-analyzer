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

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 从集群移除 Broker 的请求头：指定 Broker 名称、集群名与 Broker ID。
 * 由 NameServer 或 Controller 发起，用于 Broker 下线或故障摘除。
 */
@RocketMQAction(value = RequestCode.REMOVE_BROKER, resource = ResourceType.CLUSTER,action = Action.UPDATE)
public class RemoveBrokerRequestHeader implements CommandCustomHeader {
    /** Broker 实例名称。 */
    @CFNotNull
    private String brokerName;
    /** Broker 所属集群名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String brokerClusterName;
    /** Broker 角色 ID（0 为 Master，非 0 为 Slave）。 */
    @CFNotNull
    private Long brokerId;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回集群名称。 */
    public String getBrokerClusterName() {
        return brokerClusterName;
    }

    /** 设置集群名称。 */
    public void setBrokerClusterName(String brokerClusterName) {
        this.brokerClusterName = brokerClusterName;
    }

    /** 返回 Broker ID。 */
    public Long getBrokerId() {
        return brokerId;
    }

    /** 设置 Broker ID。 */
    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }
}
