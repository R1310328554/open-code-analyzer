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

import com.alibaba.nacos.api.ai.constant.AiConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP Server 端点规格声明，以类型 + 键值对描述端点连接方式。
 *
 * <p>支持直连（DIRECT）与 Nacos 服务引用（REF）两种模式，
 * {@link #data} 字段随 {@link #type} 不同而包含不同键集合。</p>
 *
 * @author xiweng.yy
 */
public class McpEndpointSpec {
    
    /**
     * 端点类型，应为 {@link AiConstants.Mcp#MCP_ENDPOINT_TYPE_DIRECT} 或
     * {@link AiConstants.Mcp#MCP_ENDPOINT_TYPE_REF}。
     */
    private String type;
    
    /**
     * 端点数据，随 {@link #type} 不同而包含不同键：
     * <p>
     *  DIRECT 模式需包含 {@code address}、{@code port} 以指定 MCP Server 直连地址。
     * </p>
     * <p>
     *  REF 模式需包含 {@code namespaceId}、{@code groupName}、{@code serviceName}，
     *  引用已注册到 Nacos 的服务实例。
     * </p>
     */
    private Map<String, String> data = new HashMap<>();
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Map<String, String> getData() {
        return data;
    }
    
    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
