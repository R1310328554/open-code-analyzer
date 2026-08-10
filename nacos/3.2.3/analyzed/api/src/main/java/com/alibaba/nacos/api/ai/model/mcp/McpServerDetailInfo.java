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

import com.alibaba.nacos.api.ai.model.mcp.registry.ServerVersionDetail;

import java.util.List;

/**
 * MCP Server 完整详情模型，在 {@link McpServerBasicInfo} 基础上扩展端点与规范信息。
 *
 * <p>包含前后端端点、工具规范、资源规范及全部历史版本列表，
 * 用于详情查询、导入预览与治理控制台展示。</p>
 *
 * @author xiweng.yy
 */
public class McpServerDetailInfo extends McpServerBasicInfo {
    
    /** 后端端点列表。 */
    private List<McpEndpointInfo> backendEndpoints;
    
    /** 前端端点列表。 */
    private List<McpEndpointInfo> frontendEndpoints;
    
    /** 工具规范（Tools）。 */
    private McpToolSpecification toolSpec;
    
    /** 资源规范（Resources）。 */
    private McpResourceSpecification resourceSpec;
    
    /** 全部历史版本明细列表。 */
    private List<ServerVersionDetail> allVersions;
    
    public List<McpEndpointInfo> getBackendEndpoints() {
        return backendEndpoints;
    }
    
    public void setBackendEndpoints(List<McpEndpointInfo> backendEndpoints) {
        this.backendEndpoints = backendEndpoints;
    }
    
    public List<McpEndpointInfo> getFrontendEndpoints() {
        return frontendEndpoints;
    }
    
    public void setFrontendEndpoints(List<McpEndpointInfo> frontendEndpoints) {
        this.frontendEndpoints = frontendEndpoints;
    }
    
    public McpToolSpecification getToolSpec() {
        return toolSpec;
    }
    
    public void setToolSpec(McpToolSpecification toolSpec) {
        this.toolSpec = toolSpec;
    }
    
    public McpResourceSpecification getResourceSpec() {
        return resourceSpec;
    }
    
    public void setResourceSpec(McpResourceSpecification resourceSpec) {
        this.resourceSpec = resourceSpec;
    }
    
    public List<ServerVersionDetail> getAllVersions() {
        return allVersions;
    }
    
    public void setAllVersions(List<ServerVersionDetail> allVersions) {
        this.allVersions = allVersions;
    }
}
