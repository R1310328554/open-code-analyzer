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

package com.alibaba.nacos.api.naming.listener;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 服务实例变更事件，订阅者在服务实例列表发生变化时收到通知。
 *
 * <p>封装服务名、分组、集群及最新 {@link Instance} 列表，实现 {@link Event} 供命名服务监听器回调。</p>
 *
 * @author nkorange
 */
public class NamingEvent implements Event {
    
    /** 发生变更的服务名。 */
    private String serviceName;
    
    /** 服务所属分组名。 */
    private String groupName;
    
    /** 订阅的集群名列表（逗号分隔或聚合字符串）。 */
    private String clusters;
    
    /** 变更后的服务实例列表。 */
    private List<Instance> instances;
    
    /**
     * 构造仅含服务名与实例列表的命名事件。
     *
     * @param serviceName 服务名
     * @param instances   实例列表
     */
    public NamingEvent(String serviceName, List<Instance> instances) {
        this.serviceName = serviceName;
        this.instances = instances;
    }
    
    /**
     * 构造包含分组与集群信息的命名事件。
     *
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param clusters    集群名
     * @param instances   实例列表
     */
    public NamingEvent(String serviceName, String groupName, String clusters,
        List<Instance> instances) {
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.clusters = clusters;
        this.instances = instances;
    }
    
    /** 获取服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 设置服务名。 */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 获取变更后的实例列表。 */
    public List<Instance> getInstances() {
        return instances;
    }
    
    /** 设置实例列表。 */
    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /** 获取集群名。 */
    public String getClusters() {
        return clusters;
    }
    
    /** 设置集群名。 */
    public void setClusters(String clusters) {
        this.clusters = clusters;
    }
}
