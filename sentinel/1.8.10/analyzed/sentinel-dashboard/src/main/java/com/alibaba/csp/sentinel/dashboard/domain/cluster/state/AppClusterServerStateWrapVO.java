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
package com.alibaba.csp.sentinel.dashboard.domain.cluster.state;

/**
 * 应用维度集群令牌服务端状态包装视图，聚合机器标识与 {@link ClusterServerStateVO} 详情。
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public class AppClusterServerStateWrapVO {

    /** 机器唯一标识，格式为 {@code ip@transport_command_port}。 */

    private String id;

    /** 机器 IP 地址。 */
    private String ip;
    /** Sentinel 命令端口。 */
    private Integer port;

    /** 当前已建立的客户端连接数量。 */
    private Integer connectedCount;

    /** 该服务端机器是否归属当前应用。 */
    private Boolean belongToApp;

    /** 集群令牌服务端运行状态详情。 */
    private ClusterServerStateVO state;

    public String getId() {
        return id;
    }

    public AppClusterServerStateWrapVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public AppClusterServerStateWrapVO setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public AppClusterServerStateWrapVO setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Boolean getBelongToApp() {
        return belongToApp;
    }

    public AppClusterServerStateWrapVO setBelongToApp(Boolean belongToApp) {
        this.belongToApp = belongToApp;
        return this;
    }

    public Integer getConnectedCount() {
        return connectedCount;
    }

    public AppClusterServerStateWrapVO setConnectedCount(Integer connectedCount) {
        this.connectedCount = connectedCount;
        return this;
    }

    public ClusterServerStateVO getState() {
        return state;
    }

    public AppClusterServerStateWrapVO setState(ClusterServerStateVO state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "AppClusterServerStateWrapVO{" +
            "id='" + id + '\'' +
            ", ip='" + ip + '\'' +
            ", port='" + port + '\'' +
            ", belongToApp=" + belongToApp +
            ", state=" + state +
            '}';
    }
}
