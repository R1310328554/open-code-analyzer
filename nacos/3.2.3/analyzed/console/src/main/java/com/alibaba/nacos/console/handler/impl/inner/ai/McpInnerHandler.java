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

package com.alibaba.nacos.console.handler.impl.inner.ai;

import com.alibaba.nacos.ai.service.McpLegacyImportAdapter;
import com.alibaba.nacos.ai.service.McpServerOperationService;
import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportRequest;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportResponse;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportValidationResult;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.ai.McpHandler;
import com.alibaba.nacos.console.handler.impl.inner.EnabledInnerHandler;
import org.springframework.stereotype.Service;

/**
 * MCP Server 控制台内嵌 Handler：分页查询、CRUD 委托 {@link McpServerOperationService}，旧版导入校验/执行委托 {@link McpLegacyImportAdapter}。
 * Inner implementation of Mcp handler.
 *
 * @author xiweng.yy
 */
@Service
@EnabledInnerHandler
@EnabledAiHandler
public class McpInnerHandler implements McpHandler {
    
    /** MCP Server 运维服务，负责标准 CRUD 与分页查询 */
    private final McpServerOperationService mcpServerOperationService;
    
    /** 旧版 MCP 批量导入适配器，处理 validateImport / executeImport */
    private final McpLegacyImportAdapter mcpLegacyImportAdapter;
    
    /** 注入 MCP 运维服务与旧版导入适配器 */
    public McpInnerHandler(McpServerOperationService mcpServerOperationService,
        McpLegacyImportAdapter mcpLegacyImportAdapter) {
        this.mcpServerOperationService = mcpServerOperationService;
        this.mcpLegacyImportAdapter = mcpLegacyImportAdapter;
    }
    
    /** 分页列出 MCP Server，支持名称模糊/精确搜索 */
    @Override
    public Page<McpServerBasicInfo> listMcpServers(String namespaceId, String mcpName,
        String search, int pageNo,
        int pageSize) {
        return mcpServerOperationService.listMcpServerWithPage(namespaceId, mcpName, search, pageNo,
            pageSize);
    }
    
    /** 按命名空间、名称、ID 与版本获取 MCP Server 详情 */
    @Override
    public McpServerDetailInfo getMcpServer(String namespaceId, String mcpName, String mcpServerId,
        String version) throws NacosException {
        return mcpServerOperationService.getMcpServerDetail(namespaceId, mcpServerId, mcpName,
            version);
    }
    
    /** 创建 MCP Server，可选附带工具与端点规格 */
    @Override
    public String createMcpServer(String namespaceId, McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification)
        throws NacosException {
        return mcpServerOperationService.createMcpServer(namespaceId, serverSpecification,
            toolSpecification,
            endpointSpecification);
    }
    
    /** 更新 MCP Server；可选发布为 latest 并覆盖已有实例 */
    @Override
    public void updateMcpServer(String namespaceId, boolean isPublish,
        McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification,
        boolean overrideExisting) throws NacosException {
        mcpServerOperationService.updateMcpServer(namespaceId, isPublish, serverSpecification,
            toolSpecification,
            endpointSpecification, overrideExisting);
    }
    
    /** 删除指定 MCP Server 版本 */
    @Override
    public void deleteMcpServer(String namespaceId, String mcpName, String mcpServerId,
        String version) throws NacosException {
        mcpServerOperationService.deleteMcpServer(namespaceId, mcpName, mcpServerId, version);
    }
    
    /** 校验 MCP Server 批量导入请求 */
    @Override
    public McpServerImportValidationResult validateImport(String namespaceId,
        McpServerImportRequest request) throws NacosException {
        return mcpLegacyImportAdapter.validateImport(namespaceId, request);
    }
    
    /** 执行 MCP Server 批量导入 */
    @Override
    public McpServerImportResponse executeImport(String namespaceId, McpServerImportRequest request)
        throws NacosException {
        return mcpLegacyImportAdapter.executeImport(namespaceId, request);
    }
}
