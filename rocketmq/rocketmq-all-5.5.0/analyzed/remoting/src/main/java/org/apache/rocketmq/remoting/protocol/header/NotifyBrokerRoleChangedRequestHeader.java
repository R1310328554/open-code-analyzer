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
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * Broker 角色变更通知请求头：集群内同步新 Master 地址、Epoch 及 BrokerId。
 */
@RocketMQAction(value = RequestCode.NOTIFY_BROKER_ROLE_CHANGED, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class NotifyBrokerRoleChangedRequestHeader implements CommandCustomHeader {
    /** 新 Master Broker 地址。 */
    private String masterAddress;
    /** Master Epoch 版本号。 */
    private Integer masterEpoch;
    /** 同步状态集 Epoch 版本号。 */
    private Integer syncStateSetEpoch;
    /** 新 Master Broker ID。 */
    private Long masterBrokerId;

    /** 无参构造。 */
    public NotifyBrokerRoleChangedRequestHeader() {
    }

    /** 按 Master 地址、BrokerId 及 Epoch 构造。 */
    public NotifyBrokerRoleChangedRequestHeader(String masterAddress, Long masterBrokerId, Integer masterEpoch, Integer syncStateSetEpoch) {
        this.masterAddress = masterAddress;
        this.masterEpoch = masterEpoch;
        this.syncStateSetEpoch = syncStateSetEpoch;
        this.masterBrokerId = masterBrokerId;
    }

    /** 返回 Master 地址。 */
    public String getMasterAddress() {
        return masterAddress;
    }

    /** 设置 Master 地址。 */
    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    /** 返回 Master Epoch。 */
    public Integer getMasterEpoch() {
        return masterEpoch;
    }

    /** 设置 Master Epoch。 */
    public void setMasterEpoch(Integer masterEpoch) {
        this.masterEpoch = masterEpoch;
    }

    /** 返回同步状态集 Epoch。 */
    public Integer getSyncStateSetEpoch() {
        return syncStateSetEpoch;
    }

    /** 设置同步状态集 Epoch。 */
    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {
        this.syncStateSetEpoch = syncStateSetEpoch;
    }

    /** 返回 Master Broker ID。 */
    public Long getMasterBrokerId() {
        return masterBrokerId;
    }

    /** 设置 Master Broker ID。 */
    public void setMasterBrokerId(Long masterBrokerId) {
        this.masterBrokerId = masterBrokerId;
    }

    /** 返回含 Master 信息的调试字符串。 */
    @Override
    public String toString() {
        return "NotifyBrokerRoleChangedRequestHeader{" +
                "masterAddress='" + masterAddress + '\'' +
                ", masterEpoch=" + masterEpoch +
                ", syncStateSetEpoch=" + syncStateSetEpoch +
                ", masterBrokerId=" + masterBrokerId +
                '}';
    }

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }
}
