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

/**
 * MCP Server 能力枚举，标识服务端支持的 MCP 能力类型（工具、提示词或资源）。
 *
 * <p>由 Nacos 自动探测或注册时写入，客户端可据此判断 Server 是否提供
 * 对应类别的 MCP 接口。</p>
 *
 * @author xiweng.yy
 */
public enum McpCapability {
    
    /** MCP Server 提供工具（Tools）能力。 */
    TOOL,
    
    /** MCP Server 提供提示词（Prompts）能力。 */
    PROMPT,
    
    /** MCP Server 提供资源（Resources）能力。 */
    RESOURCE;
}
