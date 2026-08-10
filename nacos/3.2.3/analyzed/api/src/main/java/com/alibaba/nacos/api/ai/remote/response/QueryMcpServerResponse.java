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

package com.alibaba.nacos.api.ai.remote.response;

import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.remote.response.Response;

/**
 * 查询 MCP Server 详情的远程响应。
 *
 * <p>继承 {@link com.alibaba.nacos.api.remote.response.Response}，
 * 通过 {@link #mcpServerDetailInfo} 返回 {@link com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo}。</p>
 *
 * @author xiweng.yy
 */
public class QueryMcpServerResponse extends Response {
    
    /** 查询到的 MCP Server 详情信息。 */
    private McpServerDetailInfo mcpServerDetailInfo;
    
    /** 获取 MCP Server 详情信息。 */
    public McpServerDetailInfo getMcpServerDetailInfo() {
        return mcpServerDetailInfo;
    }
    
    /** 设置 MCP Server 详情信息。 */
    public void setMcpServerDetailInfo(McpServerDetailInfo mcpServerDetailInfo) {
        this.mcpServerDetailInfo = mcpServerDetailInfo;
    }
}
