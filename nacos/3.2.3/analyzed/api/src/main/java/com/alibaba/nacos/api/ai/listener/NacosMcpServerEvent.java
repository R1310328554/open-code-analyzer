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

package com.alibaba.nacos.api.ai.listener;

import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;

/**
 * Nacos AI 模块 MCP 服务器变更事件。
 *
 * <p>由 {@link McpServerDetailInfo} 构造，暴露 MCP ID、命名空间、名称及完整详情，
 * 便于客户端同步 MCP 工具端点与元数据。</p>
 *
 * @author xiweng.yy
 */
public class NacosMcpServerEvent implements NacosAiEvent {
    
    private final String mcpId;
    
    private final String namespaceId;
    
    private final String mcpName;
    
    private final McpServerDetailInfo mcpServerDetailInfo;
    
    /**
     * 由 MCP 服务器详情构造事件，并提取 id、namespaceId 与 name。
     *
     * @param mcpServerDetailInfo MCP 服务器详情
     */
        this.mcpServerDetailInfo = mcpServerDetailInfo;
        this.mcpId = mcpServerDetailInfo.getId();
        this.namespaceId = mcpServerDetailInfo.getNamespaceId();
        this.mcpName = mcpServerDetailInfo.getName();
    }
    
    /** 返回 MCP 服务器唯一 ID。 */
    public String getMcpId() {
        return mcpId;
    }
    
    /** 返回 MCP 服务器所在命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** 返回 MCP 服务器名称。 */
    public String getMcpName() {
        return mcpName;
    }
    
    /** 返回 MCP 服务器完整详情。 */
    public McpServerDetailInfo getMcpServerDetailInfo() {
        return mcpServerDetailInfo;
    }
}
