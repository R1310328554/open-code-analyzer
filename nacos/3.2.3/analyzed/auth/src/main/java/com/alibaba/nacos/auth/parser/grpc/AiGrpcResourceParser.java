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

package com.alibaba.nacos.auth.parser.grpc;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.remote.request.AbstractAgentRequest;
import com.alibaba.nacos.api.ai.remote.request.AbstractMcpRequest;
import com.alibaba.nacos.api.ai.remote.request.AbstractPromptRequest;
import com.alibaba.nacos.api.ai.remote.request.ReleaseAgentCardRequest;
import com.alibaba.nacos.api.ai.remote.request.ReleaseMcpServerRequest;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Properties;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_AGENT;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_MCP;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_PROMPT;

/**
 * AI 模块 gRPC 资源解析器。
 *
 * <p>从 MCP、Agent、Prompt 等 AI 远程请求中提取命名空间、分组与资源名，
 * 并在扩展属性中标记 AI 子类型（MCP / Agent / Prompt），供授权插件区分资源类别。</p>
 *
 * @author hongye.nhy xiweng.yy
 */
public class AiGrpcResourceParser extends AbstractGrpcResourceParser {
    
    /** {@inheritDoc} — 按请求类型读取 namespaceId，缺省时使用 MCP 默认命名空间。 */
    @Override
    protected String getNamespaceId(Request request) {
        String namespaceId = null;
        if (request instanceof AbstractMcpRequest) {
            namespaceId = ((AbstractMcpRequest) request).getNamespaceId();
        } else if (request instanceof AbstractAgentRequest) {
            namespaceId = ((AbstractAgentRequest) request).getNamespaceId();
        } else if (request instanceof AbstractPromptRequest) {
            namespaceId = ((AbstractPromptRequest) request).getNamespaceId();
        }
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = AiConstants.Mcp.MCP_DEFAULT_NAMESPACE;
        }
        return namespaceId;
    }
    
    /** {@inheritDoc} — AI 资源统一使用默认分组。 */
    @Override
    protected String getGroup(Request request) {
        return Constants.DEFAULT_GROUP;
    }
    
    /** {@inheritDoc} — 按 MCP / Agent / Prompt 请求类型解析资源名。 */
    @Override
    protected String getResourceName(Request request) {
        if (request instanceof AbstractMcpRequest) {
            return getMcpName((AbstractMcpRequest) request);
        } else if (request instanceof AbstractAgentRequest) {
            return getAgentName((AbstractAgentRequest) request);
        } else if (request instanceof AbstractPromptRequest) {
            return getPromptName((AbstractPromptRequest) request);
        }
        return StringUtils.EMPTY;
    }
    
    /** 从 MCP 请求解析 MCP 服务名，发布请求优先取规格中的名称。 */
    private String getMcpName(AbstractMcpRequest request) {
        String mcpName = request.getMcpName();
        if (request instanceof ReleaseMcpServerRequest) {
            ReleaseMcpServerRequest releaseMcpServerRequest = (ReleaseMcpServerRequest) request;
            if (null != releaseMcpServerRequest.getServerSpecification()) {
                mcpName = releaseMcpServerRequest.getServerSpecification().getName();
            }
        }
        return StringUtils.isBlank(mcpName) ? StringUtils.EMPTY : mcpName;
    }
    
    /** 从 Agent 请求解析 Agent 名称，发布 AgentCard 时优先取卡片名称。 */
    private String getAgentName(AbstractAgentRequest request) {
        String agentName = request.getAgentName();
        if (request instanceof ReleaseAgentCardRequest) {
            ReleaseAgentCardRequest releaseAgentCardRequest = (ReleaseAgentCardRequest) request;
            if (null != releaseAgentCardRequest.getAgentCard()) {
                agentName = releaseAgentCardRequest.getAgentCard().getName();
            }
        }
        return StringUtils.isBlank(agentName) ? StringUtils.EMPTY : agentName;
    }
    
    /** 从 Prompt 请求解析 promptKey 作为资源名。 */
    private String getPromptName(AbstractPromptRequest request) {
        String promptKey = request.getPromptKey();
        return StringUtils.isBlank(promptKey) ? StringUtils.EMPTY : promptKey;
    }
    
    /** {@inheritDoc} — 在父类属性基础上写入 AI 子类型标识。 */
    @Override
    protected Properties getProperties(Request request) {
        Properties properties = super.getProperties(request);
        if (request instanceof AbstractMcpRequest) {
            properties.setProperty(AI_TYPE, AI_TYPE_MCP);
        } else if (request instanceof AbstractAgentRequest) {
            properties.setProperty(AI_TYPE, AI_TYPE_AGENT);
        } else if (request instanceof AbstractPromptRequest) {
            properties.setProperty(AI_TYPE, AI_TYPE_PROMPT);
        }
        return properties;
    }
}
