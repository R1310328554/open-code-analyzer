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

import java.util.List;

/**
 * MCP Server 远程服务配置，关联 Nacos 注册服务与前端暴露端点。
 *
 * <p>用于非 STDIO 协议场景，通过 {@link McpServiceRef} 引用后端实例，
 * 并由 {@link FrontEndpointConfig} 列表定义对外访问入口。</p>
 *
 * @author xiweng.yy
 */
public class McpServerRemoteServiceConfig {
    
    /** 引用的 Nacos 服务实例。 */
    private McpServiceRef serviceRef;
    
    /** MCP 协议导出路径。 */
    private String exportPath;
    
    /** 前端端点配置列表。 */
    private List<FrontEndpointConfig> frontEndpointConfigList;
    
    public McpServiceRef getServiceRef() {
        return serviceRef;
    }
    
    public void setServiceRef(McpServiceRef serviceRef) {
        this.serviceRef = serviceRef;
    }
    
    public String getExportPath() {
        return exportPath;
    }
    
    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
    }
    
    public List<FrontEndpointConfig> getFrontEndpointConfigList() {
        return frontEndpointConfigList;
    }
    
    public void setFrontEndpointConfigList(List<FrontEndpointConfig> frontEndpointConfigList) {
        this.frontEndpointConfigList = frontEndpointConfigList;
    }
}
