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

package com.alibaba.nacos.console.handler.impl.remote.ai;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportRequest;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportResponse;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportValidationResult;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.ai.McpHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import org.springframework.stereotype.Service;

/**
 * MCP Server 远程 Handler：通过 {@link NacosMaintainerClientHolder} 调用远端 AI 运维 API 完成 CRUD；批量导入在远程模式下不可用。
 * Remote implementation of Mcp handler.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
@EnabledAiHandler
public class McpRemoteHandler implements McpHandler {
    
    /** 运维客户端持有者，提供 AI Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public McpRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 分页列出远端 MCP Server，支持精确或模糊搜索。 */
    @Override
    public Page<McpServerBasicInfo> listMcpServers(String namespaceId, String mcpName,
        String search, int pageNo,
        int pageSize) throws NacosException {
        if (Constants.MCP_LIST_SEARCH_ACCURATE.equalsIgnoreCase(search)) {
            return clientHolder.getAiMaintainerService().mcp().listMcpServer(namespaceId, mcpName,
                pageNo, pageSize);
        } else {
            return clientHolder.getAiMaintainerService().mcp().searchMcpServer(namespaceId, mcpName,
                pageNo, pageSize);
        }
    }
    
    /** 按命名空间、名称、ID 与版本获取远端 MCP Server 详情。 */
    @Override
    public McpServerDetailInfo getMcpServer(String namespaceId, String mcpName, String mcpId,
        String version)
        throws NacosException {
        return clientHolder.getAiMaintainerService().mcp().getMcpServerDetail(namespaceId, mcpName,
            mcpId, version);
    }
    
    /** 在远端创建 MCP Server，可选附带工具与端点规格。 */
    @Override
    public String createMcpServer(String namespaceId, McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification)
        throws NacosException {
        return clientHolder.getAiMaintainerService().mcp()
            .createMcpServer(namespaceId, serverSpecification.getName(), serverSpecification,
                toolSpecification,
                endpointSpecification);
    }
    
    /** 更新远端 MCP Server；可选发布为 latest 并覆盖已有实例。 */
    @Override
    public void updateMcpServer(String namespaceId, boolean isPublish,
        McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification,
        boolean overrideExisting) throws NacosException {
        clientHolder.getAiMaintainerService().mcp()
            .updateMcpServer(namespaceId, serverSpecification.getName(), isPublish,
                serverSpecification,
                toolSpecification, endpointSpecification, overrideExisting);
    }
    
    /** 删除远端指定 MCP Server 版本。 */
    @Override
    public void deleteMcpServer(String namespaceId, String mcpName, String mcpId, String version)
        throws NacosException {
        clientHolder.getAiMaintainerService().mcp().deleteMcpServer(namespaceId, mcpName, mcpId,
            version);
    }
    
    /** 远程模式不支持 MCP 批量导入，抛出 {@code SERVER_NOT_IMPLEMENTED}。 */
    @Override
    public McpServerImportValidationResult validateImport(String namespaceId,
        McpServerImportRequest request)
        throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            "MCP import functionality is not supported in remote mode");
    }
    
    /** 远程模式不支持 MCP 批量导入，抛出 {@code SERVER_NOT_IMPLEMENTED}。 */
    @Override
    public McpServerImportResponse executeImport(String namespaceId, McpServerImportRequest request)
        throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            "MCP import functionality is not supported in remote mode");
    }
}
