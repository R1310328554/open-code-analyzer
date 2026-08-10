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

package com.alibaba.nacos.naming.model.vo;

import java.io.Serializable;
import java.util.Map;

/**
 * 实例详情视图对象（旧版 HTTP API 响应）。
 *
 * <p>封装服务名、IP、端口、集群、权重、健康状态、实例 ID 与元数据，供运维查询接口返回；v2 HTTP API 移除后将废弃。</p>
 *
 * @author dongyafei
 * @date 2022/9/7
 * @deprecated will be removed after v2 http api removed.
 */
@Deprecated
public class InstanceDetailInfoVo implements Serializable {
    
    private static final long serialVersionUID = -8983967044228959560L;
    
    /** 所属服务名（不含分组前缀）。 */
    private String serviceName;
    
    /** 实例 IP 地址。 */
    private String ip;
    
    /** 实例端口号。 */
    private Integer port;
    
    /** 所属集群名称。 */
    private String clusterName;
    
    /** 负载均衡权重。 */
    private Double weight;
    
    /** 当前健康状态。 */
    private Boolean healthy;
    
    /** 实例唯一标识。 */
    private String instanceId;
    
    /** 实例元数据键值对。 */
    private Map<String, String> metadata;
    
    public InstanceDetailInfoVo() {
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getClusterName() {
        return clusterName;
    }
    
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Boolean getHealthy() {
        return healthy;
    }
    
    public void setHealthy(Boolean healthy) {
        this.healthy = healthy;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
