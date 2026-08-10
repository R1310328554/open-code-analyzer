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

package com.alibaba.nacos.api.naming.remote.request;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 客户端批量注册/更新服务实例的远程请求。
 *
 * <p>继承 {@link AbstractNamingRequest}，携带操作类型与实例列表。</p>
 *
 * @author <a href="mailto:chenhao26@xiaomi.com">chenhao26</a>
 */
public class BatchInstanceRequest extends AbstractNamingRequest {
    
    /** 批量操作类型（如注册、注销）。 */
    private String type;
    
    /** 待批量处理的 {@link Instance} 列表。 */
    private List<Instance> instances;
    
    /** 无参构造。 */
    public BatchInstanceRequest() {
    }
    
    /**
     * 全字段构造。
     *
     * @param namespace   命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param type        操作类型
     * @param instances   实例列表
     */
        super(namespace, serviceName, groupName);
        this.type = type;
        this.instances = instances;
    }
    
    /** 设置操作类型。 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 获取操作类型。 */
    public String getType() {
        return this.type;
    }
    
    /** 获取实例列表。 */
    public List<Instance> getInstances() {
        return instances;
    }
    
    /** 设置实例列表。 */
    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }
}
