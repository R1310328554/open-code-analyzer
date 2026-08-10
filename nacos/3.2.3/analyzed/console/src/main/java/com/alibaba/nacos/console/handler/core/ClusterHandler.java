/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.console.handler.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.NacosMember;

import java.util.Collection;

/**
 * 集群管理控制台处理器接口：列举集群成员节点并支持 IP 关键字过滤。
 * Interface for handling cluster-related operations.
 *
 * @author zhangyukun
 */
public interface ClusterHandler {
    
    /**
     * 获取集群成员列表，可按 IP 关键字过滤。
     * Retrieve a list of cluster members with an optional search keyword.
     *
     * @param ipKeyWord 成员 IP 搜索关键字
     * @return 匹配的集群成员集合
     * @throws NacosException 操作失败时抛出
     */
    Collection<? extends NacosMember> getNodeList(String ipKeyWord) throws NacosException;
}
