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
 * Nacos 持久化实例操作远程请求。
 *
 * <p>与 {@link InstanceRequest} 类似，但针对不依赖客户端心跳的持久化实例；通过 {@link #type} 指定注册/注销等操作，{@link #instance} 携带实例详情。</p>
 *
 * @author blake.qiu
 */
public class PersistentInstanceRequest extends AbstractNamingRequest {
    
    /** 持久化实例操作类型。 */
    private String type;
    
    /** 持久化 {@link Instance} 实例详情。 */
    private Instance instance;
    
    /** 无参构造，供序列化使用。 */
    public PersistentInstanceRequest() {
    }
    
    /**
     * 构造持久化实例请求。
     *
     * @param namespace   命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param type        操作类型
     * @param instance    实例对象
     */
    public PersistentInstanceRequest(String namespace, String serviceName, String groupName,
        String type, Instance instance) {
        super(namespace, serviceName, groupName);
        this.type = type;
        this.instance = instance;
    }
    
    /** 返回操作类型。 */
    public String getType() {
        return this.type;
    }
    
    /** 设置操作类型。 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 返回持久化实例详情。 */
    public Instance getInstance() {
        return instance;
    }
    
    /** 设置持久化实例对象。 */
    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
