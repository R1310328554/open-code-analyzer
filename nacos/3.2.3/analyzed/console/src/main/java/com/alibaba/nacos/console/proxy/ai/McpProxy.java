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

package com.alibaba.nacos.console.proxy.ai;

import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportRequest;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportResponse;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportValidationResult;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.ai.McpHandler;
import org.springframework.stereotype.Service;

/**
 * MCP Server 代理：将 MCP 服务 CRUD 与批量导入委托给 {@link McpHandler}。
 * Proxy class for handling AI MCP operations.
 *
 * @author xiweng.yy
 */
@Service
public class McpProxy {
    
    /** MCP Handler 实现 */
    private final McpHandler mcpHandler;
    
    /** 注入 MCP Handler。 */
    public McpProxy(McpHandler mcpHandler) {
        this.mcpHandler = mcpHandler;
    }
    
    /**
     * 分页列出 MCP Server。
     * List mcp server.
     *
     * @param namespaceId MCP Server 所在命名空间 ID
     * @param mcpName     MCP 名称模式，为空则匹配全部
     * @param search      搜索类型 {@code blur}（模糊）或 {@code accurate}（精确）
     * @param pageNo      页码，从 1 开始
     * @param pageSize    每页条数
     * @return 匹配的 {@link McpServerBasicInfo} 分页结果
     */
    public Page<McpServerBasicInfo> listMcpServers(String namespaceId, String mcpName,
        String search, int pageNo,
        int pageSize) throws NacosException {
        return mcpHandler.listMcpServers(namespaceId, mcpName, search, pageNo, pageSize);
    }
    
    /**
     * 获取指定 MCP Server 详情。
     * Get specified mcp server detail info.
     *
     * @param namespaceId MCP Server 命名空间 ID
     * @param mcpName     MCP Server 名称
     * @return {@link McpServerDetailInfo} 详情
     * @throws NacosException 处理过程中任意异常
     */
    public McpServerDetailInfo getMcpServer(String namespaceId, String mcpName, String mcpId,
        String version) throws NacosException {
        return mcpHandler.getMcpServer(namespaceId, mcpName, mcpId, version);
    }
    
    /**
     * 创建新 MCP Server。
     * Create new mcp server.
     *
     * @param namespaceId           MCP Server 命名空间 ID
     * @param serverSpecification   服务规格，见 {@link McpServerBasicInfo}
     * @param toolSpecification     工具规格，见 {@link McpToolSpecification}，可选
     * @param endpointSpecification 端点规格，见 {@link McpEndpointSpec}，可选
     * @return 新建 MCP Server 的 ID
     * @throws NacosException 处理过程中任意异常
     */
    public String createMcpServer(String namespaceId, McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification)
        throws NacosException {
        return mcpHandler.createMcpServer(namespaceId, serverSpecification, toolSpecification,
            endpointSpecification);
    }
    
    /**
     * 更新已有 MCP Server。
     * Update existed mcp server.
     *
     * <p>
     * {@code namespaceId} 与 {@code mcpName} 不可变更。
     * </p>
     *
     * @param namespaceId           MCP Server 命名空间 ID，标识待更新目标
     * @param isPublish             是否发布，否则仅保存
     * @param serverSpecification   服务规格，见 {@link McpServerBasicInfo}
     * @param toolSpecification     工具规格，见 {@link McpToolSpecification}，可选
     * @param endpointSpecification 端点规格，见 {@link McpEndpointSpec}，可选
     * @param overrideExisting      更新时是否全量替换实例
     * @throws NacosException 处理过程中任意异常
     */
    public void updateMcpServer(String namespaceId, boolean isPublish,
        McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification,
        boolean overrideExisting) throws NacosException {
        mcpHandler.updateMcpServer(namespaceId, isPublish, serverSpecification, toolSpecification,
            endpointSpecification, overrideExisting);
    }
    
    /**
     * 删除已有 MCP Server。
     * Delete existed mcp server.
     *
     * @param namespaceId  MCP Server 命名空间 ID
     * @param mcpServerId  MCP Server ID
     * @param version      MCP Server 版本
     * @throws NacosException 处理过程中任意异常
     */
    public void deleteMcpServer(String namespaceId, String mcpName, String mcpServerId,
        String version) throws NacosException {
        mcpHandler.deleteMcpServer(namespaceId, mcpName, mcpServerId, version);
    }
    
    /**
     * 校验 MCP Server 导入请求。
     * Validate MCP server import request.
     *
     * @param namespaceId MCP Server 命名空间 ID
     * @param request     含数据与设置的导入请求
     * @return 校验结果及潜在问题详情
     * @throws NacosException 校验过程异常
     */
    public McpServerImportValidationResult validateImport(String namespaceId,
        McpServerImportRequest request) throws NacosException {
        return mcpHandler.validateImport(namespaceId, request);
    }
    
    /**
     * 执行 MCP Server 导入。
     * Execute MCP server import operation.
     *
     * @param namespaceId MCP Server 命名空间 ID
     * @param request     含数据与设置的导入请求
     * @return 导入结果与统计信息
     * @throws NacosException 导入执行异常
     */
    public McpServerImportResponse executeImport(String namespaceId, McpServerImportRequest request)
        throws NacosException {
        return mcpHandler.executeImport(namespaceId, request);
    }
}
