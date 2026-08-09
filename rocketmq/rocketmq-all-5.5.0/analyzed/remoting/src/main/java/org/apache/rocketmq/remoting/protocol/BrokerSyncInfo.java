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

package org.apache.rocketmq.remoting.protocol;

/**
 * Broker 主从同步信息：Slave 上线前从 Master 获取 HA 地址、刷盘位点与服务地址。
 */
public class BrokerSyncInfo extends RemotingSerializable {
    /** Slave 上线同步前获取的 Master HA 传输地址。 */
    /** Master HA 监听地址。 */
    private String masterHaAddress;

    /** Master 已刷盘 CommitLog 位点。 */
    private long masterFlushOffset;

    /** Master Broker 服务地址。 */
    private String masterAddress;

    /** 构造主从同步三元组。 */
    public BrokerSyncInfo(String masterHaAddress, long masterFlushOffset, String masterAddress) {
        this.masterHaAddress = masterHaAddress;
        this.masterFlushOffset = masterFlushOffset;
        this.masterAddress = masterAddress;
    }

    /** 返回 Master HA 地址。 */
    public String getMasterHaAddress() {
        return masterHaAddress;
    }

    /** 设置 Master HA 地址。 */
    public void setMasterHaAddress(String masterHaAddress) {
        this.masterHaAddress = masterHaAddress;
    }

    /** 返回 Master 刷盘位点。 */
    public long getMasterFlushOffset() {
        return masterFlushOffset;
    }

    /** 设置 Master 刷盘位点。 */
    public void setMasterFlushOffset(long masterFlushOffset) {
        this.masterFlushOffset = masterFlushOffset;
    }

    /** 返回 Master 服务地址。 */
    public String getMasterAddress() {
        return masterAddress;
    }

    /** 设置 Master 服务地址。 */
    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    /** 返回便于日志排查的字符串表示。 */
    @Override
    public String toString() {
        return "BrokerSyncInfo{" +
            "masterHaAddress='" + masterHaAddress + '\'' +
            ", masterFlushOffset=" + masterFlushOffset +
            ", masterAddress=" + masterAddress +
            '}';
    }
}
