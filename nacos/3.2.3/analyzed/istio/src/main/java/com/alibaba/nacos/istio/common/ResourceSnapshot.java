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
import com.alibaba.nacos.istio.model.IstioResources;
import com.alibaba.nacos.istio.model.IstioService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Istio 资源快照：在某时刻冻结 Nacos 服务列表与版本号，供单次 XDS/MCP 推送使用。
 *
 * <p>版本格式为 ISO 时间戳加自增后缀，保证每次推送可区分。</p>
 *
 * @author special.fy
 */
public class ResourceSnapshot {
    
    /** 版本号后缀自增序列。 */
    private static AtomicLong versionSuffix = new AtomicLong(0);
    
    /** 快照内的 Istio 资源集合（主要为服务映射）。 */
    private final IstioResources istioResources;
    
    private IstioConfig istioConfig;
    
    /** 快照是否已完成初始化。 */
    private boolean isCompleted;
    
    /** 快照版本字符串，写入 XDS/MCP systemVersionInfo。 */
    private String version;
    
    public ResourceSnapshot(IstioConfig istioConfig) {
        isCompleted = false;
        istioResources = new IstioResources(new ConcurrentHashMap<String, IstioService>(16));
        this.istioConfig = istioConfig;
    }
    
    /** 从 ResourceManager 拉取服务数据并生成版本号（仅首次有效）。 */
    public synchronized void initResourceSnapshot(NacosResourceManager manager) {
        if (isCompleted) {
            return;
        }
        
        initIstioResources(manager);
        
        generateVersion();
        
        isCompleted = true;
    }
    
    /** 生成带时间戳与递增后缀的快照版本。 */
    private void generateVersion() {
        String time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
        version = time + "/" + versionSuffix.getAndIncrement();
    }
    
    /** 将 ResourceManager 中的服务映射写入 IstioResources。 */
    private void initIstioResources(NacosResourceManager manager) {
        istioResources.setIstioServiceMap(manager.services());
    }
    
    public IstioResources getIstioResources() {
        return istioResources;
    }
    
    public IstioConfig getIstioConfig() {
        return istioConfig;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public String getVersion() {
        return version;
    }
}
