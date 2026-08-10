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

package com.alibaba.nacos.naming.pojo;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 实例批量操作上下文描述。
 *
 * <p>封装目标服务名、一致性类型（ephemeral/persist）及待操作实例列表，供一致性服务层统一处理注册、注销或元数据变更。</p>
 *
 * @author horizonzy
 * @since 1.4.0
 */
public class InstanceOperationInfo {
    
    public InstanceOperationInfo() {
    }
    
    public InstanceOperationInfo(String serviceName, String consistencyType,
        List<? extends Instance> instances) {
        this.serviceName = serviceName;
        this.consistencyType = consistencyType;
        this.instances = instances;
    }
    
    /** 含分组前缀的完整服务名；待 v1 客户端下线后拆分为 group 与 service。 */
    private String serviceName;
    
    /** 一致性类型：ephemeral 或 persist，决定走临时还是持久化一致性服务。 */
    private String consistencyType;
    
    /** 本次批量操作涉及的实例列表。 */
    private List<? extends Instance> instances;
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getConsistencyType() {
        return consistencyType;
    }
    
    public List<? extends Instance> getInstances() {
        return instances;
    }
    
}
