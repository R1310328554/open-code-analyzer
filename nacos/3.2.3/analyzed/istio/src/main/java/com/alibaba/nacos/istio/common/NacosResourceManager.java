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

package com.alibaba.nacos.istio.common;

import com.alibaba.nacos.istio.misc.IstioConfig;
import com.alibaba.nacos.istio.model.IstioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Nacos Istio 资源管理器：维护当前 {@link ResourceSnapshot}，并从 {@link NacosServiceInfoResourceWatcher} 聚合服务视图。
 *
 * <p>为 XDS/MCP 推送提供一致的资源快照与 {@link IstioConfig} 访问入口。</p>
 *
 * @author special.fy
 */
@Component
public class NacosResourceManager {
    
    /** 当前生效的 Istio 资源快照。 */
    private ResourceSnapshot resourceSnapshot;
    
    @Autowired
    NacosServiceInfoResourceWatcher serviceInfoResourceWatcher;
    
    @Autowired
    private IstioConfig istioConfig;
    
    public NacosResourceManager() {
        resourceSnapshot = new ResourceSnapshot(istioConfig);
    }
    
    /** 返回服务名到 {@link IstioService} 的快照副本。 */
    public Map<String, IstioService> services() {
        return serviceInfoResourceWatcher.snapshot();
    }
    
    public IstioConfig getIstioConfig() {
        return istioConfig;
    }
    
    /** 线程安全地获取当前资源快照。 */
    public synchronized ResourceSnapshot getResourceSnapshot() {
        return resourceSnapshot;
    }
    
    /** 线程安全地更新资源快照。 */
    public synchronized void setResourceSnapshot(ResourceSnapshot resourceSnapshot) {
        this.resourceSnapshot = resourceSnapshot;
    }
    
    /** 用当前 Nacos 服务数据初始化已有快照（不新建实例）。 */
    public void initResourceSnapshot() {
        ResourceSnapshot resourceSnapshot = getResourceSnapshot();
        resourceSnapshot.initResourceSnapshot(this);
    }
    
    /** 创建新快照、填充服务数据并设为当前快照后返回。 */
    public ResourceSnapshot createResourceSnapshot() {
        ResourceSnapshot resourceSnapshot = new ResourceSnapshot(istioConfig);
        resourceSnapshot.initResourceSnapshot(this);
        setResourceSnapshot(resourceSnapshot);
        return resourceSnapshot;
    }
}
