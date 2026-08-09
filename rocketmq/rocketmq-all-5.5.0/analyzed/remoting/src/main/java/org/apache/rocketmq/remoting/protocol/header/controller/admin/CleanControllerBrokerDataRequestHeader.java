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

package org.apache.rocketmq.remoting.protocol.header.controller.admin;

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
 * 清理 Controller 侧 Broker 元数据的请求头：可指定待清理的 brokerId 集合及是否清理存活 Broker。
 */
@RocketMQAction(value = RequestCode.CLEAN_BROKER_DATA, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class CleanControllerBrokerDataRequestHeader implements CommandCustomHeader {

    /** 目标集群名称，可为空。 */
    @CFNullable
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;

    /** 目标 Broker 组名称。 */
    @CFNotNull
    private String brokerName;

    /** 待清理的 Controller brokerId 集合（字符串形式），可为空。 */
    @CFNullable
    private String brokerControllerIdsToClean;

    /** 是否同时清理仍存活的 Broker 数据。 */
    private boolean isCleanLivingBroker = false;
    /** 请求发起时间戳（毫秒）。 */
    private long invokeTime = System.currentTimeMillis();

    /** 默认构造。 */
    public CleanControllerBrokerDataRequestHeader() {
    }

    /** 指定集群、Broker 组、待清理 brokerId 集合及存活清理标志的构造。 */
    public CleanControllerBrokerDataRequestHeader(String clusterName, String brokerName, String brokerIdSetToClean,
        boolean isCleanLivingBroker) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.brokerControllerIdsToClean = brokerIdSetToClean;
        this.isCleanLivingBroker = isCleanLivingBroker;
    }

    /** 指定集群、Broker 组与待清理 brokerId 集合的构造（默认不清理存活 Broker）。 */
    public CleanControllerBrokerDataRequestHeader(String clusterName, String brokerName, String brokerIdSetToClean) {
        this(clusterName, brokerName, brokerIdSetToClean, false);
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回请求发起时间戳。 */
    public long getInvokeTime() {
        return invokeTime;
    }

    /** 设置请求发起时间戳。 */
    public void setInvokeTime(long invokeTime) {
        this.invokeTime = invokeTime;
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

    /** 返回待清理的 Controller brokerId 集合。 */
    public String getBrokerControllerIdsToClean() {
        return brokerControllerIdsToClean;
    }

    /** 设置待清理的 Controller brokerId 集合。 */
    public void setBrokerControllerIdsToClean(String brokerIdSetToClean) {
        this.brokerControllerIdsToClean = brokerIdSetToClean;
    }

    /** 返回是否清理存活 Broker 数据。 */
    public boolean isCleanLivingBroker() {
        return isCleanLivingBroker;
    }

    /** 设置是否清理存活 Broker 数据。 */
    public void setCleanLivingBroker(boolean cleanLivingBroker) {
        isCleanLivingBroker = cleanLivingBroker;
    }

    /** 返回含集群、Broker 组与清理参数的调试字符串。 */
    @Override
    public String toString() {
        return "CleanControllerBrokerDataRequestHeader{" +
                "clusterName='" + clusterName + '\'' +
                ", brokerName='" + brokerName + '\'' +
                ", brokerIdSetToClean='" + brokerControllerIdsToClean + '\'' +
                ", isCleanLivingBroker=" + isCleanLivingBroker +
                '}';
    }
}
