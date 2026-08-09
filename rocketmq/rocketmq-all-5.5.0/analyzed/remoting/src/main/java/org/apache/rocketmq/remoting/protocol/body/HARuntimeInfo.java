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

import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Broker 主从 HA 运行时快照：Master 身份、同步从节点数及各连接/客户端传输状态。
 */
public class HARuntimeInfo extends RemotingSerializable {

    /** 当前节点是否为 Master。 */
    private boolean master;
    /** Master CommitLog 最大物理偏移。 */
    private long masterCommitLogMaxOffset;
    /** 处于同步状态的从节点数量。 */
    private int inSyncSlaveNums;
    /** Master 侧各从节点 HA 连接详情。 */
    private List<HAConnectionRuntimeInfo> haConnectionInfo = new ArrayList<>();
    /** 从节点 HA 客户端运行态（Master 侧为空对象）。 */
    private HAClientRuntimeInfo haClientRuntimeInfo = new HAClientRuntimeInfo();

    /** 返回是否为 Master。 */
    public boolean isMaster() {
        return this.master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public long getMasterCommitLogMaxOffset() {
        return this.masterCommitLogMaxOffset;
    }

    public void setMasterCommitLogMaxOffset(long masterCommitLogMaxOffset) {
        this.masterCommitLogMaxOffset = masterCommitLogMaxOffset;
    }

    public int getInSyncSlaveNums() {
        return this.inSyncSlaveNums;
    }

    public void setInSyncSlaveNums(int inSyncSlaveNums) {
        this.inSyncSlaveNums = inSyncSlaveNums;
    }

    public List<HAConnectionRuntimeInfo> getHaConnectionInfo() {
        return this.haConnectionInfo;
    }

    public void setHaConnectionInfo(List<HAConnectionRuntimeInfo> haConnectionInfo) {
        this.haConnectionInfo = haConnectionInfo;
    }

    public HAClientRuntimeInfo getHaClientRuntimeInfo() {
        return this.haClientRuntimeInfo;
    }

    public void setHaClientRuntimeInfo(HAClientRuntimeInfo haClientRuntimeInfo) {
        this.haClientRuntimeInfo = haClientRuntimeInfo;
    }

    /** Master 视角下单条 HA 连接：从节点地址、复制进度与同步状态。 */
    public static class HAConnectionRuntimeInfo extends RemotingSerializable {
        /** 从节点地址。 */
        private String addr;
        /** 从节点已 ACK 的 CommitLog 偏移。 */
        private long slaveAckOffset;
        /** 与 Master 最大偏移的差值。 */
        private long diff;
        /** 是否判定为同步副本。 */
        private boolean inSync;
        /** 每秒传输字节数。 */
        private long transferredByteInSecond;
        /** 本次 HA 传输起始偏移。 */
        private long transferFromWhere;

        public String getAddr() {
            return this.addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public long getSlaveAckOffset() {
            return this.slaveAckOffset;
        }

        public void setSlaveAckOffset(long slaveAckOffset) {
            this.slaveAckOffset = slaveAckOffset;
        }

        public long getDiff() {
            return this.diff;
        }

        public void setDiff(long diff) {
            this.diff = diff;
        }

        public boolean isInSync() {
            return this.inSync;
        }

        public void setInSync(boolean inSync) {
            this.inSync = inSync;
        }

        public long getTransferredByteInSecond() {
            return this.transferredByteInSecond;
        }

        public void setTransferredByteInSecond(long transferredByteInSecond) {
            this.transferredByteInSecond = transferredByteInSecond;
        }

        public long getTransferFromWhere() {
            return transferFromWhere;
        }

        public void setTransferFromWhere(long transferFromWhere) {
            this.transferFromWhere = transferFromWhere;
        }
    }

    /** 从节点 HA 客户端：Master 地址、读写时间戳及复制进度。 */
    public static class HAClientRuntimeInfo extends RemotingSerializable {
        /** 所连 Master 地址。 */
        private String masterAddr;
        private long transferredByteInSecond;
        /** 本地已同步的最大偏移。 */
        private long maxOffset;
        /** 最近一次从 Master 读取时间戳。 */
        private long lastReadTimestamp;
        /** 最近一次写入本地 CommitLog 时间戳。 */
        private long lastWriteTimestamp;
        /** Master 已刷盘偏移（从节点观测值）。 */
        private long masterFlushOffset;
        /** HA 客户端是否已激活连接。 */
        private boolean isActivated = false;

        public String getMasterAddr() {
            return this.masterAddr;
        }

        public void setMasterAddr(String masterAddr) {
            this.masterAddr = masterAddr;
        }

        public long getTransferredByteInSecond() {
            return this.transferredByteInSecond;
        }

        public void setTransferredByteInSecond(long transferredByteInSecond) {
            this.transferredByteInSecond = transferredByteInSecond;
        }

        public long getMaxOffset() {
            return this.maxOffset;
        }

        public void setMaxOffset(long maxOffset) {
            this.maxOffset = maxOffset;
        }

        public long getLastReadTimestamp() {
            return this.lastReadTimestamp;
        }

        public void setLastReadTimestamp(long lastReadTimestamp) {
            this.lastReadTimestamp = lastReadTimestamp;
        }

        public long getLastWriteTimestamp() {
            return this.lastWriteTimestamp;
        }

        public void setLastWriteTimestamp(long lastWriteTimestamp) {
            this.lastWriteTimestamp = lastWriteTimestamp;
        }

        public long getMasterFlushOffset() {
            return masterFlushOffset;
        }

        public void setMasterFlushOffset(long masterFlushOffset) {
            this.masterFlushOffset = masterFlushOffset;
        }
    }

}
