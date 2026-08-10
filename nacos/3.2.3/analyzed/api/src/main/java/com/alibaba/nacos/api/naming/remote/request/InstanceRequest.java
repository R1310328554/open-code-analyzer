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

/**
 * Nacos 命名服务实例操作远程请求。
 *
 * <p>客户端通过 gRPC 向服务端发送注册、注销或更新临时实例等操作时，携带 {@link #type} 与 {@link #instance}；命名空间、服务名与分组继承自 {@link AbstractNamingRequest}。</p>
 *
 * @author xiweng.yy
 */
public class InstanceRequest extends AbstractNamingRequest {
    
    /** 实例操作类型（如 register、deregister、beat 等）。 */
    private String type;
    
    /** 待操作的 {@link Instance} 实例详情。 */
    private Instance instance;
    
    /** 无参构造，供序列化框架使用。 */
    public InstanceRequest() {
    }
    
    /**
     * 构造带完整上下文的实例请求。
     *
     * @param namespace   命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param type        操作类型
     * @param instance    实例对象
     */
    public InstanceRequest(String namespace, String serviceName, String groupName, String type,
        Instance instance) {
        super(namespace, serviceName, groupName);
        this.type = type;
        this.instance = instance;
    }
    
    /** 设置实例操作类型。 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 返回实例操作类型。 */
    public String getType() {
        return this.type;
    }
    
    /** 设置待操作的实例对象。 */
    public void setInstance(Instance instance) {
        this.instance = instance;
    }
    
    /** 返回实例详情。 */
    public Instance getInstance() {
        return instance;
    }
}
