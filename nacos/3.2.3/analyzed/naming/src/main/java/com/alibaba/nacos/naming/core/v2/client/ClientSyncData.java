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

package com.alibaba.nacos.naming.core.v2.client;

import com.alibaba.nacos.naming.core.v2.pojo.BatchInstanceData;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;

import java.io.Serializable;
import java.util.List;

/**
 * 客户端 Distro 同步数据载体。
 *
 * <p>序列化客户端 ID、发布的服务实例列表、批量注册数据及扩展属性，供集群节点间一致性同步使用。</p>
 *
 * @author xiweng.yy
 */
public class ClientSyncData implements Serializable {
    
    private static final long serialVersionUID = -5141768777704539562L;
    
    /** 客户端唯一标识。 */
    private String clientId;
    
    /** 客户端扩展属性（含修订号等）。 */
    private ClientAttributes attributes;
    
    /** 已发布服务的命名空间列表（与实例列表一一对应）。 */
    private List<String> namespaces;
    
    /** 已发布服务的分组名列表。 */
    private List<String> groupNames;
    
    /** 已发布服务的服务名列表。 */
    private List<String> serviceNames;
    
    /** 单实例注册模式的发布信息列表。 */
    private List<InstancePublishInfo> instancePublishInfos;
    
    /** 批量注册模式的实例数据。 */
    private BatchInstanceData batchInstanceData;
    
    /** 无参构造，供反序列化使用。 */
    public ClientSyncData() {
    }
    
    /** 构造包含发布实例与批量数据的同步快照。 */
    public ClientSyncData(String clientId, List<String> namespaces, List<String> groupNames,
        List<String> serviceNames,
        List<InstancePublishInfo> instancePublishInfos,
        BatchInstanceData batchInstanceData) {
        this.clientId = clientId;
        this.namespaces = namespaces;
        this.groupNames = groupNames;
        this.serviceNames = serviceNames;
        this.instancePublishInfos = instancePublishInfos;
        this.batchInstanceData = batchInstanceData;
        this.attributes = new ClientAttributes();
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public List<String> getNamespaces() {
        return namespaces;
    }
    
    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }
    
    public List<String> getGroupNames() {
        return groupNames;
    }
    
    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }
    
    public List<String> getServiceNames() {
        return serviceNames;
    }
    
    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }
    
    public List<InstancePublishInfo> getInstancePublishInfos() {
        return instancePublishInfos;
    }
    
    public void setInstancePublishInfos(List<InstancePublishInfo> instancePublishInfos) {
        this.instancePublishInfos = instancePublishInfos;
    }
    
    public ClientAttributes getAttributes() {
        return attributes;
    }
    
    public void setAttributes(ClientAttributes attributes) {
        this.attributes = attributes;
    }
    
    public BatchInstanceData getBatchInstanceData() {
        return batchInstanceData;
    }
    
    public void setBatchInstanceData(BatchInstanceData batchInstanceData) {
        this.batchInstanceData = batchInstanceData;
    }
}
