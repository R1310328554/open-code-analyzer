/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.model.mcp;

import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;

import java.util.List;

/**
 * Url page result.
 * <p>基于游标的 MCP 服务 URL 分页查询结果，包含当前页服务详情列表与下一页游标。</p>
 * @author xinluo
 */
public class UrlPageResult {
    
    /** 当前页的 MCP 服务详情列表。 */
    private List<McpServerDetailInfo> servers;
    
    /** 下一页游标；为空表示无更多数据。 */
    private String nextCursor;
    
    /** 构造分页结果。 */
    public UrlPageResult(List<McpServerDetailInfo> servers, String nextCursor) {
        this.servers = servers;
        this.nextCursor = nextCursor;
    }
    
    public List<McpServerDetailInfo> getServers() {
        return servers;
    }
    
    public String getNextCursor() {
        return nextCursor;
    }
    
    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
    
    public void setServers(List<McpServerDetailInfo> servers) {
        this.servers = servers;
    }
}
