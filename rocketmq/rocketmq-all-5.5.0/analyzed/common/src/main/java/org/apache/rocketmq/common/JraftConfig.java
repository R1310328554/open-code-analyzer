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
package org.apache.rocketmq.common;

/**
 * jRaft Controller 的 Raft 相关配置项。
 */
public class JraftConfig {
    /** 选举超时（毫秒）。 */
    private int jRaftElectionTimeoutMs = 1000;

    /** 扫描等待超时（毫秒）。 */
    private int jRaftScanWaitTimeoutMs = 1000;
    /** 快照间隔（秒）。 */
    private int jRaftSnapshotIntervalSecs = 3600;
    /** jRaft 组 ID。 */
    private String jRaftGroupId = "jRaft-Controller";
    /** 本节点 jRaft Server 地址。 */
    private String jRaftServerId = "localhost:9880";
    /** 初始集群成员配置。 */
    private String jRaftInitConf = "localhost:9880,localhost:9881,localhost:9882";
    /** Controller RPC 地址列表。 */
    private String jRaftControllerRPCAddr = "localhost:9770,localhost:9771,localhost:9772";

    /** 获取选举超时。 */
    public int getjRaftElectionTimeoutMs() {
        return jRaftElectionTimeoutMs;
    }

    /** 设置选举超时。 */
    public void setjRaftElectionTimeoutMs(int jRaftElectionTimeoutMs) {
        this.jRaftElectionTimeoutMs = jRaftElectionTimeoutMs;
    }

    /** 获取快照间隔。 */
    public int getjRaftSnapshotIntervalSecs() {
        return jRaftSnapshotIntervalSecs;
    }

    /** 设置快照间隔。 */
    public void setjRaftSnapshotIntervalSecs(int jRaftSnapshotIntervalSecs) {
        this.jRaftSnapshotIntervalSecs = jRaftSnapshotIntervalSecs;
    }

    /** 获取 jRaft 组 ID。 */
    public String getjRaftGroupId() {
        return jRaftGroupId;
    }

    /** 设置 jRaft 组 ID。 */
    public void setjRaftGroupId(String jRaftGroupId) {
        this.jRaftGroupId = jRaftGroupId;
    }

    /** 获取本节点 Server ID。 */
    public String getjRaftServerId() {
        return jRaftServerId;
    }

    /** 设置本节点 Server ID。 */
    public void setjRaftServerId(String jRaftServerId) {
        this.jRaftServerId = jRaftServerId;
    }

    /** 获取初始集群配置。 */
    public String getjRaftInitConf() {
        return jRaftInitConf;
    }

    /** 设置初始集群配置。 */
    public void setjRaftInitConf(String jRaftInitConf) {
        this.jRaftInitConf = jRaftInitConf;
    }

    /** 获取 Controller RPC 地址。 */
    public String getjRaftControllerRPCAddr() {
        return jRaftControllerRPCAddr;
    }

    /** 设置 Controller RPC 地址。 */
    public void setjRaftControllerRPCAddr(String jRaftControllerRPCAddr) {
        this.jRaftControllerRPCAddr = jRaftControllerRPCAddr;
    }

    /** 返回本节点 jRaft 地址（同 ServerId）。 */
    public String getjRaftAddress() {
        return this.jRaftServerId;
    }

    /** 获取扫描等待超时。 */
    public int getjRaftScanWaitTimeoutMs() {
        return jRaftScanWaitTimeoutMs;
    }

    /** 设置扫描等待超时。 */
    public void setjRaftScanWaitTimeoutMs(int jRaftScanWaitTimeoutMs) {
        this.jRaftScanWaitTimeoutMs = jRaftScanWaitTimeoutMs;
    }
}