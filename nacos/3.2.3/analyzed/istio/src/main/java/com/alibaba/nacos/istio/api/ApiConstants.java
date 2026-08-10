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

/**
 * Istio/MCP/xDS API 类型 URL 与资源集合名常量定义。
 * @author special.fy
 */
public class ApiConstants {
    
    /** Google protobuf Any 类型 URL 默认前缀。 */

    public static final String API_TYPE_PREFIX = "type.googleapis.com/";
    
    /** MCP over xDS 使用的 Istio CRD proto 包路径（待扩展 Gateway/VS/DR 等）。 */

    public static final String SERVICE_ENTRY_PROTO_PACKAGE =
        "networking.istio.io/v1alpha3/ServiceEntry";
    public static final String MESH_CONFIG_PROTO_PACKAGE = "core/v1alpha1/MeshConfig";
    
    /** MCP 资源集合命名前缀与 ServiceEntry 集合标识。 */

    public static final String MCP_PREFIX = "istio/";
    public static final String SERVICE_ENTRY_COLLECTION =
        MCP_PREFIX + "networking/v1alpha3/serviceentries";
    
    /** MCP Resource 与 ServiceEntry 的完整 type URL。 */

    public static final String MCP_RESOURCE_PROTO = API_TYPE_PREFIX + "istio.mcp.v1alpha1.Resource";
    public static final String SERVICE_ENTRY_PROTO =
        API_TYPE_PREFIX + "istio.networking.v1alpha3.ServiceEntry";
    
    /** 标准 xDS 资源 type URL（Cluster/Endpoint/Listener/Route，LDS/RDS/SDS 待完善）。 */

    public static final String CLUSTER_TYPE = API_TYPE_PREFIX + "envoy.config.cluster.v3.Cluster";
    public static final String ENDPOINT_TYPE =
        API_TYPE_PREFIX + "envoy.config.endpoint.v3.ClusterLoadAssignment";
    
    public static final String LISTENER_TYPE =
        API_TYPE_PREFIX + "envoy.config.listener.v3.Listener";
    
    public static final String ROUTE_TYPE =
        API_TYPE_PREFIX + "envoy.config.route.v3.RouteConfiguration";
    
}
