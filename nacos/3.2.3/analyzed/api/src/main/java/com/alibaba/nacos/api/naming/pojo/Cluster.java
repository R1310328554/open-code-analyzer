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

package com.alibaba.nacos.api.naming.pojo;

import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Tcp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务集群模型，描述同一服务下实例的逻辑分组及其健康检查配置。
 *
 * <p>本类会序列化为 JSON，部分字段与方法命名未采用驼峰规则以保持历史兼容。</p>
 *
 * @author nkorange
 */
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class Cluster implements Serializable {
    
    private static final long serialVersionUID = -7196138840047197271L;
    
    /**
     * 所属服务名。
     */
    private String serviceName;
    
    /**
     * 集群名称。
     */
    private String name;
    
    /**
     * 本集群的健康检查配置，默认使用 TCP 探测。
     */
    private AbstractHealthChecker healthChecker = new Tcp();
    
    /**
     * 本集群实例的默认注册端口。
     */
    private int defaultPort = 80;
    
    /**
     * 本集群实例的默认健康检查端口。
     */
    private int defaultCheckPort = 80;
    
    /**
     * 是否使用实例 IP 与端口进行健康检查。
     */
    private boolean useIpPort4Check = true;
    
    /** 集群扩展元数据键值对。 */
    private Map<String, String> metadata = new HashMap<>();
    
    /** 无参构造。 */
    public Cluster() {
        
    }
    
    /**
     * 按集群名构造集群对象。
     *
     * @param clusterName 集群名称
     */
    public Cluster(String clusterName) {
        this.name = clusterName;
    }
    
    /** 获取所属服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 设置所属服务名。 */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 获取集群名称。 */
    public String getName() {
        return name;
    }
    
    /** 设置集群名称。 */
    public void setName(String name) {
        this.name = name;
    }
    
    /** 获取健康检查配置。 */
    public AbstractHealthChecker getHealthChecker() {
        return healthChecker;
    }
    
    /** 设置健康检查配置。 */
    public void setHealthChecker(AbstractHealthChecker healthChecker) {
        this.healthChecker = healthChecker;
    }
    
    /** 获取默认注册端口。 */
    public int getDefaultPort() {
        return defaultPort;
    }
    
    /** 设置默认注册端口。 */
    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }
    
    /** 获取默认健康检查端口。 */
    public int getDefaultCheckPort() {
        return defaultCheckPort;
    }
    
    /** 设置默认健康检查端口。 */
    public void setDefaultCheckPort(int defaultCheckPort) {
        this.defaultCheckPort = defaultCheckPort;
    }
    
    /** 是否使用实例 IP 与端口进行健康检查。 */
    public boolean isUseIpPort4Check() {
        return useIpPort4Check;
    }
    
    /** 设置是否使用实例 IP 与端口进行健康检查。 */
    public void setUseIpPort4Check(boolean useIpPort4Check) {
        this.useIpPort4Check = useIpPort4Check;
    }
    
    /** 获取集群元数据。 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /** 设置集群元数据。 */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
