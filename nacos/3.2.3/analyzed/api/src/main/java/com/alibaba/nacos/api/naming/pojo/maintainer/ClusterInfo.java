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

package com.alibaba.nacos.api.naming.pojo.maintainer;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 运维客户端使用的集群详情，包含健康检查配置与可选实例列表。
 *
 * <p>集群详情不能单独查询，须通过 {@link ServiceDetailInfo#getClusterMap()} 获取。</p>
 *
 * @author xiweng.yy
 */
public class ClusterInfo implements NacosForm {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 2146881454057032105L;
    
    /** 集群名称。 */
    private String clusterName;
    
    /** 集群级健康检查器配置。 */
    private AbstractHealthChecker healthChecker;
    
    /** 健康检查端口，默认 80。 */
    private int healthyCheckPort = 80;
    
    /** 是否使用实例端口进行健康检查（否则使用 {@link #healthyCheckPort}）。 */
    private boolean useInstancePortForCheck = true;
    
    /** 集群元数据键值对。 */
    private Map<String, String> metadata;
    
    /** 集群内实例列表（可选）。 */
    private List<Instance> hosts;
    
    /**
     * 获取集群实例列表。
     *
     * @return 实例列表
     */
    public List<Instance> getHosts() {
        return hosts;
    }
    
    /**
     * 设置集群实例列表。
     *
     * @param hosts 实例列表
     */
    public void setHosts(List<Instance> hosts) {
        this.hosts = hosts;
    }
    
    /** 获取集群名称。 */
    public String getClusterName() {
        return clusterName;
    }
    
    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
    /** 获取健康检查器。 */
    public AbstractHealthChecker getHealthChecker() {
        return healthChecker;
    }
    
    /** 设置健康检查器。 */
    public void setHealthChecker(AbstractHealthChecker healthChecker) {
        this.healthChecker = healthChecker;
    }
    
    /** 获取集群元数据。 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /** 设置集群元数据。 */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /** 获取健康检查端口。 */
    public int getHealthyCheckPort() {
        return healthyCheckPort;
    }
    
    /** 设置健康检查端口。 */
    public void setHealthyCheckPort(int healthyCheckPort) {
        this.healthyCheckPort = healthyCheckPort;
    }
    
    /** 是否使用实例端口做健康检查。 */
    public boolean isUseInstancePortForCheck() {
        return useInstancePortForCheck;
    }
    
    /** 设置是否使用实例端口做健康检查。 */
    public void setUseInstancePortForCheck(boolean useInstancePortForCheck) {
        this.useInstancePortForCheck = useInstancePortForCheck;
    }
    
    /** 校验表单：集群名为空时使用默认集群名。 */
    @Override
    public void validate() throws NacosApiException {
        if (StringUtils.isEmpty(clusterName)) {
            this.clusterName = Constants.DEFAULT_CLUSTER_NAME;
        }
    }
}
