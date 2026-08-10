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
 * 服务订阅者信息，描述订阅某服务的客户端端点与应用标识。
 *
 * @author xiweng.yy
 */
public class SubscriberInfo implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -3803634488440573042L;
    
    /** 命名空间 ID。 */
    private String namespaceId;
    
    /** 分组名。 */
    private String groupName;
    
    /** 被订阅的服务名。 */
    private String serviceName;
    
    /** 订阅者 IP。 */
    private String ip;
    
    /** 订阅者端口。 */
    private int port;
    
    /** 客户端 SDK/代理标识。 */
    private String agent;
    
    /** 应用名称。 */
    private String appName;
    
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
    
    /** 获取订阅者 IP。 */
    public String getIp() {
        return ip;
    }
    
    /** 设置订阅者 IP。 */
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    /** 获取订阅者端口。 */
    public int getPort() {
        return port;
    }
    
    /** 设置订阅者端口。 */
    public void setPort(int port) {
        this.port = port;
    }
    
    /** 获取客户端代理标识。 */
    public String getAgent() {
        return agent;
    }
    
    /** 设置客户端代理标识。 */
    public void setAgent(String agent) {
        this.agent = agent;
    }
    
    /** 获取应用名。 */
    public String getAppName() {
        return appName;
    }
    
    /** 设置应用名。 */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /** 获取 {@code ip:port} 格式的订阅者地址。 */
    public String getAddress() {
        return ip + ":" + port;
    }
}
