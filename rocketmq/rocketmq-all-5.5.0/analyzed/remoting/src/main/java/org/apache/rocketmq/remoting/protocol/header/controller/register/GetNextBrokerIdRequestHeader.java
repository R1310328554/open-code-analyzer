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

package org.apache.rocketmq.remoting.protocol.header.controller.register;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 向 Controller 查询下一个可用 brokerId 的请求头：指定集群与 Broker 组名。
 */
@RocketMQAction(value = RequestCode.CONTROLLER_GET_NEXT_BROKER_ID, resource = ResourceType.CLUSTER, action = Action.GET)
public class GetNextBrokerIdRequestHeader implements CommandCustomHeader {

    /** 目标集群名称。 */
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;

    /** 目标 Broker 组名称。 */
    private String brokerName;

    /** 默认构造。 */
    public GetNextBrokerIdRequestHeader() {

    }

    /** 指定集群与 Broker 组名的构造。 */
    public GetNextBrokerIdRequestHeader(String clusterName, String brokerName) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
    }

    /** 返回含集群与 Broker 组名的调试字符串。 */
    @Override
    public String toString() {
        return "GetNextBrokerIdRequestHeader{" +
                "clusterName='" + clusterName + '\'' +
                ", brokerName='" + brokerName + '\'' +
                '}';
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 返回 Broker 组名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
