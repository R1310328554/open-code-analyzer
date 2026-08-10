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

package com.alibaba.nacos.istio.api;

import com.alibaba.nacos.istio.mcp.EmptyMcpGenerator;
import com.alibaba.nacos.istio.mcp.ServiceEntryMcpGenerator;
import com.alibaba.nacos.istio.xds.CdsGenerator;
import com.alibaba.nacos.istio.xds.EdsGenerator;
import com.alibaba.nacos.istio.xds.EmptyXdsGenerator;
import com.alibaba.nacos.istio.xds.LdsGenerator;
import com.alibaba.nacos.istio.xds.RdsGenerator;
import com.alibaba.nacos.istio.xds.ServiceEntryXdsGenerator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.istio.api.ApiConstants.*;

/**
 * Istio API 生成器工厂，按 typeUrl 路由到 XDS/MCP 各类 {@link com.alibaba.nacos.istio.api.ApiGenerator} 实现。
 *
 * <p>启动时注册 ServiceEntry、Cluster、Endpoint、Listener、Route 等资源的生成器；未匹配类型时回退到 {@link com.alibaba.nacos.istio.mcp.EmptyMcpGenerator} 或 {@link com.alibaba.nacos.istio.xds.EmptyXdsGenerator}。</p>
 *
 * @author special.fy
 */
@Component
public class ApiGeneratorFactory {
    
    /** typeUrl 到 {@link ApiGenerator} 实例的映射表。 */
    private final Map<String, ApiGenerator<?>> apiGeneratorMap;
    
    /** 构造时注册 MCP over XDS、XDS 各类型及 MCP ServiceEntry 生成器。 */
    public ApiGeneratorFactory() {
        apiGeneratorMap = new HashMap<>(2);
        // MCP over XDS：ServiceEntry 走 XDS 通道
        apiGeneratorMap.put(SERVICE_ENTRY_PROTO_PACKAGE, ServiceEntryXdsGenerator.getInstance());
        // TODO 支持更多 API 生成器
        
        // XDS 资源类型：Cluster / Endpoint / Listener / Route
        apiGeneratorMap.put(CLUSTER_TYPE, CdsGenerator.getInstance());
        apiGeneratorMap.put(ENDPOINT_TYPE, EdsGenerator.getInstance());
        apiGeneratorMap.put(LISTENER_TYPE, LdsGenerator.getInstance());
        apiGeneratorMap.put(ROUTE_TYPE, RdsGenerator.getInstance());
        
        // MCP 资源集合：ServiceEntry
        apiGeneratorMap.put(SERVICE_ENTRY_COLLECTION, ServiceEntryMcpGenerator.getInstance());
    }
    
    /**
     * 按 typeUrl 获取对应 API 生成器；未知类型按 MCP/XDS 前缀返回空生成器。
     *
     * @param typeUrl Envoy/Istio 资源 type URL
     * @return 匹配的生成器或空实现
     */
    public ApiGenerator<?> getApiGenerator(String typeUrl) {
        ApiGenerator<?> apiGenerator = apiGeneratorMap.get(typeUrl);
        return apiGenerator != null ? apiGenerator : (typeUrl.startsWith(MCP_PREFIX)
            ? EmptyMcpGenerator.getInstance() : EmptyXdsGenerator.getInstance());
    }
}
