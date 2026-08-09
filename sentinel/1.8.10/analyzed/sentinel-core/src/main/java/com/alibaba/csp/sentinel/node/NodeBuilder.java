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
package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;

/**
 * 构建新的 {@link DefaultNode} 与 {@link ClusterNode}。
 *
 * @author qinan.qn
 */
@Deprecated
public interface NodeBuilder {

    /**
     * 创建新的 {@link DefaultNode} 作为调用树节点。
     *
     * @param id 资源
     * @param clusterNode 该资源对应的集群节点
     * @return 新创建的树节点
     */
    DefaultNode buildTreeNode(ResourceWrapper id, ClusterNode clusterNode);

    /**
     * 为单个资源创建新的 {@link ClusterNode} 作为全局统计节点。
     *
     * @return 新创建的集群节点
     */
    ClusterNode buildClusterNode();
}
