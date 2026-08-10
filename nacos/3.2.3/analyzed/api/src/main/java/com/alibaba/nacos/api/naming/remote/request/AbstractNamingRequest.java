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

import com.alibaba.nacos.api.remote.request.Request;

import static com.alibaba.nacos.api.common.Constants.Naming.NAMING_MODULE;

/**
 * 命名模块远程请求的抽象基类，统一携带命名空间、服务名与分组。
 *
 * @author liuzunfei
 */
public abstract class AbstractNamingRequest extends Request {
    
    /** 命名空间 ID。 */
    private String namespace;
    
    /** 服务名。 */
    private String serviceName;
    
    /** 分组名。 */
    private String groupName;
    
    /** 无参构造。 */
    public AbstractNamingRequest() {
    }
    
    /**
     * 指定命名空间、服务名与分组构造。
     *
     * @param namespace   命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     */
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.groupName = groupName;
    }
    
    /** 返回命名模块标识。 */
    @Override
    public String getModule() {
        return NAMING_MODULE;
    }
    
    /** 获取命名空间 ID。 */
    public String getNamespace() {
        return namespace;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /** 获取服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 设置服务名。 */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
