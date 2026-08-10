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

package com.alibaba.nacos.istio.xds;

import com.alibaba.nacos.istio.api.ApiGenerator;
import com.alibaba.nacos.istio.misc.IstioConfig;
import com.alibaba.nacos.istio.model.IstioService;
import com.alibaba.nacos.istio.model.PushRequest;
import com.alibaba.nacos.istio.model.ServiceEntryWrapper;
import com.google.protobuf.Any;
import istio.mcp.v1alpha1.MetadataOuterClass.Metadata;
import istio.mcp.v1alpha1.ResourceOuterClass.Resource;
import istio.networking.v1alpha3.ServiceEntryOuterClass;
import istio.networking.v1alpha3.ServiceEntryOuterClass.ServiceEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.alibaba.nacos.istio.api.ApiConstants.*;
import static com.alibaba.nacos.istio.util.IstioCrdUtil.buildServiceEntry;
import static com.alibaba.nacos.istio.util.IstioCrdUtil.parseServiceEntryNameToServiceName;

/**
 * XDS 通道下的 ServiceEntry 生成器：将 Nacos 服务注册信息转换为 Istio ServiceEntry MCP 资源。
 *
 * <p>全量模式封装为 MCP Resource；增量模式直接输出 XDS {@link io.envoyproxy.envoy.service.discovery.v3.Resource}。</p>
 *
 * @author special.fy
 */
public final class ServiceEntryXdsGenerator implements ApiGenerator<Any> {
    
    /** 单例实例。 */
    private static volatile ServiceEntryXdsGenerator singleton = null;
    
    /** 本次生成周期内缓存的 ServiceEntry 包装列表。 */
    private List<ServiceEntryWrapper> serviceEntries;
    
    /** 获取 ServiceEntry XDS 生成器单例。 */
    public static ServiceEntryXdsGenerator getInstance() {
        if (singleton == null) {
            synchronized (ServiceEntryXdsGenerator.class) {
                if (singleton == null) {
                    singleton = new ServiceEntryXdsGenerator();
                }
            }
        }
        return singleton;
    }
    
    /** 全量生成 MCP 格式的 ServiceEntry 资源列表。 */
    @Override
    public List<Any> generate(PushRequest pushRequest) {
        List<Resource> resources = new ArrayList<>();
        serviceEntries = new ArrayList<>(16);
        IstioConfig istioConfig = pushRequest.getResourceSnapshot().getIstioConfig();
        Map<String, IstioService> serviceInfoMap =
            pushRequest.getResourceSnapshot().getIstioResources().getIstioServiceMap();
        
        for (Map.Entry<String, IstioService> entry : serviceInfoMap.entrySet()) {
            String serviceName = entry.getKey();
            
            ServiceEntryWrapper serviceEntryWrapper = buildServiceEntry(serviceName,
                serviceName + istioConfig.getDomainSuffix(), serviceInfoMap.get(serviceName));
            if (serviceEntryWrapper != null) {
                serviceEntries.add(serviceEntryWrapper);
            }
        }
        for (ServiceEntryWrapper serviceEntryWrapper : serviceEntries) {
            Metadata metadata = serviceEntryWrapper.getMetadata();
            ServiceEntry serviceEntry = serviceEntryWrapper.getServiceEntry();
            
            Any any = Any.newBuilder().setValue(serviceEntry.toByteString())
                .setTypeUrl(SERVICE_ENTRY_PROTO).build();
            
            resources.add(Resource.newBuilder().setBody(any).setMetadata(metadata).build());
        }
        
        List<Any> result = new ArrayList<>();
        for (Resource resource : resources) {
            result.add(Any.newBuilder().setValue(resource.toByteString())
                .setTypeUrl(MCP_RESOURCE_PROTO).build());
        }
        
        return result;
    }
    
    /** 增量生成 ServiceEntry；全量模式返回 {@code null}。 */
    @Override
    public List<io.envoyproxy.envoy.service.discovery.v3.Resource> deltaGenerate(
        PushRequest pushRequest) {
        if (pushRequest.isFull()) {
            return null;
        }
        
        List<io.envoyproxy.envoy.service.discovery.v3.Resource> result = new ArrayList<>();
        serviceEntries = new ArrayList<>();
        Set<String> reason = pushRequest.getReason();
        IstioConfig istioConfig = pushRequest.getResourceSnapshot().getIstioConfig();
        Map<String, IstioService> istioServiceMap =
            pushRequest.getResourceSnapshot().getIstioResources().getIstioServiceMap();
        
        if (pushRequest.getSubscribe().size() != 0) {
            for (String subscribe : pushRequest.getSubscribe()) {
                String serviceName = parseServiceEntryNameToServiceName(subscribe,
                    istioConfig.getDomainSuffix());
                if (reason.contains(serviceName)) {
                    if (istioServiceMap.containsKey(serviceName)) {
                        ServiceEntryWrapper serviceEntryWrapper = buildServiceEntry(serviceName,
                            subscribe, istioServiceMap.get(serviceName));
                        if (serviceEntryWrapper != null) {
                            serviceEntries.add(serviceEntryWrapper);
                        } else {
                            pushRequest.addRemoved(subscribe);
                        }
                    } else {
                        pushRequest.addRemoved(subscribe);
                    }
                }
            }
        } else {
            for (Map.Entry<String, IstioService> entry : istioServiceMap.entrySet()) {
                String hostName = entry.getKey() + "." + istioConfig.getDomainSuffix();
                ServiceEntryWrapper serviceEntryWrapper =
                    buildServiceEntry(entry.getKey(), hostName, entry.getValue());
                if (serviceEntryWrapper != null) {
                    serviceEntries.add(serviceEntryWrapper);
                } else {
                    pushRequest.addRemoved(hostName);
                }
            }
        }
        
        for (ServiceEntryWrapper serviceEntryWrapper : serviceEntries) {
            ServiceEntryOuterClass.ServiceEntry serviceEntry =
                serviceEntryWrapper.getServiceEntry();
            
            Any any = Any.newBuilder().setValue(serviceEntry.toByteString())
                .setTypeUrl(SERVICE_ENTRY_PROTO).build();
            
            result.add(io.envoyproxy.envoy.service.discovery.v3.Resource.newBuilder()
                .setResource(any).setVersion(
                    pushRequest.getResourceSnapshot().getVersion())
                .build());
        }
        
        return result;
    }
}
