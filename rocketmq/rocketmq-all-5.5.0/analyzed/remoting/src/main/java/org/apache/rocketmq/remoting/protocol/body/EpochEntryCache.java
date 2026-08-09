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

import java.util.List;
import org.apache.rocketmq.remoting.protocol.EpochEntry;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Broker Epoch 条目缓存：集群/Broker 标识、Epoch 列表及最大 CommitLog 位点。
 * 用于 Controller 模式下副本同步状态上报。
 */
public class EpochEntryCache extends RemotingSerializable {
    /** 所属集群名称。 */
    private String clusterName;
    /** Broker 逻辑名称。 */
    private String brokerName;
    /** Broker 副本 ID（0 通常为 Master）。 */
    private long brokerId;
    /** Epoch 条目列表（leader epoch 与起始位点）。 */
    private List<EpochEntry> epochList;
    /** 当前最大 CommitLog 位点。 */
    private long maxOffset;

    /** 全字段构造。 */
    public EpochEntryCache(String clusterName, String brokerName, long brokerId, List<EpochEntry> epochList, long maxOffset) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.brokerId = brokerId;
        this.epochList = epochList;
        this.maxOffset = maxOffset;
    }

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    /** 返回 Broker ID。 */
    public long getBrokerId() {
        return brokerId;
    }

    /** 设置 Broker ID。 */
    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }

    /** 返回 Epoch 列表。 */
    public List<EpochEntry> getEpochList() {
        return this.epochList;
    }

    /** 设置 Epoch 列表。 */
    public void setEpochList(List<EpochEntry> epochList) {
        this.epochList = epochList;
    }

    /** 返回最大位点。 */
    public long getMaxOffset() {
        return maxOffset;
    }

    /** 设置最大位点。 */
    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "EpochEntryCache{" +
            "clusterName='" + clusterName + '\'' +
            ", brokerName='" + brokerName + '\'' +
            ", brokerId=" + brokerId +
            ", epochList=" + epochList +
            ", maxOffset=" + maxOffset +
            '}';
    }
}
