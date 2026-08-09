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
package org.apache.rocketmq.controller.impl.heartbeat;

import io.netty.channel.Channel;
import java.io.Serializable;

/**
 * Broker 存活快照：记录心跳地址、超时、Netty 通道、副本 epoch 与消费位点等运行时状态。
 * 供 Controller 心跳管理器维护在线 Broker 表并在 Raft 模式下序列化复制。
 */
public class BrokerLiveInfo implements Serializable {
    private static final long serialVersionUID = 3612173344946510993L;
    /** Broker 逻辑名称。 */
    private final String brokerName;

    /** Broker 对外服务地址。 */
    private String brokerAddr;
    /** 心跳超时毫秒数，超过则视为离线。 */
    private long heartbeatTimeoutMillis;
    /** 与 Broker 建立的 Netty 通道。 */
    private Channel channel;
    /** 副本 Broker ID。 */
    private long brokerId;
    /** 最近一次收到心跳的时间戳。 */
    private long lastUpdateTimestamp;
    /** 当前副本 epoch，用于主从切换一致性判断。 */
    private int epoch;
    /** 已写入 CommitLog 的最大偏移量。 */
    private long maxOffset;
    /** 已确认同步的偏移量。 */
    private long confirmOffset;
    /** 参与选主的优先级，数值越小优先级越高。 */
    private Integer electionPriority;

    /** 构造不含 confirmOffset 的存活信息（首次注册场景）。 */
    public BrokerLiveInfo(String brokerName, String brokerAddr, long brokerId, long lastUpdateTimestamp,
        long heartbeatTimeoutMillis, Channel channel, int epoch, long maxOffset, Integer electionPriority) {
        this.brokerName = brokerName;
        this.brokerAddr = brokerAddr;
        this.brokerId = brokerId;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
        this.channel = channel;
        this.epoch = epoch;
        this.electionPriority = electionPriority;
        this.maxOffset = maxOffset;
    }

    public BrokerLiveInfo(String brokerName, String brokerAddr, long brokerId, long lastUpdateTimestamp,
        long heartbeatTimeoutMillis, Channel channel, int epoch, long maxOffset, Integer electionPriority,
        long confirmOffset) {
        this.brokerName = brokerName;
        this.brokerAddr = brokerAddr;
        this.brokerId = brokerId;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
        this.channel = channel;
        this.epoch = epoch;
        this.maxOffset = maxOffset;
        this.electionPriority = electionPriority;
        this.confirmOffset = confirmOffset;
    }

    @Override
    public String toString() {
        return "BrokerLiveInfo{" +
            "brokerName='" + brokerName + '\'' +
            ", brokerAddr='" + brokerAddr + '\'' +
            ", heartbeatTimeoutMillis=" + heartbeatTimeoutMillis +
            ", channel=" + channel +
            ", brokerId=" + brokerId +
            ", lastUpdateTimestamp=" + lastUpdateTimestamp +
            ", epoch=" + epoch +
            ", maxOffset=" + maxOffset +
            ", confirmOffset=" + confirmOffset +
            '}';
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    public long getHeartbeatTimeoutMillis() {
        return heartbeatTimeoutMillis;
    }

    public void setHeartbeatTimeoutMillis(long heartbeatTimeoutMillis) {
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public String getBrokerAddr() {
        return brokerAddr;
    }

    public void setConfirmOffset(long confirmOffset) {
        this.confirmOffset = confirmOffset;
    }

    public void setElectionPriority(Integer electionPriority) {
        this.electionPriority = electionPriority;
    }

    public Integer getElectionPriority() {
        return electionPriority;
    }

    /** 返回已确认同步偏移量。 */
    public long getConfirmOffset() {
        return confirmOffset;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
