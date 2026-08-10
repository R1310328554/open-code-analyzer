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

package com.alibaba.nacos.auth.parser.http;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Properties;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_AGENT;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_AGENT_SPEC;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_MCP;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_PROMPT;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.AI_TYPE_SKILL;

/**
 * AI 模块 HTTP 资源解析器。
 *
 * <p>根据请求 URI 路径（MCP、A2A、Skill、Prompt、AgentSpec）解析对应 AI 资源的
 * 命名空间、分组与资源名，并在扩展属性中标记 AI 子类型。</p>
 *
 * @author xiweng.yy
 */
public class AiHttpResourceParser extends AbstractHttpResourceParser {
    
    /** MCP 相关 API 路径前缀。 */
    public static final String MCP_PATH = "/ai/mcp";
    
    /** A2A Agent 相关 API 路径前缀。 */
    public static final String A2A_PATH = "/ai/a2a";
    
    /** Skill 相关 API 路径前缀。 */
    public static final String SKILL_PATH = "/ai/skills";
    
    /** Prompt 相关 API 路径前缀。 */
    public static final String PROMPT_PATH = "/ai/prompt";
    
    /** AgentSpec 相关 API 路径前缀。 */
    public static final String AGENT_SPEC_PATH = "/ai/agentSpec";
    
    /** 请求参数中 AgentCard JSON 的键名。 */
    private static final String AGENT_CARD_PARAM = "agentCard";
    
    /** {@inheritDoc} — 从请求参数读取 namespaceId，缺省使用 MCP 默认命名空间。 */
    @Override
    protected String getNamespaceId(HttpServletRequest request) {
        String namespaceId = request.getParameter(Constants.NAMESPACE_ID);
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = AiConstants.Mcp.MCP_DEFAULT_NAMESPACE;
        }
        return namespaceId;
    }
    
    /** {@inheritDoc} — AI 资源统一使用默认分组。 */
    @Override
    protected String getGroup(HttpServletRequest request) {
        return Constants.DEFAULT_GROUP;
    }
    
    /** {@inheritDoc} — 按 URI 路径分支解析 MCP / Agent / Skill / Prompt / AgentSpec 资源名。 */
    @Override
    protected String getResourceName(HttpServletRequest request) {
        String url = request.getRequestURI();
        if (url.contains(MCP_PATH)) {
            return getMcpName(request);
        } else if (url.contains(A2A_PATH)) {
            return getAgentName(request);
        } else if (url.contains(SKILL_PATH)) {
            return getSkillName(request);
        } else if (url.contains(PROMPT_PATH)) {
            return getPromptName(request);
        } else if (url.contains(AGENT_SPEC_PATH)) {
            return getAgentSpecName(request);
        }
        return StringUtils.EMPTY;
    }
    
    /** 从请求参数 mcpName 读取 MCP 服务名。 */
    private String getMcpName(HttpServletRequest request) {
        String mcpName = request.getParameter("mcpName");
        return StringUtils.isBlank(mcpName) ? StringUtils.EMPTY : mcpName;
    }
    
    /** 从 agentName 或 agentCard JSON 解析 Agent 名称。 */
    private String getAgentName(HttpServletRequest request) {
        String agentName = request.getParameter("agentName");
        if (request.getParameterMap().containsKey(AGENT_CARD_PARAM)) {
            agentName = deserializeAndGetAgentName(request.getParameter(AGENT_CARD_PARAM));
        }
        return StringUtils.isBlank(agentName) ? StringUtils.EMPTY : agentName;
    }
    
    /** 反序列化 AgentCard JSON 并提取 name 字段。 */
    private String deserializeAndGetAgentName(String agentCardJson) {
        try {
            AgentCard agentCard = JacksonUtils.toObj(agentCardJson, AgentCard.class);
            return agentCard.getName();
        } catch (Exception ignored) {
            return StringUtils.EMPTY;
        }
    }
    
    /** 从请求参数 skillName 读取 Skill 名称。 */
    private String getSkillName(HttpServletRequest request) {
        String skillName = request.getParameter("skillName");
        return StringUtils.isBlank(skillName) ? StringUtils.EMPTY : skillName;
    }
    
    /** 从请求参数 promptKey 读取 Prompt 键名。 */
    private String getPromptName(HttpServletRequest request) {
        String promptKey = request.getParameter("promptKey");
        return StringUtils.isBlank(promptKey) ? StringUtils.EMPTY : promptKey;
    }
    
    /** 从请求参数 agentSpecName 读取 AgentSpec 名称。 */
    private String getAgentSpecName(HttpServletRequest request) {
        String agentSpecName = request.getParameter("agentSpecName");
        return StringUtils.isBlank(agentSpecName) ? StringUtils.EMPTY : agentSpecName;
    }
    
    /** {@inheritDoc} — 按 URI 路径写入 AI 子类型扩展属性。 */
    @Override
    protected Properties getProperties(HttpServletRequest request) {
        Properties properties = new Properties();
        String url = request.getRequestURI();
        if (url.contains(MCP_PATH)) {
            properties.setProperty(AI_TYPE, AI_TYPE_MCP);
        } else if (url.contains(A2A_PATH)) {
            properties.setProperty(AI_TYPE, AI_TYPE_AGENT);
        } else if (url.contains(SKILL_PATH)) {
            properties.setProperty(AI_TYPE, AI_TYPE_SKILL);
        } else if (url.contains(PROMPT_PATH)) {
            properties.setProperty(AI_TYPE, AI_TYPE_PROMPT);
        } else if (url.contains(AGENT_SPEC_PATH)) {
            properties.setProperty(AI_TYPE, AI_TYPE_AGENT_SPEC);
        }
        return properties;
    }
}
