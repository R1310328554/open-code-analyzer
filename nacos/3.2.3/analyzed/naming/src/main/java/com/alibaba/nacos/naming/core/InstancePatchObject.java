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

package com.alibaba.nacos.naming.core;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 实例 PATCH 更新载荷，记录本次请求需变更的字段子集。
 *
 * <p>供 {@link com.alibaba.nacos.legacy.adapter.naming.InstanceController#patch(HttpServletRequest)} 与 {@link InstanceOperator#patchInstance} 使用；未设置的字段保持原值。</p>
 *
 * @author xiweng.yy
 */
public class InstancePatchObject {
    
    /** 目标实例所属集群名（不可变）。 */
    private final String cluster;
    
    /** 目标实例 IP（不可变）。 */
    private final String ip;
    
    /** 目标实例端口（不可变）。 */
    private final int port;
    
    /** 待替换的元数据映射（可选）。 */
    private Map<String, String> metadata;
    
    /** 待更新权重（可选）。 */
    private Double weight;
    
    /** 待更新健康状态（可选）。 */
    private Boolean healthy;
    
    /** 待更新启用状态（可选）。 */
    private Boolean enabled;
    
    public InstancePatchObject(String cluster, String ip, int port) {
        this.cluster = cluster;
        this.ip = ip;
        this.port = port;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public String getIp() {
        return ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
