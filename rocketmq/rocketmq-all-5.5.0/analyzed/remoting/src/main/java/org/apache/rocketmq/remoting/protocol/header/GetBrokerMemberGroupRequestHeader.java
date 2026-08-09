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
 * 查询 Broker 成员组（Master/Slave 副本集）的请求头：指定集群与 Broker 名。
 */
@RocketMQAction(value = RequestCode.GET_BROKER_MEMBER_GROUP, action = Action.GET)
public class GetBrokerMemberGroupRequestHeader implements CommandCustomHeader {
    /** 集群名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;

    /** Broker 逻辑名称。 */
    @CFNotNull
    private String brokerName;

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 设置集群名称。 */
    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(final String brokerName) {
        this.brokerName = brokerName;
    }

    @Override
    public void checkFields() throws RemotingCommandException {

    }
}
