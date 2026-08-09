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

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;


/**
 * Controller 选举 Master 的响应头：返回新 Master 的 brokerId、地址、epoch 及 SyncStateSet epoch。
 */
public class ElectMasterResponseHeader implements CommandCustomHeader {

    /** 新 Master 的 brokerId。 */
    private Long masterBrokerId;
    /** 新 Master 的网络地址。 */
    private String masterAddress;
    /** 新 Master 的 epoch 版本号。 */
    private Integer masterEpoch;
    /** 当前 SyncStateSet 的 epoch 版本号。 */
    private Integer syncStateSetEpoch;

    /** 默认构造。 */
    public ElectMasterResponseHeader() {
    }

    /** 指定 Master brokerId、地址、epoch 与 SyncStateSet epoch 的构造。 */
    public ElectMasterResponseHeader(Long masterBrokerId, String masterAddress, Integer masterEpoch, Integer syncStateSetEpoch) {
        this.masterBrokerId = masterBrokerId;
        this.masterAddress = masterAddress;
        this.masterEpoch = masterEpoch;
        this.syncStateSetEpoch = syncStateSetEpoch;
    }

    /** 返回 Master 地址。 */
    public String getMasterAddress() {
        return masterAddress;
    }

    /** 设置 Master 地址。 */
    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    /** 返回 Master epoch。 */
    public Integer getMasterEpoch() {
        return masterEpoch;
    }

    /** 设置 Master epoch。 */
    public void setMasterEpoch(Integer masterEpoch) {
        this.masterEpoch = masterEpoch;
    }

    /** 返回 SyncStateSet epoch。 */
    public Integer getSyncStateSetEpoch() {
        return syncStateSetEpoch;
    }

    /** 设置 SyncStateSet epoch。 */
    public void setSyncStateSetEpoch(Integer syncStateSetEpoch) {
        this.syncStateSetEpoch = syncStateSetEpoch;
    }

    /** 设置 Master brokerId。 */
    public void setMasterBrokerId(Long masterBrokerId) {
        this.masterBrokerId = masterBrokerId;
    }

    /** 返回 Master brokerId。 */
    public Long getMasterBrokerId() {
        return masterBrokerId;
    }

    /** 返回含 Master 信息与 epoch 的调试字符串。 */
    @Override
    public String toString() {
        return "ElectMasterResponseHeader{" +
                "masterBrokerId=" + masterBrokerId +
                ", masterAddress='" + masterAddress + '\'' +
                ", masterEpoch=" + masterEpoch +
                ", syncStateSetEpoch=" + syncStateSetEpoch +
                '}';
    }

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
