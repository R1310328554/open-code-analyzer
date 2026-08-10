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

package com.alibaba.nacos.ai.index;

import com.alibaba.nacos.ai.model.mcp.McpServerIndexData;
import com.alibaba.nacos.api.model.Page;

/**
 * Server info index interface. We should know the relation between the mcp server id and namespaceId + mcpServerName.
 * <p>MCP 服务索引接口，维护 MCP ID 与「命名空间 + 服务名」之间的映射关系，支持分页搜索与按 ID/名称查询。</p>
 *
 * @author xinluo
 */
public interface McpServerIndex {
    
    /**
     * Search Mcp server by name and namespaceId with pagination.
     * <p>在指定命名空间内按服务名分页搜索 MCP 索引数据。</p>
     *
     * @param namespaceId namespace ID
     * @param name        mcp server name
     * @param search      search mode
     * @param pageNo      page number
     * @param limit       page size limit
     * @return MCP Server Index Data page
     */
    Page<McpServerIndexData> searchMcpServerByNameWithPage(String namespaceId, String name,
        String search, int pageNo,
        int limit);
    
    /**
     * Get mcp server by id.
     * <p>按 MCP 服务 ID 查询索引数据。</p>
     *
     * @param id mcp server id
     * @return {@link McpServerIndexData}
     */
    McpServerIndexData getMcpServerById(String id);
    
    /**
     * Get mcp server by namespaceId and servername.
     * <p>按命名空间与服务名查询索引数据。</p>
     *
     * @param namespaceId namespaceId
     * @param name        servername
     * @return {@link McpServerIndexData}
     */
    McpServerIndexData getMcpServerByName(String namespaceId, String name);
    
    /**
     * Remove cache entry by namespace ID and MCP server name.
     * <p>删除指定命名空间下某服务名的缓存条目（无缓存实现可为 no-op）。</p>
     *
     * @param namespaceId namespace ID
     * @param mcpName     MCP server name
     */
    void removeMcpServerByName(String namespaceId, String mcpName);
    
    /**
     * Remove cache entry by MCP server ID.
     * <p>按 MCP ID 删除对应缓存条目。</p>
     *
     * @param mcpId MCP server ID
     */
    void removeMcpServerById(String mcpId);
}
