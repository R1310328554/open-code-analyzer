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
 * 命名客户端发布者（注册实例）信息。
 *
 * <p>运维 API 用于描述某客户端向 Nacos 注册实例时的连接与集群归属。</p>
 *
 * @author xiweng.yy
 */
public class ClientPublisherInfo implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -4858433977035198914L;
    
    /** 客户端唯一标识。 */
    private String clientId;
    
    /** 客户端 IP 地址。 */
    private String ip;
    
    /** 客户端端口。 */
    private int port;
    
    /** 实例所属集群名称。 */
    private String clusterName;
    
    /** 获取客户端 ID。 */
    public String getClientId() {
        return clientId;
    }
    
    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    /** 获取客户端 IP。 */
    public String getIp() {
        return ip;
    }
    
    /** 设置客户端 IP。 */
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    /** 获取客户端端口。 */
    public int getPort() {
        return port;
    }
    
    /** 设置客户端端口。 */
    public void setPort(int port) {
        this.port = port;
    }
    
    /** 获取集群名称。 */
    public String getClusterName() {
        return clusterName;
    }
    
    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
