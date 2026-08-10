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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * 可流式 HTTP 传输配置，对应 components.schemas.StreamableHttpTransport。
 *
 * <p>客户端通过 HTTP 与 MCP Server 通信，支持流式请求/响应，
 * 适用于远程部署的 Server。</p>
 *
 * @author xinluo
 */
@JsonTypeName("streamable-http")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamableHttpTransport {
    
    /** 传输类型，固定为 {@code streamable-http}。 */
    private String type = "streamable-http";
    
    /** HTTP 端点 URL。 */
    private String url;
    
    /** 连接时附加的 HTTP 请求头。 */
    private List<KeyValueInput> headers;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public List<KeyValueInput> getHeaders() {
        return headers;
    }
    
    public void setHeaders(List<KeyValueInput> headers) {
        this.headers = headers;
    }
}
