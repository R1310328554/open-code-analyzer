/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.pojo.maintainer;

import java.io.Serializable;

/**
 * 命名客户端与服务关联信息。
 *
 * <p>汇总某客户端在指定命名空间/分组/服务下的发布与订阅角色。</p>
 *
 * @author xiweng.yy
 */
public class ClientServiceInfo implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 7400821120040393395L;
    
    /** 命名空间 ID。 */
    private String namespaceId;
    
    /** 服务分组名。 */
    private String groupName;
    
    /** 服务名。 */
    private String serviceName;
    
    /** 该客户端作为发布者时的信息（可为 {@code null}）。 */
    private ClientPublisherInfo publisherInfo;
    
    /** 该客户端作为订阅者时的信息（可为 {@code null}）。 */
    private ClientSubscriberInfo subscriberInfo;
    
    /** 获取命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /** 获取服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 设置服务名。 */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 获取发布者信息。 */
    public ClientPublisherInfo getPublisherInfo() {
        return publisherInfo;
    }
    
    /** 设置发布者信息。 */
    public void setPublisherInfo(ClientPublisherInfo publisherInfo) {
        this.publisherInfo = publisherInfo;
    }
    
    /** 获取订阅者信息。 */
    public ClientSubscriberInfo getSubscriberInfo() {
        return subscriberInfo;
    }
    
    /** 设置订阅者信息。 */
    public void setSubscriberInfo(ClientSubscriberInfo subscriberInfo) {
        this.subscriberInfo = subscriberInfo;
    }
}
