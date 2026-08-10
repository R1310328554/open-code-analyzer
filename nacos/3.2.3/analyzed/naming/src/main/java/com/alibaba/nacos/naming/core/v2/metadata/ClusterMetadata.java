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

import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Tcp;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 服务集群元数据模型。
 *
 * <p>描述集群级健康检查配置及扩展属性，随 {@link ServiceMetadata} 持久化。</p>
 *
 * @author xiweng.yy
 */
public class ClusterMetadata implements Serializable {
    
    private static final long serialVersionUID = -80030989533083615L;
    
    /** 健康检查端口，默认 80。 */
    private int healthyCheckPort = 80;
    
    /** 健康检查类型，默认 TCP。 */
    private String healthyCheckType = Tcp.TYPE;
    
    /** 健康检查器实例，默认 TCP 检查器。 */
    private AbstractHealthChecker healthChecker = new Tcp();
    
    /** 是否使用实例端口进行健康检查。 */
    private boolean useInstancePortForCheck = true;
    
    /** 集群扩展键值属性。 */
    private Map<String, String> extendData = new ConcurrentHashMap<>(1);
    
    public int getHealthyCheckPort() {
        return healthyCheckPort;
    }
    
    public void setHealthyCheckPort(int healthyCheckPort) {
        this.healthyCheckPort = healthyCheckPort;
    }
    
    public String getHealthyCheckType() {
        return healthyCheckType;
    }
    
    public void setHealthyCheckType(String healthyCheckType) {
        this.healthyCheckType = healthyCheckType;
    }
    
    public AbstractHealthChecker getHealthChecker() {
        return healthChecker;
    }
    
    public void setHealthChecker(AbstractHealthChecker healthChecker) {
        this.healthChecker = healthChecker;
    }
    
    public boolean isUseInstancePortForCheck() {
        return useInstancePortForCheck;
    }
    
    public void setUseInstancePortForCheck(boolean useInstancePortForCheck) {
        this.useInstancePortForCheck = useInstancePortForCheck;
    }
    
    public Map<String, String> getExtendData() {
        return extendData;
    }
    
    public void setExtendData(Map<String, String> extendData) {
        this.extendData = extendData;
    }
}
