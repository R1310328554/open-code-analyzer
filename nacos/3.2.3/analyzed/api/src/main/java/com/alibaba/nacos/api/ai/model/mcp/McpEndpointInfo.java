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

import java.util.List;

import com.alibaba.nacos.api.ai.model.mcp.registry.KeyValueInput;

/**
 * MCP 后端端点信息，描述 Server 实际监听或转发的网络地址。
 *
 * <p>用于 {@link McpServerDetailInfo} 中配置前后端端点列表，
 * 包含协议、地址、端口、路径及自定义请求头。</p>
 *
 * @author xiweng.yy
 */
public class McpEndpointInfo {
    
    /** 端点协议（http / https）。 */
    private String protocol;
    
    /** 主机地址（IP 或域名）。 */
    private String address;
    
    /** 监听端口。 */
    private int port;
    
    /** URL 路径。 */
    private String path;
    
    /** 自定义请求头列表。 */
    private List<KeyValueInput> headers;
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public List<KeyValueInput> getHeaders() {
        return headers;
    }
    
    public void setHeaders(List<KeyValueInput> headers) {
        this.headers = headers;
    }
}
