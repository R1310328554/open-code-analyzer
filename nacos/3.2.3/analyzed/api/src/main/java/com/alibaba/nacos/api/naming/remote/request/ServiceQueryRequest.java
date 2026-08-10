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

/**
 * 命名服务实例查询远程请求。
 *
 * <p>客户端查询指定服务下实例列表时使用；可限定 {@link #cluster}、仅返回健康实例 {@link #healthyOnly}，以及指定 UDP 推送端口 {@link #udpPort}。</p>
 *
 * @author xiweng.yy
 */
public class ServiceQueryRequest extends AbstractNamingRequest {
    
    /** 目标集群名（可为空表示全部集群）。 */
    private String cluster;
    
    /** 是否仅返回健康实例。 */
    private boolean healthyOnly;
    
    /** 客户端 UDP 端口，供服务端推送实例变更。 */
    private int udpPort;
    
    /** 无参构造，供序列化使用。 */
    public ServiceQueryRequest() {
    }
    
    /**
     * 构造服务实例查询请求。
     *
     * @param namespace   命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     */
    public ServiceQueryRequest(String namespace, String serviceName, String groupName) {
        super(namespace, serviceName, groupName);
    }
    
    /** 返回目标集群名。 */
    public String getCluster() {
        return cluster;
    }
    
    /** 设置目标集群名。 */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    /** 是否仅查询健康实例。 */
    public boolean isHealthyOnly() {
        return healthyOnly;
    }
    
    /** 设置是否仅返回健康实例。 */
    public void setHealthyOnly(boolean healthyOnly) {
        this.healthyOnly = healthyOnly;
    }
    
    /** 返回客户端 UDP 推送端口。 */
    public int getUdpPort() {
        return udpPort;
    }
    
    /** 设置客户端 UDP 推送端口。 */
    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }
}
