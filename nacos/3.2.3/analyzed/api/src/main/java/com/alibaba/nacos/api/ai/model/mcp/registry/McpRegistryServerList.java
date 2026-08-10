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

package com.alibaba.nacos.api.ai.model.mcp.registry;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * MCP Registry 服务端列表响应（由原 ServerList 重命名）。
 *
 * <p>包含分页 Server 条目及 nextCursor 等元数据，
 * 与 Registry 包 list servers API 响应对齐。</p>
 *
 * @author xinluo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpRegistryServerList {
    
    /** 当前页的 Server 摘要列表。 */
    private List<ServerResponse> servers;
    
    /** 分页元数据（游标、总数等）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Metadata metadata;
    
    public List<ServerResponse> getServers() {
        return servers;
    }
    
    public void setServers(List<ServerResponse> servers) {
        this.servers = servers;
    }
    
    public Metadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Metadata {
        
        /** 下一页游标，无更多数据时为 null。 */
        @JsonProperty("nextCursor")
        @JsonAlias("next_cursor")
        private String nextCursor;
        
        /** 当前页或总计数量（依 API 语义）。 */
        private Integer count;
        
        public Metadata() {
        }
        
        public Metadata(String nextCursor, Integer count) {
            this.nextCursor = nextCursor;
            this.count = count;
        }
        
        public String getNextCursor() {
            return nextCursor;
        }
        
        public void setNextCursor(String nextCursor) {
            this.nextCursor = nextCursor;
        }
        
        public Integer getCount() {
            return count;
        }
        
        public void setCount(Integer count) {
            this.count = count;
        }
    }
}
