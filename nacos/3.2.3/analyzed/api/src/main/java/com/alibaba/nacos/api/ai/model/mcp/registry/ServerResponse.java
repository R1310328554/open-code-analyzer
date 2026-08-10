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

package com.alibaba.nacos.api.ai.model.mcp.registry;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP Registry 单条 Server 查询响应包装。
 *
 * <p>包含 {@link McpRegistryServerDetail} 主体及 {@code _meta} 扩展元数据，
 * 用于 Registry API 返回单个 Server 条目。</p>
 *
 * @author xinluo
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerResponse {
    
    /** MCP Server 详情主体。 */
    private McpRegistryServerDetail server;
    
    /** Registry 扩展元数据（{@code _meta} 字段）。 */
    @JsonProperty("_meta")
    private Meta meta;
    
    public McpRegistryServerDetail getServer() {
        return server;
    }
    
    public void setServer(McpRegistryServerDetail server) {
        this.server = server;
    }
    
    public Meta getMeta() {
        return meta;
    }
    
    public void setMeta(Meta meta) {
        this.meta = meta;
    }
    
    /**
     * {@code _meta} 元数据包装，支持官方与扩展命名空间。
     *
     * <p>官方信息存于 {@link #official}，其余键值通过 {@link #additionalMetadata} 透传。</p>
     *
     * @author xinluo
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        
        /** 官方 Registry 元数据。 */
        @JsonProperty("io.modelcontextprotocol.registry/official")
        private OfficialMeta official;
        
        /** 其他扩展命名空间键值对。 */
        @JsonAnySetter
        private Map<String, Object> additionalMetadata = new HashMap<>();
        
        public OfficialMeta getOfficial() {
            return official;
        }
        
        public void setOfficial(OfficialMeta official) {
            this.official = official;
        }
        
        @JsonAnyGetter
        public Map<String, Object> getAdditionalMetadata() {
            return additionalMetadata;
        }
    }
}
