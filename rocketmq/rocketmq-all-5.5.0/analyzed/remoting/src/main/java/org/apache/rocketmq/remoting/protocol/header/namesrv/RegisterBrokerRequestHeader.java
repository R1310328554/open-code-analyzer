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
 * $Id: RegisterBrokerRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header.namesrv;

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
 * Broker 向 NameServer 注册的请求头：上报集群、地址、HA 地址及 brokerId 等元数据。
 * 请求体可携带 Topic 配置列表，compressed 与 bodyCrc32 标识压缩与校验。
 */
@RocketMQAction(value = RequestCode.REGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class RegisterBrokerRequestHeader implements CommandCustomHeader {
    /** Broker 组名称。 */
    @CFNotNull
    private String brokerName;
    /** Broker 对外服务地址。 */
    @CFNotNull
    private String brokerAddr;
    /** 集群名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;
    /** HA 服务地址，用于主从同步。 */
    @CFNotNull
    private String haServerAddr;
    /** Broker 的 brokerId（0 表示 Master）。 */
    @CFNotNull
    private Long brokerId;
    /** 心跳超时时间（毫秒），可为空。 */
    @CFNullable
    private Long heartbeatTimeoutMillis;
    /** 是否允许 Acting Master 模式，可为空。 */
    @CFNullable
    private Boolean enableActingMaster;

    /** 请求体是否压缩。 */
    private boolean compressed;

    /** 请求体 CRC32 校验值。 */
    private Integer bodyCrc32 = 0;

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

    /** 返回 HA 服务地址。 */
    public String getHaServerAddr() {
        return haServerAddr;
    }

    /** 设置 HA 服务地址。 */
    public void setHaServerAddr(String haServerAddr) {
        this.haServerAddr = haServerAddr;
    }

    /** 返回 brokerId。 */
    public Long getBrokerId() {
        return brokerId;
    }

    /** 设置 brokerId。 */
    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    /** 返回心跳超时时间（毫秒）。 */
    public Long getHeartbeatTimeoutMillis() {
        return heartbeatTimeoutMillis;
    }

    /** 设置心跳超时时间（毫秒）。 */
    public void setHeartbeatTimeoutMillis(Long heartbeatTimeoutMillis) {
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
    }

    /** 返回请求体是否压缩。 */
    public boolean isCompressed() {
        return compressed;
    }

    /** 设置请求体是否压缩。 */
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    /** 返回请求体 CRC32 校验值。 */
    public Integer getBodyCrc32() {
        return bodyCrc32;
    }

    /** 设置请求体 CRC32 校验值。 */
    public void setBodyCrc32(Integer bodyCrc32) {
        this.bodyCrc32 = bodyCrc32;
    }

    /** 返回是否允许 Acting Master 模式。 */
    public Boolean getEnableActingMaster() {
        return enableActingMaster;
    }

    /** 设置是否允许 Acting Master 模式。 */
    public void setEnableActingMaster(Boolean enableActingMaster) {
        this.enableActingMaster = enableActingMaster;
    }
}
