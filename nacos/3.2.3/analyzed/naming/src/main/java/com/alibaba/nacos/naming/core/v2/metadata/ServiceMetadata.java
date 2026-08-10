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

package com.alibaba.nacos.naming.core.v2.metadata;

import com.alibaba.nacos.api.selector.Selector;
import com.alibaba.nacos.naming.selector.NoneSelector;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos 命名 V2 服务级元数据模型。
 *
 * <p>包含 ephemeral 标志、保护阈值、负载均衡选择器、扩展字段与集群元数据映射。</p>
 *
 * @author xiweng.yy
 */
public class ServiceMetadata implements Serializable {
    
    private static final long serialVersionUID = -6605609934135069566L;
    
    /** 服务是否为临时（ephemeral）类型；持久服务由 Raft 管理。 */
    /**
     * Service is ephemeral or persistence.
      * <p>Nacos 命名 V2 元数据、POJO、客户端操作与健康检查；详见上方类/接口说明。</p>
     */
    private boolean ephemeral = true;
    
    /** 健康实例比例保护阈值，低于该比例时触发保护策略。 */
    /**
     * protect threshold.
      * <p>Nacos 命名 V2 元数据、POJO、客户端操作与健康检查；详见上方类/接口说明。</p>
     */
    private float protectThreshold = 0.0F;
    
    /** 服务实例选择器类型，默认 {@link NoneSelector}。 */
    /**
     * Type of {@link Selector}.
      * <p>Nacos 命名 V2 元数据、POJO、客户端操作与健康检查；详见上方类/接口说明。</p>
     */
    private Selector selector = new NoneSelector();
    
    /** 服务级扩展键值对（并发安全）。 */
    private Map<String, String> extendData = new ConcurrentHashMap<>(1);
    
    /** 集群名 → {@link ClusterMetadata} 映射。 */
    private Map<String, ClusterMetadata> clusters = new ConcurrentHashMap<>(1);
    
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
    
    public float getProtectThreshold() {
        return protectThreshold;
    }
    
    public void setProtectThreshold(float protectThreshold) {
        this.protectThreshold = protectThreshold;
    }
    
    public Selector getSelector() {
        return selector;
    }
    
    public void setSelector(Selector selector) {
        this.selector = selector;
    }
    
    public Map<String, String> getExtendData() {
        return extendData;
    }
    
    public void setExtendData(Map<String, String> extendData) {
        this.extendData = extendData;
    }
    
    public Map<String, ClusterMetadata> getClusters() {
        return clusters;
    }
    
    public void setClusters(Map<String, ClusterMetadata> clusters) {
        this.clusters = clusters;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceMetadata)) {
            return false;
        }
        ServiceMetadata metadata = (ServiceMetadata) o;
        return Float.compare(metadata.protectThreshold, protectThreshold) == 0
            && selector == metadata.selector
            && Objects.equals(extendData, metadata.extendData)
            && Objects.equals(clusters, metadata.clusters);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(protectThreshold, selector, extendData, clusters);
    }
}
