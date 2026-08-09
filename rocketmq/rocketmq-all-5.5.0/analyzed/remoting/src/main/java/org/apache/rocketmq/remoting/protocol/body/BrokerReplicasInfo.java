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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 集群 Broker 副本拓扑：各 brokerName 的 Master/ISR 与滞后副本信息。
 */
public class BrokerReplicasInfo extends RemotingSerializable  {
    /** brokerName → 副本详情。 */
    private Map<String/*brokerName*/, ReplicasInfo> replicasInfoTable;

    /** 默认构造，初始化空副本表。 */
    public BrokerReplicasInfo() {
        this.replicasInfoTable = new HashMap<>();
    }

    /** 注册或覆盖某 Broker 的副本信息。 */
    public void addReplicaInfo(final String brokerName, final ReplicasInfo replicasInfo) {
        this.replicasInfoTable.put(brokerName, replicasInfo);
    }

    /** 返回副本信息表。 */
    public Map<String, ReplicasInfo> getReplicasInfoTable() {
        return replicasInfoTable;
    }

    public void setReplicasInfoTable(
            Map<String, ReplicasInfo> replicasInfoTable) {
        this.replicasInfoTable = replicasInfoTable;
    }

    /** 单 Broker 副本集：Master 身份、epoch 及同步/非同步副本列表。 */
    public static class ReplicasInfo extends RemotingSerializable {

        /** 当前 Master 的 brokerId。 */
        private Long masterBrokerId;

        /** Master 访问地址。 */
        private String masterAddress;
        /** Master epoch（Controller  fencing 用）。 */
        private Integer masterEpoch;
        /** 同步副本集 epoch。 */
        private Integer syncStateSetEpoch;
        /** 在同步副本集（ISR）内的副本。 */
        private List<ReplicaIdentity> inSyncReplicas;
        /** 滞后或未入 ISR 的副本。 */
        private List<ReplicaIdentity> notInSyncReplicas;

        public ReplicasInfo(Long masterBrokerId, String masterAddress, int masterEpoch, int syncStateSetEpoch,
                            List<ReplicaIdentity> inSyncReplicas, List<ReplicaIdentity> notInSyncReplicas) {
            this.masterBrokerId = masterBrokerId;
            this.masterAddress = masterAddress;
            this.masterEpoch = masterEpoch;
            this.syncStateSetEpoch = syncStateSetEpoch;
            this.inSyncReplicas = inSyncReplicas;
            this.notInSyncReplicas = notInSyncReplicas;
        }

        public String getMasterAddress() {
            return masterAddress;
        }

        public void setMasterAddress(String masterAddress) {
            this.masterAddress = masterAddress;
        }

        public int getMasterEpoch() {
            return masterEpoch;
        }

        public void setMasterEpoch(int masterEpoch) {
            this.masterEpoch = masterEpoch;
        }

        public int getSyncStateSetEpoch() {
            return syncStateSetEpoch;
        }

        public void setSyncStateSetEpoch(int syncStateSetEpoch) {
            this.syncStateSetEpoch = syncStateSetEpoch;
        }

        public List<ReplicaIdentity> getInSyncReplicas() {
            return inSyncReplicas;
        }

        public void setInSyncReplicas(
                List<ReplicaIdentity> inSyncReplicas) {
            this.inSyncReplicas = inSyncReplicas;
        }

        public List<ReplicaIdentity> getNotInSyncReplicas() {
            return notInSyncReplicas;
        }

        public void setNotInSyncReplicas(
                List<ReplicaIdentity> notInSyncReplicas) {
            this.notInSyncReplicas = notInSyncReplicas;
        }

        public void setMasterBrokerId(Long masterBrokerId) {
            this.masterBrokerId = masterBrokerId;
        }

        public Long getMasterBrokerId() {
            return masterBrokerId;
        }

        /** 判断指定副本是否在 ISR 列表中。 */
        public boolean isExistInSync(String brokerName, Long brokerId, String brokerAddress) {
            return this.getInSyncReplicas().contains(new ReplicaIdentity(brokerName, brokerId, brokerAddress));
        }

        /** 判断指定副本是否在非同步列表中。 */
        public boolean isExistInNotSync(String brokerName, Long brokerId, String brokerAddress) {
            return this.getNotInSyncReplicas().contains(new ReplicaIdentity(brokerName, brokerId, brokerAddress));
        }

        /** 判断副本是否存在于 ISR 或非同步列表任一之中。 */
        public boolean isExistInAllReplicas(String brokerName, Long brokerId, String brokerAddress) {
            return this.isExistInSync(brokerName, brokerId, brokerAddress) || this.isExistInNotSync(brokerName, brokerId, brokerAddress);
        }
    }

    /** 副本身份：brokerName、brokerId、地址及存活标记。 */
    public static class ReplicaIdentity extends RemotingSerializable {
        /** Broker 逻辑名。 */
        private String brokerName;
        /** 副本 brokerId。 */
        private Long brokerId;

        /** 副本访问地址。 */
        private String brokerAddress;
        /** 副本是否存活（心跳探测结果）。 */
        private Boolean alive;

        /** 构造副本身份，alive 默认 false。 */
        public ReplicaIdentity(String brokerName, Long brokerId, String brokerAddress) {
            this.brokerName = brokerName;
            this.brokerId = brokerId;
            this.brokerAddress = brokerAddress;
            this.alive = false;
        }

        public ReplicaIdentity(String brokerName, Long brokerId, String brokerAddress, Boolean alive) {
            this.brokerName = brokerName;
            this.brokerId = brokerId;
            this.brokerAddress = brokerAddress;
            this.alive = alive;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public void setBrokerName(String brokerName) {
            this.brokerName = brokerName;
        }

        public String getBrokerAddress() {
            return brokerAddress;
        }

        public void setBrokerAddress(String brokerAddress) {
            this.brokerAddress = brokerAddress;
        }

        public Long getBrokerId() {
            return brokerId;
        }

        public void setBrokerId(Long brokerId) {
            this.brokerId = brokerId;
        }

        public Boolean getAlive() {
            return alive;
        }

        public void setAlive(Boolean alive) {
            this.alive = alive;
        }

        @Override
        public String toString() {
            return "ReplicaIdentity{" +
                    "brokerName='" + brokerName + '\'' +
                    ", brokerId=" + brokerId +
                    ", brokerAddress='" + brokerAddress + '\'' +
                    ", alive=" + alive +
                    '}';
        }

        /** 按 brokerName、brokerId、brokerAddress 判等。 */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReplicaIdentity that = (ReplicaIdentity) o;
            return brokerName.equals(that.brokerName) && brokerId.equals(that.brokerId) && brokerAddress.equals(that.brokerAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(brokerName, brokerId, brokerAddress);
        }
    }
}
