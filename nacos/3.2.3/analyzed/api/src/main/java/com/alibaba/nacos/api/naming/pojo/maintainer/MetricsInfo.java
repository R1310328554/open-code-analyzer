/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Nacos 命名模块运维指标快照。
 *
 * <p>统计服务数、实例数、订阅数及各类客户端连接数量，{@code null} 字段在 JSON 中省略。</p>
 *
 * @author xiweng.yy
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricsInfo implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -5064297490423743871L;
    
    /** 节点/模块运行状态描述。 */
    private String status;
    
    /** 服务总数。 */
    private Integer serviceCount;
    
    /** 实例总数。 */
    private Integer instanceCount;
    
    /** 订阅关系总数。 */
    private Integer subscribeCount;
    
    /** 客户端总数。 */
    private Integer clientCount;
    
    /** 基于长连接的客户端数量。 */
    private Integer connectionBasedClientCount;
    
    /** 临时 IP:端口 模式客户端数量。 */
    private Integer ephemeralIpPortClientCount;
    
    /** 持久 IP:端口 模式客户端数量。 */
    private Integer persistentIpPortClientCount;
    
    /** 当前节点负责的客户端数量。 */
    private Integer responsibleClientCount;
    
    /** 无参构造。 */
    public MetricsInfo() {
    }
    
    /** 获取运行状态。 */
    public String getStatus() {
        return status;
    }
    
    /** 设置运行状态。 */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /** 获取服务总数。 */
    public Integer getServiceCount() {
        return serviceCount;
    }
    
    /** 设置服务总数。 */
    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }
    
    /** 获取实例总数。 */
    public Integer getInstanceCount() {
        return instanceCount;
    }
    
    /** 设置实例总数。 */
    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }
    
    /** 获取订阅总数。 */
    public Integer getSubscribeCount() {
        return subscribeCount;
    }
    
    /** 设置订阅总数。 */
    public void setSubscribeCount(Integer subscribeCount) {
        this.subscribeCount = subscribeCount;
    }
    
    /** 获取客户端总数。 */
    public Integer getClientCount() {
        return clientCount;
    }
    
    /** 设置客户端总数。 */
    public void setClientCount(Integer clientCount) {
        this.clientCount = clientCount;
    }
    
    /** 获取长连接客户端数。 */
    public Integer getConnectionBasedClientCount() {
        return connectionBasedClientCount;
    }
    
    /** 设置长连接客户端数。 */
    public void setConnectionBasedClientCount(Integer connectionBasedClientCount) {
        this.connectionBasedClientCount = connectionBasedClientCount;
    }
    
    /** 获取临时 IP:端口 客户端数。 */
    public Integer getEphemeralIpPortClientCount() {
        return ephemeralIpPortClientCount;
    }
    
    /** 设置临时 IP:端口 客户端数。 */
    public void setEphemeralIpPortClientCount(Integer ephemeralIpPortClientCount) {
        this.ephemeralIpPortClientCount = ephemeralIpPortClientCount;
    }
    
    /** 获取持久 IP:端口 客户端数。 */
    public Integer getPersistentIpPortClientCount() {
        return persistentIpPortClientCount;
    }
    
    /** 设置持久 IP:端口 客户端数。 */
    public void setPersistentIpPortClientCount(Integer persistentIpPortClientCount) {
        this.persistentIpPortClientCount = persistentIpPortClientCount;
    }
    
    /** 获取本节点负责客户端数。 */
    public Integer getResponsibleClientCount() {
        return responsibleClientCount;
    }
    
    /** 设置本节点负责客户端数。 */
    public void setResponsibleClientCount(Integer responsibleClientCount) {
        this.responsibleClientCount = responsibleClientCount;
    }
}
