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

package com.alibaba.nacos.client.ai.remote.redo;

import java.util.Objects;

/**
 * MCP 服务器端点信息，供重做（redo）数据序列化与比对使用。
 *
 * <p>包含地址、端口与版本，用于 {@link McpServerEndpointRedoData} 在连接恢复后重试注册。</p>
 *
 * @author xiweng.yy
 */
public class McpServerEndpoint {
    
    /** MCP 服务器地址。 */
    private final String address;
    
    /** MCP 服务器端口。 */
    private final int port;
    
    /** MCP 服务器版本号。 */
    private final String version;
    
    /**
     * 构造 MCP 服务器端点。
     *
     * @param address 服务器地址
     * @param port    服务器端口
     * @param version 版本号
     */
    public McpServerEndpoint(String address, int port, String version) {
        this.address = address;
        this.port = port;
        this.version = version;
    }
    
    /** 获取服务器地址。 */
    public String getAddress() {
        return address;
    }
    
    /** 获取服务器端口。 */
    public int getPort() {
        return port;
    }
    
    /** 获取版本号。 */
    public String getVersion() {
        return version;
    }
    
    @Override
    /** 基于地址、端口与版本判断相等性。 */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        McpServerEndpoint that = (McpServerEndpoint) o;
        return port == that.port && Objects.equals(address, that.address)
            && Objects.equals(version, that.version);
    }
    
    @Override
    /** 返回哈希码。 */
    public int hashCode() {
        return Objects.hash(address, port, version);
    }
}
