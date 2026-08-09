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
package com.alibaba.csp.sentinel.dashboard.domain.cluster;

import java.util.List;
import java.util.Set;

import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;

/**
 * 应用级集群令牌服务端全量分配请求体。
 * <p>{@link #clusterMap} 为服务端机器与客户端/命名空间的映射列表；
 * {@link #remainingList} 为本次未参与分配的机器 ID 集合。</p>
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public class ClusterAppFullAssignRequest {

    /** 集群分配映射列表，每项对应一台令牌服务端机器。 */
    private List<ClusterAppAssignMap> clusterMap;
    /** 未分配机器 ID 集合。 */
    private Set<String> remainingList;

    public List<ClusterAppAssignMap> getClusterMap() {
        return clusterMap;
    }

    public ClusterAppFullAssignRequest setClusterMap(
        List<ClusterAppAssignMap> clusterMap) {
        this.clusterMap = clusterMap;
        return this;
    }

    public Set<String> getRemainingList() {
        return remainingList;
    }

    public ClusterAppFullAssignRequest setRemainingList(Set<String> remainingList) {
        this.remainingList = remainingList;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterAppFullAssignRequest{" +
            "clusterMap=" + clusterMap +
            ", remainingList=" + remainingList +
            '}';
    }
}
