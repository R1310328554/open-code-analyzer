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

package com.alibaba.nacos.api.ai.constant;

import com.alibaba.nacos.api.ai.model.mcp.registry.McpServerStatusEnum;

/**
 * Nacos AI 模块常量定义，涵盖 MCP、A2A 协议与客户端配置键。
 *
 * @author xiweng.yy
 */
public class AiConstants {
    
    /** MCP 相关常量。 */
    public static class Mcp {
        
        /** MCP 默认命名空间。 */
        public static final String MCP_DEFAULT_NAMESPACE = "public";
        
        /** stdio 传输协议标识。 */
        public static final String MCP_PROTOCOL_STDIO = "stdio";
        
        /** SSE 传输协议标识。 */
        public static final String MCP_PROTOCOL_SSE = "mcp-sse";
        
        /** 可流式 HTTP 传输协议标识。 */
        public static final String MCP_PROTOCOL_STREAMABLE = "mcp-streamable";
        
        /** HTTP 传输协议标识。 */
        public static final String MCP_PROTOCOL_HTTP = "http";
        
        /** Dubbo 传输协议标识。 */
        public static final String MCP_PROTOCOL_DUBBO = "dubbo";
        
        /** 端点类型：引用 Nacos 服务。 */
        public static final String MCP_ENDPOINT_TYPE_REF = "REF";
        
        /** 端点类型：直连地址。 */
        public static final String MCP_ENDPOINT_TYPE_DIRECT = "DIRECT";
        
        /** 前端端点映射至后端服务的类型标识。 */
        public static final String MCP_FRONT_ENDPOINT_TYPE_TO_BACK = "BACKEND";
        
        /** MCP 服务器活跃状态名。 */
        public static final String MCP_STATUS_ACTIVE = McpServerStatusEnum.ACTIVE.getName();
        
        /** MCP 服务器已废弃状态名。 */
        public static final String MCP_STATUS_DEPRECATED = McpServerStatusEnum.DEPRECATED.getName();
        
        /** MCP 服务器已删除状态名。 */
        public static final String MCP_STATUS_DELETED = McpServerStatusEnum.DELETED.getName();
        
        /** Registry 官方 SSE 传输名。 */
        public static final String OFFICIAL_TRANSPORT_SSE = "sse";
        
        /** Registry 官方可流式 HTTP 传输名。 */
        public static final String OFFICIAL_TRANSPORT_STREAMABLE = "streamable-http";
    }
    
    /** AI 客户端传输模式配置键（gRPC/HTTP）。 */
    public static final String AI_TRANSPORT_MODE = "nacosAiTransportMode";
    
    /** AI 传输模式：gRPC。 */
    public static final String AI_TRANSPORT_MODE_GRPC = "grpc";
    
    /** AI 传输模式：HTTP。 */
    public static final String AI_TRANSPORT_MODE_HTTP = "http";
    
    /** AI 请求超时配置键（毫秒）。 */
    public static final String AI_REQUEST_TIMEOUT = "nacosAiRequestTimeout";
    
    /** MCP 服务器本地缓存刷新间隔配置键。 */
    public static final String AI_MCP_SERVER_CACHE_UPDATE_INTERVAL =
        "nacosAiMcpServerCacheUpdateInterval";
    
    /** Agent Card 本地缓存刷新间隔配置键。 */
    public static final String AI_AGENT_CARD_CACHE_UPDATE_INTERVAL =
        "nacosAiAgentCardCacheUpdateInterval";
    
    /** Prompt 本地缓存刷新间隔配置键。 */
    public static final String AI_PROMPT_CACHE_UPDATE_INTERVAL = "nacosAiPromptCacheUpdateInterval";
    
    /** Skill 本地缓存刷新间隔配置键。 */
    public static final String AI_SKILL_CACHE_UPDATE_INTERVAL = "nacosAiSkillCacheUpdateInterval";
    
    /** AgentSpec 本地缓存刷新间隔配置键。 */
    public static final String AI_AGENTSPEC_CACHE_UPDATE_INTERVAL =
        "nacosAiAgentSpecCacheUpdateInterval";
    
    /** 各类 AI 缓存默认刷新间隔（毫秒）。 */
    public static final long DEFAULT_AI_CACHE_UPDATE_INTERVAL = 10000L;
    
    /** A2A（Agent-to-Agent）协议相关常量。 */
    public static class A2a {
        
        /** A2A 默认命名空间。 */
        public static final String A2A_DEFAULT_NAMESPACE = "public";
        
        /**
         * 端点类型：发现 A2A Agent 时直接使用 Agent Card 中的 url 字段。
         */
        public static final String A2A_ENDPOINT_TYPE_URL = "URL";
        
        /**
         * 端点类型：发现 A2A Agent 时使用 backend 服务解析地址。
         */
        public static final String A2A_ENDPOINT_TYPE_SERVICE = "SERVICE";
        
        /** A2A 端点默认传输协议。 */
        public static final String A2A_ENDPOINT_DEFAULT_TRANSPORT = "JSONRPC";
        
        /** A2A 端点默认应用层协议。 */
        public static final String A2A_ENDPOINT_DEFAULT_PROTOCOL = "HTTP";
    }
}
