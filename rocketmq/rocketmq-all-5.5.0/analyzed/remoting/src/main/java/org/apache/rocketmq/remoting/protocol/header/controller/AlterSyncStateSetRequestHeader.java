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
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * Controller 修改同步副本集（SyncStateSet）的请求头：携带 Broker 组名、Master brokerId 与 epoch。
 */
@RocketMQAction(value = RequestCode.CONTROLLER_ALTER_SYNC_STATE_SET, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class AlterSyncStateSetRequestHeader implements CommandCustomHeader {
    /** 目标 Broker 组名称。 */
    private String brokerName;
    /** 当前 Master 的 brokerId。 */
    private Long masterBrokerId;
    /** Master 的 epoch 版本号。 */
    private Integer masterEpoch;
    /** 请求发起时间戳（毫秒）。 */
    private long invokeTime = System.currentTimeMillis();

    /** 默认构造。 */
    public AlterSyncStateSetRequestHeader() {
    }

    /** 指定 Broker 组、Master brokerId 与 epoch 的构造。 */
    public AlterSyncStateSetRequestHeader(String brokerName, Long masterBrokerId, Integer masterEpoch) {
        this.brokerName = brokerName;
        this.masterBrokerId = masterBrokerId;
        this.masterEpoch = masterEpoch;
    }

    /** 返回请求发起时间戳。 */
    public long getInvokeTime() {
        return invokeTime;
    }

    /** 设置请求发起时间戳。 */
    public void setInvokeTime(long invokeTime) {
        this.invokeTime = invokeTime;
    }

    /** 返回 Broker 组名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回 Master brokerId。 */
    public Long getMasterBrokerId() {
        return masterBrokerId;
    }

    /** 设置 Master brokerId。 */
    public void setMasterBrokerId(Long masterBrokerId) {
        this.masterBrokerId = masterBrokerId;
    }

    /** 返回 Master epoch。 */
    public Integer getMasterEpoch() {
        return masterEpoch;
    }

    /** 设置 Master epoch。 */
    public void setMasterEpoch(Integer masterEpoch) {
        this.masterEpoch = masterEpoch;
    }

    /** 返回含 Broker 组、Master brokerId 与 epoch 的调试字符串。 */
    @Override
    public String toString() {
        return "AlterSyncStateSetRequestHeader{" +
                "brokerName='" + brokerName + '\'' +
                ", masterBrokerId=" + masterBrokerId +
                ", masterEpoch=" + masterEpoch +
                '}';
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
