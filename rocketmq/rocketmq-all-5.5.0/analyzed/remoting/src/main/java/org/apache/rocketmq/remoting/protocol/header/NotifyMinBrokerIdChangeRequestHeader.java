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
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 最小 BrokerId 变更通知请求头：集群内同步 BrokerId 重分配及 HA 地址信息。
 */
@RocketMQAction(value = RequestCode.NOTIFY_MIN_BROKER_ID_CHANGE, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class NotifyMinBrokerIdChangeRequestHeader implements CommandCustomHeader {
    /** 新的最小 BrokerId，可为空。 */
    @CFNullable
    private Long minBrokerId;

    /** Broker 名称，可为空。 */
    @CFNullable
    private String brokerName;

    /** 持有最小 BrokerId 的 Broker 地址，可为空。 */
    @CFNullable
    private String minBrokerAddr;

    /** 下线 Broker 地址，可为空。 */
    @CFNullable
    private String offlineBrokerAddr;

    /** HA 同步目标 Broker 地址，可为空。 */
    @CFNullable
    private String haBrokerAddr;

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回最小 BrokerId。 */
    public Long getMinBrokerId() {
        return minBrokerId;
    }

    /** 设置最小 BrokerId。 */
    public void setMinBrokerId(Long minBrokerId) {
        this.minBrokerId = minBrokerId;
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回最小 BrokerId 持有者地址。 */
    public String getMinBrokerAddr() {
        return minBrokerAddr;
    }

    /** 设置最小 BrokerId 持有者地址。 */
    public void setMinBrokerAddr(String minBrokerAddr) {
        this.minBrokerAddr = minBrokerAddr;
    }

    /** 返回下线 Broker 地址。 */
    public String getOfflineBrokerAddr() {
        return offlineBrokerAddr;
    }

    /** 设置下线 Broker 地址。 */
    public void setOfflineBrokerAddr(String offlineBrokerAddr) {
        this.offlineBrokerAddr = offlineBrokerAddr;
    }

    /** 返回 HA Broker 地址。 */
    public String getHaBrokerAddr() {
        return haBrokerAddr;
    }

    /** 设置 HA Broker 地址。 */
    public void setHaBrokerAddr(String haBrokerAddr) {
        this.haBrokerAddr = haBrokerAddr;
    }
}
