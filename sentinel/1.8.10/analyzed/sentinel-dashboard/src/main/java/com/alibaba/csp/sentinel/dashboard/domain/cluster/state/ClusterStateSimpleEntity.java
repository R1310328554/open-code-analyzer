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
 * 集群运行模式与可用性摘要实体，供 Dashboard 快速展示客户端/服务端是否就绪。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterStateSimpleEntity {

    /** 集群运行模式（未启用/客户端/服务端等）。 */
    private Integer mode;
    /** 状态最后变更时间戳（毫秒）。 */
    private Long lastModified;
    /** 客户端组件是否可用。 */
    private Boolean clientAvailable;
    /** 服务端组件是否可用。 */
    private Boolean serverAvailable;

    public Integer getMode() {
        return mode;
    }

    public ClusterStateSimpleEntity setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public ClusterStateSimpleEntity setLastModified(Long lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public Boolean getClientAvailable() {
        return clientAvailable;
    }

    public ClusterStateSimpleEntity setClientAvailable(Boolean clientAvailable) {
        this.clientAvailable = clientAvailable;
        return this;
    }

    public Boolean getServerAvailable() {
        return serverAvailable;
    }

    public ClusterStateSimpleEntity setServerAvailable(Boolean serverAvailable) {
        this.serverAvailable = serverAvailable;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterStateSimpleEntity{" +
            "mode=" + mode +
            ", lastModified=" + lastModified +
            ", clientAvailable=" + clientAvailable +
            ", serverAvailable=" + serverAvailable +
            '}';
    }
}
