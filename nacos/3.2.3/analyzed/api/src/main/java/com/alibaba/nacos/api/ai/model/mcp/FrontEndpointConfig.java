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

package com.alibaba.nacos.api.ai.model.mcp;

import com.alibaba.nacos.api.ai.model.mcp.registry.KeyValueInput;

import java.util.List;

import com.alibaba.nacos.api.ai.constant.AiConstants;

/**
 * 对外暴露的前端端点配置，描述 MCP Server 面向客户端的访问入口。
 *
 * <p>根据 {@link #endpointType} 不同，{@link #endpointData} 的实际类型各异，
 * 可指向 Nacos 服务引用、直连地址或透传后端端点。</p>
 *
 * @author OmCheeLin
 */
public class FrontEndpointConfig {
    
    /** 端点配置类型标识。 */
    private String type;
    
    /** 访问协议（如 http、https）。 */
    private String protocol;
    
    /** 端点数据类型，决定 {@link #endpointData} 的解析方式。 */
    private String endpointType;
    
    /**
     * 端点数据，具体类型取决于 {@link #endpointType}：
     * <ul>
     *     <li>{@link AiConstants.Mcp#MCP_ENDPOINT_TYPE_REF} 时为 {@link McpServiceRef}</li>
     *     <li>{@link AiConstants.Mcp#MCP_ENDPOINT_TYPE_DIRECT} 时为 {@link String}</li>
     *     <li>{@link AiConstants.Mcp#MCP_FRONT_ENDPOINT_TYPE_TO_BACK} 时为 {@code null}（透传后端）</li>
     * </ul>
     */
    private Object endpointData;
    
    /** 端点访问路径。 */
    private String path;
    
    /** 请求头列表。 */
    private List<KeyValueInput> headers;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getEndpointType() {
        return endpointType;
    }
    
    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }
    
    public Object getEndpointData() {
        return endpointData;
    }
    
    public void setEndpointData(Object endpointData) {
        this.endpointData = endpointData;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public List<KeyValueInput> getHeaders() {
        return headers;
    }
    
    public void setHeaders(List<KeyValueInput> headers) {
        this.headers = headers;
    }
}
