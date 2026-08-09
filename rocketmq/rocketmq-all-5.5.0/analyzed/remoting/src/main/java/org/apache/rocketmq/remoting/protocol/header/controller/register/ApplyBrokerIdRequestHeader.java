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
 * Broker 向 Controller 申请 brokerId 的请求头：携带集群、Broker 组名、申请的 brokerId 与注册校验码。
 */
@RocketMQAction(value = RequestCode.CONTROLLER_APPLY_BROKER_ID, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class ApplyBrokerIdRequestHeader implements CommandCustomHeader {

    /** 目标集群名称。 */
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;

    /** 目标 Broker 组名称。 */
    private String brokerName;

    /** 申请的 brokerId。 */
    private Long appliedBrokerId;

    /** 注册校验码，用于防重放。 */
    private String registerCheckCode;

    /** 默认构造。 */
    public ApplyBrokerIdRequestHeader() {

    }

    /** 指定集群、Broker 组、申请 brokerId 与校验码的构造。 */
    public ApplyBrokerIdRequestHeader(String clusterName, String brokerName, Long appliedBrokerId, String registerCheckCode) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.appliedBrokerId = appliedBrokerId;
        this.registerCheckCode = registerCheckCode;
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

    /** 返回申请的 brokerId。 */
    public Long getAppliedBrokerId() {
        return appliedBrokerId;
    }

    /** 返回注册校验码。 */
    public String getRegisterCheckCode() {
        return registerCheckCode;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 设置申请的 brokerId。 */
    public void setAppliedBrokerId(Long appliedBrokerId) {
        this.appliedBrokerId = appliedBrokerId;
    }

    /** 设置注册校验码。 */
    public void setRegisterCheckCode(String registerCheckCode) {
        this.registerCheckCode = registerCheckCode;
    }
}
