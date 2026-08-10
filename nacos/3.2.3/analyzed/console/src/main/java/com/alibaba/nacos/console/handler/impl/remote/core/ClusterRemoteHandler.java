/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.handler.impl.remote.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.NacosMember;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.handler.core.ClusterHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 集群管理远程 Handler：通过 {@link NacosMaintainerClientHolder} 查询远端 Nacos 集群成员节点列表。
 * Remote Implementation of ClusterHandler that handles cluster-related operations.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
public class ClusterRemoteHandler implements ClusterHandler {
    
    /** 运维客户端持有者，提供 Naming Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public ClusterRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /**
     * 获取远端集群成员列表（ipKeyWord 参数保留供接口兼容，当前未用于过滤）。
     * Retrieves a list of cluster members with an optional search keyword.
     *
     * @param ipKeyWord 成员 IP 搜索关键字
     * @return 匹配的集群成员集合
     */
    @Override
    public Collection<? extends NacosMember> getNodeList(String ipKeyWord) throws NacosException {
        return clientHolder.getNamingMaintainerService().listClusterNodes(StringUtils.EMPTY,
            StringUtils.EMPTY);
    }
}
