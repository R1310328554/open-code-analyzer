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

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 发布 MCP Server 的远程响应。
 *
 * <p>继承 {@link com.alibaba.nacos.api.remote.response.Response}，
 * 通过 {@link #mcpId} 返回已创建或更新的 MCP Server 唯一标识。</p>
 *
 * @author xiweng.yy
 */
public class ReleaseMcpServerResponse extends Response {
    
    /** 已发布 MCP Server 的唯一标识 ID。 */
    private String mcpId;
    
    /** 获取 MCP Server 唯一标识。 */
    public String getMcpId() {
        return mcpId;
    }
    
    /** 设置 MCP Server 唯一标识。 */
    public void setMcpId(String mcpId) {
        this.mcpId = mcpId;
    }
}
