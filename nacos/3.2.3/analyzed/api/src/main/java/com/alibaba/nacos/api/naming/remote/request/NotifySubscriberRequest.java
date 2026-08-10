/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.remote.request.ServerRequest;

import static com.alibaba.nacos.api.common.Constants.Naming.NAMING_MODULE;

/**
 * 通知订阅者服务实例变更请求（服务端 → 客户端）。
 *
 * <p>当服务实例列表发生变化时，Nacos 服务端向已订阅的客户端推送此 {@link ServerRequest}，携带最新 {@link ServiceInfo} 快照。</p>
 *
 * @author xiweng.yy
 */
public class NotifySubscriberRequest extends ServerRequest {
    
    /** 命名空间 ID。 */
    private String namespace;
    
    /** 服务名。 */
    private String serviceName;
    
    /** 分组名。 */
    private String groupName;
    
    /** 变更后的服务实例快照。 */
    private ServiceInfo serviceInfo;
    
    /** 无参构造，供反序列化使用。 */
    public NotifySubscriberRequest() {
    }
    
    /** 返回命名模块标识。 */
    @Override
    public String getModule() {
        return NAMING_MODULE;
    }
    
    /** 私有构造，通过 {@link #buildNotifySubscriberRequest} 创建。 */
    private NotifySubscriberRequest(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    /**
     * 根据最新服务快照构建订阅通知请求。
     *
     * @param serviceInfo 服务实例信息
     * @return 通知请求实例
     */
    public static NotifySubscriberRequest buildNotifySubscriberRequest(ServiceInfo serviceInfo) {
        return new NotifySubscriberRequest(serviceInfo);
    }
    
    /** 返回服务实例快照。 */
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
    
    /** 设置服务实例快照。 */
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    /** 返回命名空间 ID。 */
    public String getNamespace() {
        return namespace;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /** 返回服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 设置服务名。 */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 返回分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
}
