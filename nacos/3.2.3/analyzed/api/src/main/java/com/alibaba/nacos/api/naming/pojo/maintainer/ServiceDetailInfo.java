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

import com.alibaba.nacos.api.selector.Selector;

import java.io.Serializable;
import java.util.Map;

/**
 * 运维客户端使用的服务详情，包含元数据、保护阈值、选择器及集群映射。
 *
 * @author xiweng.yy
 */
public class ServiceDetailInfo implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 6351606608785841722L;
    
    /** 命名空间 ID。 */
    private String namespaceId;
    
    /** 服务名。 */
    private String serviceName;
    
    /** 分组名。 */
    private String groupName;
    
    /** 集群名到 {@link ClusterInfo} 的映射。 */
    private Map<String, ClusterInfo> clusterMap;
    
    /** 服务级元数据。 */
    private Map<String, String> metadata;
    
    /** 健康实例保护阈值（0~1）。 */
    private float protectThreshold;
    
    /** 服务实例选择器。 */
    private Selector selector;
    
    /** 是否为临时服务。 */
    private Boolean ephemeral;
    
    /**
     * 获取服务名。
     *
     * @return 服务名
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * 设置服务名。
     *
     * @param serviceName 服务名
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /**
     * 获取集群映射。
     *
     * @return 集群名到详情的映射
     */
    public Map<String, ClusterInfo> getClusterMap() {
        return clusterMap;
    }
    
    /**
     * 设置集群映射。
     *
     * @param clusterMap 集群映射
     */
    public void setClusterMap(Map<String, ClusterInfo> clusterMap) {
        this.clusterMap = clusterMap;
    }
    
    /**
     * 获取服务元数据。
     *
     * @return 元数据映射
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * 设置服务元数据。
     *
     * @param metadata 元数据映射
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /** 获取命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** 获取保护阈值。 */
    public float getProtectThreshold() {
        return protectThreshold;
    }
    
    /** 设置保护阈值。 */
    public void setProtectThreshold(float protectThreshold) {
        this.protectThreshold = protectThreshold;
    }
    
    /** 获取选择器。 */
    public Selector getSelector() {
        return selector;
    }
    
    /** 设置选择器。 */
    public void setSelector(Selector selector) {
        this.selector = selector;
    }
    
    /** 是否为临时服务。 */
    public Boolean isEphemeral() {
        return ephemeral;
    }
    
    /** 设置是否临时服务。 */
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
}
