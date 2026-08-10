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

package com.alibaba.nacos.api.ai.remote.request;

import com.alibaba.nacos.api.ai.remote.AiRemoteConstants;

/**
 * 向 Nacos AI 模块注册或注销 MCP Server 端点的远程请求。
 *
 * <p>继承 {@link AbstractMcpRequest}，通过 {@link #address}、{@link #port} 与可选 {@link #version}
 * 标识服务端点，{@link #type} 区分注册与注销操作。</p>
 *
 * @author xiweng.yy
 */
public class McpServerEndpointRequest extends AbstractMcpRequest {
    
    /** MCP Server 端点主机地址。 */
    private String address;
    
    /** MCP Server 端点端口号。 */
    private int port;
    
    /** 端点关联的 MCP Server 版本；为空时表示该端点对所有版本可见。 */
    private String version;
    
    /** 操作类型，取值为 {@link AiRemoteConstants#REGISTER_ENDPOINT} 或 {@link AiRemoteConstants#DE_REGISTER_ENDPOINT}。 */
    private String type;
    
    /** 获取端点主机地址。 */
    public String getAddress() {
        return address;
    }
    
    /** 设置端点主机地址。 */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /** 获取端点端口号。 */
    public int getPort() {
        return port;
    }
    
    /** 设置端点端口号。 */
    public void setPort(int port) {
        this.port = port;
    }
    
    /** 获取注册/注销操作类型。 */
    public String getType() {
        return type;
    }
    
    /** 设置注册/注销操作类型。 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 获取端点关联的 MCP Server 版本。 */
    public String getVersion() {
        return version;
    }
    
    /** 设置端点关联的 MCP Server 版本。 */
    public void setVersion(String version) {
        this.version = version;
    }
}
