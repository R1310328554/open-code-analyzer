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

package com.alibaba.nacos.istio.model;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Nacos 命名服务到 Istio ServiceEntry 的中间表示。
 *
 * <p>聚合健康实例为 {@link IstioEndpoint} 列表，并保留创建时间戳以避免 Istio 误触发全量拉取。</p>
 *
 * @author special.fy
 */
public class IstioService {
    
    /** 服务名。 */
    private final String name;
    
    /** Nacos 分组名。 */
    private final String groupName;
    
    /** 命名空间。 */
    private final String namespace;
    
    /** Nacos 服务修订号，写入 MCP metadata version。 */
    private final Long revision;
    
    /** 代表性端口，取首个有效端点。 */
    private int port = 0;
    
    /** 代表性协议（http/grpc）。 */
    private String protocol;
    
    /** 过滤后的实例端点列表。 */
    private final List<IstioEndpoint> hosts;
    
    /** 服务创建时间戳，用于 Istio 推送去抖（参见 istio/istio#30684）。 */
    private final Date createTimeStamp;
    
    /**
     * 新建服务：记录当前时间为 createTimeStamp。
     *
     * @param service     Nacos v2 服务元数据
     * @param serviceInfo 含实例列表的服务信息
     */
    public IstioService(Service service, ServiceInfo serviceInfo) {
        this.name = serviceInfo.getName();
        this.groupName = serviceInfo.getGroupName();
        this.namespace = service.getNamespace();
        this.revision = service.getRevision();
        // 记录创建时间，避免 Istio 因时间戳变化误触发 pull/push（见 istio/istio#30684）
        createTimeStamp = new Date();
        
        this.hosts = sanitizeServiceInfo(serviceInfo);
    }

    /**
     * 更新服务：复用旧服务的 createTimeStamp，避免推送风暴。
     *
     * @param service     Nacos v2 服务元数据
     * @param serviceInfo 含实例列表的服务信息
     * @param old         更新前的 IstioService
     */
    public IstioService(Service service, ServiceInfo serviceInfo, IstioService old) {
        this.name = serviceInfo.getName();
        this.groupName = serviceInfo.getGroupName();
        this.namespace = service.getNamespace();
        this.revision = service.getRevision();
        // 继承旧创建时间，避免 Istio 因时间戳变化误触发 pull/push
        createTimeStamp = old.getCreateTimeStamp();
        
        this.hosts = sanitizeServiceInfo(serviceInfo);
    }

    /** 过滤健康且启用的实例；若无可用实例则 panic 模式推送全部。 */
    private List<IstioEndpoint> sanitizeServiceInfo(ServiceInfo serviceInfo) {
        List<IstioEndpoint> hosts = new ArrayList<>();

        for (Instance instance : serviceInfo.getHosts()) {
            if (instance.isHealthy() && instance.isEnabled()) {
                IstioEndpoint istioEndpoint = new IstioEndpoint(instance);
                if (port == 0) {
                    port = istioEndpoint.getPort();
                    protocol = istioEndpoint.getProtocol();
                }
                hosts.add(istioEndpoint);
            }
        }

        // Panic 模式：全部实例不健康时仍推送，避免 Istio 侧服务完全消失
        if (hosts.isEmpty()) {
            for (Instance instance : serviceInfo.getHosts()) {
                IstioEndpoint istioEndpoint = new IstioEndpoint(instance);
                hosts.add(istioEndpoint);
            }
        }

        return hosts;
    }
    
    public String getNamespace() {
        return namespace;
    }

    public Long getRevision() {
        return revision;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public List<IstioEndpoint> getHosts() {
        return hosts;
    }

    public Date getCreateTimeStamp() {
        return createTimeStamp;
    }
}