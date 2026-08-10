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

package com.alibaba.nacos.api.ai.remote.request;

import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpResourceSpecification;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;

/**
 * 发布新 MCP Server 或为已有 MCP Server 发布新版本的远程请求。
 *
 * <p>发布逻辑如下：</p>
 * <ul>
 *   <li>MCP Server 不存在时，按规格参数创建新 Server；</li>
 *   <li>Server 已存在但规格中版本为新版本时，创建新版本；</li>
 *   <li>Server 与版本均已存在时，不执行任何变更。</li>
 * </ul>
 *
 * @author xiweng.yy
 */
public class ReleaseMcpServerRequest extends AbstractMcpRequest {
    
    /** MCP Server 基础信息规格（名称、描述、版本等）。 */
    private McpServerBasicInfo serverSpecification;
    
    /** MCP 工具（Tool）规格定义。 */
    private McpToolSpecification toolSpecification;
    
    /** MCP 资源（Resource）规格定义。 */
    private McpResourceSpecification resourceSpecification;
    
    /** MCP 端点（Endpoint）规格定义。 */
    private McpEndpointSpec endpointSpecification;
    
    /** 获取 MCP Server 基础信息规格。 */
    public McpServerBasicInfo getServerSpecification() {
        return serverSpecification;
    }
    
    /** 设置 MCP Server 基础信息规格。 */
    public void setServerSpecification(McpServerBasicInfo serverSpecification) {
        this.serverSpecification = serverSpecification;
    }
    
    /** 获取 MCP 工具规格。 */
    public McpToolSpecification getToolSpecification() {
        return toolSpecification;
    }
    
    /** 设置 MCP 工具规格。 */
    public void setToolSpecification(McpToolSpecification toolSpecification) {
        this.toolSpecification = toolSpecification;
    }
    
    /** 获取 MCP 资源规格。 */
    public McpResourceSpecification getResourceSpecification() {
        return resourceSpecification;
    }
    
    /** 设置 MCP 资源规格。 */
    public void setResourceSpecification(McpResourceSpecification resourceSpecification) {
        this.resourceSpecification = resourceSpecification;
    }
    
    /** 获取 MCP 端点规格。 */
    public McpEndpointSpec getEndpointSpecification() {
        return endpointSpecification;
    }
    
    /** 设置 MCP 端点规格。 */
    public void setEndpointSpecification(McpEndpointSpec endpointSpecification) {
        this.endpointSpecification = endpointSpecification;
    }
}
