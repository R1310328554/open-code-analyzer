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

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * Broker 向 Controller 注册的响应头：返回 Master 副本信息与同步副本集 epoch。
 */
public class RegisterBrokerToControllerResponseHeader implements CommandCustomHeader {

    /** 集群名称。 */
    private String clusterName;

    /** Broker 组名称。 */
    private String brokerName;

    /** 当前 Master 的 brokerId。 */
    private Long masterBrokerId;

    /** Master 对外服务地址。 */
    private String masterAddress;

    /** Master 的 epoch 版本号。 */
    private Integer masterEpoch;

    /** 同步副本集（SyncStateSet）的 epoch。 */
    private Integer syncStateSetEpoch;

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 默认构造。 */
    public RegisterBrokerToControllerResponseHeader() {
    }

    /** 指定集群与 Broker 组名的构造。 */
    public RegisterBrokerToControllerResponseHeader(String clusterName, String brokerName) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
    }

    /** 设置 Master brokerId。 */
    public void setMasterBrokerId(Long masterBrokerId) {
        this.masterBrokerId = masterBrokerId;
    }

    /** 设置 Master 地址。 */
    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    /** 设置 Master epoch。 */
    public void setMasterEpoch(Integer masterEpoch) {
        this.masterEpoch = masterEpoch;
    }

    /** 设置同步副本集 epoch。 */
    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {
        this.syncStateSetEpoch = syncStateSetEpoch;
    }

    /** 返回 Master epoch。 */
    public Integer getMasterEpoch() {
        return masterEpoch;
    }

    /** 返回同步副本集 epoch。 */
    public Integer getSyncStateSetEpoch() {
        return syncStateSetEpoch;
    }

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 返回 Broker 组名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 返回 Master brokerId。 */
    public Long getMasterBrokerId() {
        return masterBrokerId;
    }

    /** 返回 Master 地址。 */
    public String getMasterAddress() {
        return masterAddress;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /** 设置 Broker 组名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
}
