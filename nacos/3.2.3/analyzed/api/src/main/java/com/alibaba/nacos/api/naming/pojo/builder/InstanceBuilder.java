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

package com.alibaba.nacos.api.naming.pojo.builder;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@link Instance} 的流式构建器，支持链式设置各属性后一次性生成实例对象。
 *
 * <p>未显式设置的字段在 {@link #build()} 时保持 {@link Instance} 默认值。</p>
 *
 * @author xiweng.yy
 */
public class InstanceBuilder {
    
    /** 实例 ID。 */
    private String instanceId;
    
    /** 实例 IP。 */
    private String ip;
    
    /** 实例端口。 */
    private Integer port;
    
    /** 负载均衡权重。 */
    private Double weight;
    
    /** 健康状态。 */
    private Boolean healthy;
    
    /** 是否启用。 */
    private Boolean enabled;
    
    /** 是否为临时实例。 */
    private Boolean ephemeral;
    
    /** 所属集群名。 */
    private String clusterName;
    
    /** 所属服务名。 */
    private String serviceName;
    
    /** 扩展元数据。 */
    private Map<String, String> metadata = new HashMap<>();
    
    /** 私有构造，通过 {@link #newBuilder()} 创建。 */
    private InstanceBuilder() {
    }
    
    /** 设置实例 ID 并返回构建器自身。 */
    public InstanceBuilder setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }
    
    /** 设置 IP 并返回构建器自身。 */
    public InstanceBuilder setIp(String ip) {
        this.ip = ip;
        return this;
    }
    
    /** 设置端口并返回构建器自身。 */
    public InstanceBuilder setPort(Integer port) {
        this.port = port;
        return this;
    }
    
    /** 设置权重并返回构建器自身。 */
    public InstanceBuilder setWeight(Double weight) {
        this.weight = weight;
        return this;
    }
    
    /** 设置健康状态并返回构建器自身。 */
    public InstanceBuilder setHealthy(Boolean healthy) {
        this.healthy = healthy;
        return this;
    }
    
    /** 设置是否启用并返回构建器自身。 */
    public InstanceBuilder setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    /** 设置是否为临时实例并返回构建器自身。 */
    public InstanceBuilder setEphemeral(Boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }
    
    /** 设置集群名并返回构建器自身。 */
    public InstanceBuilder setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }
    
    /** 设置服务名并返回构建器自身。 */
    public InstanceBuilder setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }
    
    /** 设置元数据 Map 并返回构建器自身。 */
    public InstanceBuilder setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
    
    /** 向元数据添加键值对并返回构建器自身。 */
    public InstanceBuilder addMetadata(String metaKey, String metaValue) {
        this.metadata.put(metaKey, metaValue);
        return this;
    }
    
    /**
     * 根据已设置的属性构建新的 {@link Instance}。
     *
     * @return 新实例对象
     */
    public Instance build() {
        Instance result = new Instance();
        if (!Objects.isNull(instanceId)) {
            result.setInstanceId(instanceId);
        }
        if (!Objects.isNull(ip)) {
            result.setIp(ip);
        }
        if (!Objects.isNull(port)) {
            result.setPort(port);
        }
        if (!Objects.isNull(weight)) {
            result.setWeight(weight);
        }
        if (!Objects.isNull(healthy)) {
            result.setHealthy(healthy);
        }
        if (!Objects.isNull(enabled)) {
            result.setEnabled(enabled);
        }
        if (!Objects.isNull(ephemeral)) {
            result.setEphemeral(ephemeral);
        }
        if (!Objects.isNull(clusterName)) {
            result.setClusterName(clusterName);
        }
        if (!Objects.isNull(serviceName)) {
            result.setServiceName(serviceName);
        }
        result.setMetadata(metadata);
        return result;
    }
    
    /** 创建新的构建器实例。 */
    public static InstanceBuilder newBuilder() {
        return new InstanceBuilder();
    }
}
