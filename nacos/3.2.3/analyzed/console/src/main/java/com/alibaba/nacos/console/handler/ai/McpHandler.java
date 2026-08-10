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

package com.alibaba.nacos.console.handler.ai;

import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportRequest;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportResponse;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportValidationResult;
import com.alibaba.nacos.api.ai.model.mcp.McpTool;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;

/**
 * MCP（Model Context Protocol）控制台处理器接口：MCP Server 的 CRUD、导入校验与执行。
 * Actual Handler class for handling AI MCP operations.
 *
 * @author xiweng.yy
 */
public interface McpHandler {
    
    /**
     * 分页列出 MCP Server，支持模糊或精确名称搜索。
     * List mcp server.
     *
     * @param namespaceId MCP Server 所在命名空间 ID
     * @param mcpName     MCP 名称模式，为空则不过滤名称
     * @param search      搜索类型：`blur` 模糊匹配或 `accurate` 精确匹配
     * @param pageNo      页码，从 1 开始
     * @param pageSize    每页条数
     * @return 匹配条件的 {@link McpServerBasicInfo} 分页列表
     * @throws NacosException 处理过程中的任意异常
     */
    Page<McpServerBasicInfo> listMcpServers(String namespaceId, String mcpName, String search,
        int pageNo, int pageSize)
        throws NacosException;
    
    /**
     * 获取指定 MCP Server 的完整详情。
     * Get specified mcp server detail info.
     *
     * @param namespaceId MCP Server 命名空间 ID
     * @param mcpName     MCP Server 名称
     * @param mcpId       MCP Server ID
     * @param version     MCP Server 版本号
     * @return {@link McpServerDetailInfo} 详情
     * @throws NacosException 处理过程中的任意异常
     */
    McpServerDetailInfo getMcpServer(String namespaceId, String mcpName, String mcpId,
        String version) throws NacosException;
    
    /**
     * 创建新的 MCP Server，可选附带工具与端点规格。
     * Create new mcp server.
     *
     * @param namespaceId           MCP Server 命名空间 ID
     * @param serverSpecification   服务规格，见 {@link McpServerBasicInfo}
     * @param toolSpecification     工具规格，见 {@link McpTool}，可选
     * @param endpointSpecification 端点规格，见 {@link McpEndpointSpec}，可选
     * @return 新创建的 MCP Server ID
     * @throws NacosException 处理过程中的任意异常
     */
    String createMcpServer(String namespaceId, McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification)
        throws NacosException;
    
    /**
     * 更新已有 MCP Server；命名空间 ID 与 MCP 名称不可变更。
     * Update existed mcp server.
     *
     * <p>
     * `namespaceId` 与 `mcpName` 不可修改。
     * </p>
     *
     * @param namespaceId           namespace id of mcp server, used to mark which mcp server to update
     * @param isPublish             是否将当前版本发布为 latest
     * @param serverSpecification   mcp server specification, see {@link McpServerBasicInfo}
     * @param toolSpecification     mcp server included tools, see {@link McpTool}, optional
     * @param endpointSpecification mcp server endpoint specification, see {@link McpEndpointSpec}, optional
     * @param overrideExisting      更新时是否替换全部实例
     * @throws NacosException any exception during handling
     */
    void updateMcpServer(String namespaceId, boolean isPublish,
        McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification, McpEndpointSpec endpointSpecification,
        boolean overrideExisting) throws NacosException;
    
    /**
     * 删除指定 MCP Server 版本。
     * Delete existed mcp server.
     *
     * @param namespaceId MCP Server 命名空间 ID
     * @param mcpId       MCP Server ID
     * @param version     MCP Server 版本号
     * @param mcpName     MCP Server 名称
     * @throws NacosException 处理过程中的任意异常
     */
    void deleteMcpServer(String namespaceId, String mcpName, String mcpId, String version)
        throws NacosException;
    
    /**
     * 校验 MCP Server 批量导入请求。
     * Validate MCP server import request.
     *
     * @param namespaceId 目标命名空间 ID
     * @param request     含数据与设置的导入请求
     * @return 含潜在问题详情的校验结果
     * @throws NacosException 校验过程异常
     */
    McpServerImportValidationResult validateImport(String namespaceId,
        McpServerImportRequest request) throws NacosException;
    
    /**
     * 执行 MCP Server 导入操作。
     * Execute MCP server import operation.
     *
     * @param namespaceId 目标命名空间 ID
     * @param request     含数据与设置的导入请求
     * @return 含结果与统计信息的导入响应
     * @throws NacosException 导入执行异常
     */
    McpServerImportResponse executeImport(String namespaceId, McpServerImportRequest request)
        throws NacosException;
}
