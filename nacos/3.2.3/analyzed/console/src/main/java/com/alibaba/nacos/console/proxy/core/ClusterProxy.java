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

package com.alibaba.nacos.console.proxy.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.NacosMember;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.handler.core.ClusterHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 集群管理代理：委托 {@link ClusterHandler} 获取节点列表，并按 IP 关键字本地过滤与排序。
 * Proxy class for handling cluster-related operations.
 *
 * @author zhangyukun
 */
@Service
public class ClusterProxy {
    
    /** 集群 Handler 实现 */
    private final ClusterHandler clusterHandler;
    
    /**
     * 注入集群 Handler。
     * Constructs a new ClusterProxy with the given ClusterInnerHandler and ConsoleConfig.
     *
     * @param clusterHandler ClusterHandler 默认实现
     */
    public ClusterProxy(ClusterHandler clusterHandler) {
        this.clusterHandler = clusterHandler;
    }
    
    /**
     * 获取集群成员列表，可按 IP 关键字前缀过滤并按地址排序。
     * Retrieve a list of cluster members with an optional search keyword.
     *
     * @param ipKeyWord 成员 IP 搜索关键字
     * @return 匹配的成员集合
     * @throws IllegalArgumentException 部署类型无效时抛出
     */
    public Collection<NacosMember> getNodeList(String ipKeyWord) throws NacosException {
        Collection<? extends NacosMember> members = clusterHandler.getNodeList(ipKeyWord);
        List<NacosMember> result = new ArrayList<>();
        members.forEach(member -> {
            if (StringUtils.isBlank(ipKeyWord)) {
                result.add(member);
                return;
            }
            final String address = member.getAddress();
            if (StringUtils.equals(address, ipKeyWord)
                || StringUtils.startsWith(address, ipKeyWord)) {
                result.add(member);
            }
        });
        result.sort(Comparator.comparing(NacosMember::getAddress));
        return result;
    }
}
