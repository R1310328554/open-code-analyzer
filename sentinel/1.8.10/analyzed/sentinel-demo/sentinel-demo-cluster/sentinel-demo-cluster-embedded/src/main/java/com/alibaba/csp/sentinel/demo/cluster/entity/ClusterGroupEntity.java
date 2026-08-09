/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.cluster.entity;

import java.util.Set;

/**
 * 集群分组实体：描述 Token Server 机器及其关联 Client 集合。
 * <p>machineId 格式为 {@code ip@commandPort}，commandPort 为 Dashboard 通信端口。</p>
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterGroupEntity {

    /** Token Server 机器标识（ip@commandPort）。 */
    private String machineId;
    /** Token Server 对外 IP。 */
    private String ip;
    /** Token Server 监听端口。 */
    private Integer port;

    /** 归属该 Server 的 Client machineId 集合。 */
    private Set<String> clientSet;

    public String getMachineId() {
        return machineId;
    }

    public ClusterGroupEntity setMachineId(String machineId) {
        this.machineId = machineId;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public ClusterGroupEntity setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ClusterGroupEntity setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Set<String> getClientSet() {
        return clientSet;
    }

    public ClusterGroupEntity setClientSet(Set<String> clientSet) {
        this.clientSet = clientSet;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterGroupEntity{" +
            "machineId='" + machineId + '\'' +
            ", ip='" + ip + '\'' +
            ", port=" + port +
            ", clientSet=" + clientSet +
            '}';
    }
}
