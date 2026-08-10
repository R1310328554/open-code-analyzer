/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.mcp;

/**
 * MCP 后端服务引用，指向 Nacos 中已注册的实际 MCP Server 实例。
 *
 * <p>通过命名空间、分组、服务名与传输协议四元组定位后端服务，
 * 供 MCP 网关或代理将工具调用路由至正确的 Nacos 服务实例。</p>
 *
 * @author xiweng.yy
 */
public class McpServiceRef {
    
    /** Nacos 命名空间 ID。 */
    private String namespaceId;
    
    /** Nacos 服务分组名。 */
    private String groupName;
    
    /** Nacos 注册服务名。 */
    private String serviceName;
    
    /** 与 MCP Server 通信的传输协议（如 SSE、stdio 等）。 */
    private String transportProtocol;
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }
    
    public String getTransportProtocol() {
        return transportProtocol;
    }
}
