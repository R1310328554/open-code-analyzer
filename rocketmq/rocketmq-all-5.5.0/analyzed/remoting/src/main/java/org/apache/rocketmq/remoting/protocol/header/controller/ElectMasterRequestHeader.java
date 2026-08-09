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
package org.apache.rocketmq.remoting.protocol.header.controller;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * Controller 选举 Master 的请求头：支持 Broker 触发、Controller 触发与管理员指定选举。
 */
@RocketMQAction(value = RequestCode.CONTROLLER_ELECT_MASTER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class ElectMasterRequestHeader implements CommandCustomHeader {

    /** 目标集群名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName = "";

    /** 目标 Broker 组名称。 */
    @CFNotNull
    private String brokerName = "";

    /**
     * 候选 brokerId。
     * Broker 触发选主：Broker 组首次选举时，该 brokerId 将被选为 Master。
     * 管理员触发选主：即 assignedBrokerId，Broker 有效时优先将其选为新 Master。
     */
    @CFNotNull
    private Long brokerId = -1L;

    /** 是否为指定选举（管理员触发时为 true）。 */
    @CFNotNull
    private Boolean designateElect = false;

    /** 请求发起时间戳（毫秒）。 */
    private Long invokeTime = System.currentTimeMillis();

    /** 默认构造。 */
    public ElectMasterRequestHeader() {
    }

    /** 仅指定 Broker 组名的构造（Controller 触发）。 */
    public ElectMasterRequestHeader(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 指定集群、Broker 组与 brokerId 的构造。 */
    public ElectMasterRequestHeader(String clusterName, String brokerName, Long brokerId) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.brokerId = brokerId;
    }

    /** 指定集群、Broker 组、brokerId 及是否指定选举的构造。 */
    public ElectMasterRequestHeader(String clusterName, String brokerName, Long brokerId, boolean designateElect) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.brokerId = brokerId;
        this.designateElect = designateElect;
    }

    /** 创建 Broker 触发的选主请求头。 */
    public static ElectMasterRequestHeader ofBrokerTrigger(String clusterName, String brokerName,
        Long brokerId) {
        return new ElectMasterRequestHeader(clusterName, brokerName, brokerId);
    }

    /** 创建 Controller 触发的选主请求头。 */
    public static ElectMasterRequestHeader ofControllerTrigger(String brokerName) {
        return new ElectMasterRequestHeader(brokerName);
    }

    /** 创建管理员指定选举的请求头。 */
    public static ElectMasterRequestHeader ofAdminTrigger(String clusterName, String brokerName, Long brokerId) {
        return new ElectMasterRequestHeader(clusterName, brokerName, brokerId, true);
    }

    /** 返回 Broker 组名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回候选 brokerId。 */
    public Long getBrokerId() {
        return brokerId;
    }

    /** 设置候选 brokerId。 */
    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /** 返回是否为指定选举。 */
    public boolean getDesignateElect() {
        return this.designateElect;
    }

    /** 返回请求发起时间戳。 */
    public Long getInvokeTime() {
        return invokeTime;
    }

    /** 设置请求发起时间戳。 */
    public void setInvokeTime(Long invokeTime) {
        this.invokeTime = invokeTime;
    }

    /** 返回含集群、Broker 组、brokerId 与指定选举标志的调试字符串。 */
    @Override
    public String toString() {
        return "ElectMasterRequestHeader{" +
                "clusterName='" + clusterName + '\'' +
                ", brokerName='" + brokerName + '\'' +
                ", brokerId=" + brokerId +
                ", designateElect=" + designateElect +
                '}';
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
