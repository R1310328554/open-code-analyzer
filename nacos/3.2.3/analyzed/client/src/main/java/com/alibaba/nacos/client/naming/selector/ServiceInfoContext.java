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

package com.alibaba.nacos.client.naming.selector;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.selector.NamingContext;

import java.util.List;

/**
 * 基于 {@link ServiceInfo} 的命名上下文。
 *
 * <p>将本地缓存的服务信息适配为 {@link NamingContext}，供选择器直接读取服务名、分组、集群与实例列表。</p>
 *
 * @author xiweng.yy
 */
public class ServiceInfoContext implements NamingContext {
    
    /** 底层服务信息快照。 */
    private final ServiceInfo serviceInfo;
    
    /** 使用指定 {@link ServiceInfo} 构造命名上下文。 */
    public ServiceInfoContext(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    @Override
    public String getServiceName() {
        return serviceInfo.getName();
    }
    
    @Override
    public String getGroupName() {
        return serviceInfo.getGroupName();
    }
    
    @Override
    public String getClusters() {
        return serviceInfo.getClusters();
    }
    
    @Override
    public List<Instance> getInstances() {
        return serviceInfo.getHosts();
    }
}
