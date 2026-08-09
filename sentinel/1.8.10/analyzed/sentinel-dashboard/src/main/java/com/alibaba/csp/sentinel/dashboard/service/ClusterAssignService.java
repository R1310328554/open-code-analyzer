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
package com.alibaba.csp.sentinel.dashboard.service;

import java.util.List;
import java.util.Set;

import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterAppAssignResultVO;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;

/**
 * 集群令牌服务端/客户端分配服务接口，支持绑定、解绑与批量应用分配方案。
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public interface ClusterAssignService {

    /**
     * 解绑指定集群令牌服务端及其关联客户端。
     *
     * @param app 应用名
     * @param machineId 有效机器标识（{@code host@commandPort}）
     * @return 分配操作结果
     */
    ClusterAppAssignResultVO unbindClusterServer(String app, String machineId);

    /**
     * 批量解绑多台集群令牌服务端及其关联客户端。
     *
     * @param app 应用名
     * @param machineIdSet 机器标识集合（{@code host@commandPort}）
     * @return 分配操作结果
     */
    ClusterAppAssignResultVO unbindClusterServers(String app, Set<String> machineIdSet);

    /**
     * 对指定应用应用完整的集群服务端/客户端分配方案。
     *
     * @param app 应用名
     * @param clusterMap 集群分配映射（服务端 → 客户端集合）
     * @param remainingSet 未分配机器标识集合
     * @return 分配操作结果
     */
    ClusterAppAssignResultVO applyAssignToApp(String app, List<ClusterAppAssignMap> clusterMap,
                                              Set<String> remainingSet);
}
