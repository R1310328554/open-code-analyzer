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

package org.apache.rocketmq.remoting.protocol.body;


import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.header.controller.ElectMasterResponseHeader;

import java.util.Set;

/**
 * Controller 模式下 Master 角色变更通知条目：成员组、Master 地址/Epoch 及 syncStateSet。
 */
public class RoleChangeNotifyEntry {

    /** Broker 成员组信息。 */
    private final BrokerMemberGroup brokerMemberGroup;

    /** 新 Master 服务地址。 */
    private final String masterAddress;

    /** 新 Master 的 brokerId。 */
    private final Long masterBrokerId;

    /** Master Epoch 版本号。 */
    private final int masterEpoch;

    /** syncStateSet Epoch 版本号。 */
    private final int syncStateSetEpoch;

    /** 同步副本 brokerId 集合。 */
    private final Set<Long> syncStateSet;

    /** 全字段构造。 */
    public RoleChangeNotifyEntry(BrokerMemberGroup brokerMemberGroup, String masterAddress, Long masterBrokerId, int masterEpoch, int syncStateSetEpoch, Set<Long> syncStateSet) {
        this.brokerMemberGroup = brokerMemberGroup;
        this.masterAddress = masterAddress;
        this.masterEpoch = masterEpoch;
        this.syncStateSetEpoch = syncStateSetEpoch;
        this.masterBrokerId = masterBrokerId;
        this.syncStateSet = syncStateSet;
    }

    /** 从选举 Master 响应命令解析角色变更条目。 */
    public static RoleChangeNotifyEntry convert(RemotingCommand electMasterResponse) {
        final ElectMasterResponseHeader header = (ElectMasterResponseHeader) electMasterResponse.readCustomHeader();
        BrokerMemberGroup brokerMemberGroup = null;
        Set<Long> syncStateSet = null;

        if (electMasterResponse.getBody() != null && electMasterResponse.getBody().length > 0) {
            ElectMasterResponseBody body = RemotingSerializable.decode(electMasterResponse.getBody(), ElectMasterResponseBody.class);
            brokerMemberGroup = body.getBrokerMemberGroup();
            syncStateSet = body.getSyncStateSet();
        }

        return new RoleChangeNotifyEntry(brokerMemberGroup, header.getMasterAddress(), header.getMasterBrokerId(), header.getMasterEpoch(), header.getSyncStateSetEpoch(), syncStateSet);
    }


    /** 返回成员组。 */
    public BrokerMemberGroup getBrokerMemberGroup() {
        return brokerMemberGroup;
    }

    /** 返回 Master 地址。 */
    public String getMasterAddress() {
        return masterAddress;
    }

    public int getMasterEpoch() {
        return masterEpoch;
    }

    public int getSyncStateSetEpoch() {
        return syncStateSetEpoch;
    }

    public Long getMasterBrokerId() {
        return masterBrokerId;
    }

    /** 返回 syncStateSet。 */
    public Set<Long> getSyncStateSet() {
        return syncStateSet;
    }
}
