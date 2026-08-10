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

package com.alibaba.nacos.api.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.ai.listener.AbstractNacosAgentSpecListener;
import com.alibaba.nacos.api.ai.listener.AbstractNacosMcpServerListener;
import com.alibaba.nacos.api.ai.listener.AbstractNacosPromptListener;
import com.alibaba.nacos.api.ai.listener.AbstractNacosSkillListener;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpResourceSpecification;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.ai.model.prompt.Prompt;
import com.alibaba.nacos.api.exception.NacosException;

/**
 * Nacos AI 客户端服务接口，聚合 MCP、Skill、AgentSpec、Prompt 与 A2A 能力。
 *
 * <p>继承 {@link A2aService}，通过 {@link AiFactory#createAiService(java.util.Properties)} 创建实例。</p>
 *
 * @author xiweng.yy
 */
public interface AiService extends A2aService {
    
    /**
     * 获取 MCP 服务器最新版本详情。
     *
     * @param mcpName name of mcp server
     * @return detail information of MCP server
     * @throws NacosException if request parameter is invalid or mcp server not found or handle error
     */
    @Since("3.0.3")
    default McpServerDetailInfo getMcpServer(String mcpName) throws NacosException {
        return getMcpServer(mcpName, null);
    }
    
    /**
     * 按版本获取 MCP 服务器详情（version 为空则取最新）。
     *
     * @param mcpName name of MCP name
     * @param version version of MCP, if null, will get the latest version
     * @return detail information of MCP server
     * @throws NacosException if request parameter is invalid or mcp server not found or handle error
     */
    @Since("3.0.3")
    McpServerDetailInfo getMcpServer(String mcpName, String version) throws NacosException;
    
    /**
     * 发布新 MCP 服务器或已有服务器的新版本。
     *
     * <p>
     *     If mcp server is not exist, will create an new mcp server with parameter specification.
     *     If mcp server is exist, but version in specification is new one, request will create a new version of mcp server.
     *     If mcp server is exist, and version in specification is exist, request will do nothing.
     * </p>
     *
     * @param serverSpecification mcp server specification
     * @param toolSpecification   mcp server tool specification
     * @return mcp id
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.0.3")
    default String releaseMcpServer(McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification)
        throws NacosException {
        return releaseMcpServer(serverSpecification, toolSpecification, (McpEndpointSpec) null);
    }
    
    /**
     * Release new mcp server or release new version of exist mcp server request.
     *
     * @param serverSpecification mcp server specification
     * @param toolSpecification mcp server tool specification
     * @param resourceSpecification mcp server resource specification
     * @return mcp id
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.2.1")
    default String releaseMcpServer(McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification,
        McpResourceSpecification resourceSpecification) throws NacosException {
        return releaseMcpServer(serverSpecification, toolSpecification, resourceSpecification,
            null);
    }
    
    /**
     * Release new mcp server or release new version of exist mcp server request.
     *
     * <p>
     *     If mcp server is not exist, will create an new mcp server with parameter specification.
     *     If mcp server is exist, but version in specification is new one, request will create a new version of mcp server.
     *     If mcp server is exist, and version in specification is exist, request will do nothing.
     * </p>
     *
     * @param serverSpecification mcp server specification
     * @param toolSpecification   mcp server tool specification
     * @param endpointSpecification mcp server endpoint specification, optional, if null, will create ref service auto.
     * @return mcp id
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.0.3")
    String releaseMcpServer(McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification,
        McpEndpointSpec endpointSpecification) throws NacosException;
    
    /**
     * Release new mcp server or release new version of exist mcp server request.
     *
     * @param serverSpecification mcp server specification
     * @param toolSpecification mcp server tool specification
     * @param resourceSpecification mcp server resource specification
     * @param endpointSpecification mcp server endpoint specification, optional, if null, will create ref service auto.
     * @return mcp id
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.2.1")
    String releaseMcpServer(McpServerBasicInfo serverSpecification,
        McpToolSpecification toolSpecification,
        McpResourceSpecification resourceSpecification, McpEndpointSpec endpointSpecification)
        throws NacosException;
    
    /**
     * 向 MCP 服务器注册端点（适用于全部版本）。
     *
     * @param mcpName   name of mcp server
     * @param address   address of endpoint
     * @param port      port of endpoint
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.0.3")
    default void registerMcpServerEndpoint(String mcpName, String address, int port)
        throws NacosException {
        registerMcpServerEndpoint(mcpName, address, port, null);
    }
    
    /**
     * 向指定版本的 MCP 服务器注册端点。
     *
     * @param mcpName   name of mcp server
     * @param address   address of endpoint
     * @param port      port of endpoint
     * @param version   version of mcp server
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.0.3")
    void registerMcpServerEndpoint(String mcpName, String address, int port, String version)
        throws NacosException;
    
    /**
     * 从 MCP 服务器注销本客户端注册的端点。
     *
     * <p>
     *     The registered endpoint must be registered by this client service.
     *     If the registered endpoint is registered by other client service, the endpoint will fail to deregister.
     * </p>
     *
     * @param mcpName   name of mcp server
     * @param address   address of endpoint
     * @param port      port of endpoint
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.0.3")
    void deregisterMcpServerEndpoint(String mcpName, String address, int port)
        throws NacosException;
    
    /**
     * 订阅 MCP 服务器变更。
     *
     * @param mcpName           name of mcp server
     * @param mcpServerListener listener of mcp server, callback when mcp server is changed
     * @return The detail info of mcp server at current time
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.0.3")
    default McpServerDetailInfo subscribeMcpServer(String mcpName,
        AbstractNacosMcpServerListener mcpServerListener)
        throws NacosException {
        return subscribeMcpServer(mcpName, null, mcpServerListener);
    }
    
    /**
     * Subscribe mcp server.
     *
     * @param mcpName           name of mcp server
     * @param version           version of mcp server
     * @param mcpServerListener listener of mcp server, callback when mcp server is changed
     * @return The detail info of mcp server at current time, nullable if agent card not found
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.0.3")
    McpServerDetailInfo subscribeMcpServer(String mcpName, String version,
        AbstractNacosMcpServerListener mcpServerListener) throws NacosException;
    
    /**
     * 取消 MCP 服务器订阅。
     *
     * @param mcpName           name of mcp server
     * @param mcpServerListener listener of mcp server
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.0.3")
    default void unsubscribeMcpServer(String mcpName,
        AbstractNacosMcpServerListener mcpServerListener)
        throws NacosException {
        unsubscribeMcpServer(mcpName, null, mcpServerListener);
    }
    
    /**
     * Un-subscribe mcp server.
     *
     * @param mcpName           name of mcp server
     * @param version           version of mcp server
     * @param mcpServerListener listener of mcp server
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.0.3")
    void unsubscribeMcpServer(String mcpName, String version,
        AbstractNacosMcpServerListener mcpServerListener)
        throws NacosException;
    
    /**
     * 按名称下载 Skill ZIP（默认最新版本）。
     *
     * <p>The ZIP contains the skill directory structure: SKILL.md and all resource files.
     * Binary resources are decoded from Base64 back to raw bytes.</p>
     *
     * @param skillName skill name (unique identifier)
     * @return ZIP file as byte array
     * @throws NacosException if skill not found or query error
     */
    @Since("3.2.0")
    byte[] downloadSkillZip(String skillName) throws NacosException;
    
    /**
     * 按名称与版本下载 Skill ZIP。
     *
     * @param skillName skill name (unique identifier)
     * @param version   target skill version, if null, will get latest version
     * @return ZIP file as byte array
     * @throws NacosException if skill not found or query error
     */
    @Since("3.2.0")
    byte[] downloadSkillZipByVersion(String skillName, String version) throws NacosException;
    
    /**
     * 按名称与标签下载 Skill ZIP。
     *
     * @param skillName skill name (unique identifier)
     * @param label     target skill label (e.g. "latest", "stable")
     * @return ZIP file as byte array
     * @throws NacosException if skill not found or query error
     */
    @Since("3.2.0")
    byte[] downloadSkillZipByLabel(String skillName, String label) throws NacosException;
    
    // ==================== AgentSpec 管理 API ====================
    
    /**
     * 按名称加载完整 AgentSpec（含全部资源配置）。
     *
     * <p>
     * This method will query the agent spec main configuration and all resource configurations,
     * then assemble them into a complete AgentSpec object.
     * </p>
     *
     * @param agentSpecName agent spec name (unique identifier)
     * @return complete AgentSpec object with all resources
     * @throws NacosException if agent spec not found or query error
     */
    @Since("3.2.0")
    AgentSpec loadAgentSpec(String agentSpecName) throws NacosException;
    
    /**
     * 订阅 AgentSpec 配置变更。
     *
     * @param agentSpecName       name of agent spec
     * @param agentSpecListener   listener of agent spec, callback when agent spec configuration is changed
     * @return The agent spec object at current time, nullable if agent spec not found
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.2.0")
    AgentSpec subscribeAgentSpec(String agentSpecName,
        AbstractNacosAgentSpecListener agentSpecListener)
        throws NacosException;
    
    /**
     * 取消 AgentSpec 订阅。
     *
     * @param agentSpecName       name of agent spec
     * @param agentSpecListener   listener of agent spec
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.2.0")
    void unsubscribeAgentSpec(String agentSpecName,
        AbstractNacosAgentSpecListener agentSpecListener)
        throws NacosException;
    
    // ==================== Prompt 管理 API ====================
    
    /**
     * 按 promptKey 获取 Prompt（当前版本）。
     *
     * @param promptKey prompt key (unique identifier)
     * @return prompt object with current version
     * @throws NacosException if prompt not found or query error
     */
    @Since("3.2.0")
    Prompt getPrompt(String promptKey) throws NacosException;
    
    /**
     * 按 promptKey 与版本获取 Prompt。
     *
     * @param promptKey prompt key (unique identifier)
     * @param version target prompt version, if null, will get latest version
     * @return prompt object with target version
     * @throws NacosException if prompt not found or query error
     */
    @Since("3.2.0")
    Prompt getPromptByVersion(String promptKey, String version) throws NacosException;
    
    /**
     * 按 promptKey 与标签获取 Prompt。
     *
     * @param promptKey prompt key (unique identifier)
     * @param label target prompt label
     * @return prompt object with target label
     * @throws NacosException if prompt not found or query error
     */
    @Since("3.2.0")
    Prompt getPromptByLabel(String promptKey, String label) throws NacosException;
    
    /**
     * 订阅 Prompt 配置变更。
     *
     * @param promptKey      prompt key
     * @param version        target prompt version, optional
     * @param label          target prompt label, optional
     * @param promptListener listener for prompt changes
     * @return current prompt object, may be null if prompt not found
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.2.0")
    Prompt subscribePrompt(String promptKey, String version, String label,
        AbstractNacosPromptListener promptListener) throws NacosException;
    
    /**
     * 取消 Prompt 订阅。
     *
     * @param promptKey      prompt key
     * @param version        target prompt version, optional
     * @param label          target prompt label, optional
     * @param promptListener listener for prompt changes
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.2.0")
    void unsubscribePrompt(String promptKey, String version, String label,
        AbstractNacosPromptListener promptListener) throws NacosException;
    
    /**
     * 订阅 Skill 变更，返回当前 ZIP 字节。
     *
     * @param skillName     skill name
     * @param version       target skill version, optional
     * @param label         target skill label, optional
     * @param skillListener listener for skill changes
     * @return current skill ZIP bytes, may be {@code null} when the skill is not found
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.2.2")
    byte[] subscribeSkill(String skillName, String version, String label,
        AbstractNacosSkillListener skillListener) throws NacosException;
    
    /**
     * 取消 Skill 订阅。
     *
     * @param skillName     skill name
     * @param version       target skill version, optional
     * @param label         target skill label, optional
     * @param skillListener listener previously registered via
     *                      {@link #subscribeSkill(String, String, String, AbstractNacosSkillListener)}
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.2.2")
    void unsubscribeSkill(String skillName, String version, String label,
        AbstractNacosSkillListener skillListener) throws NacosException;
    
    /**
     * 关闭 AI 服务并释放资源。
     *
     * @throws NacosException exception.
     */
    @Since("3.0.3")
    void shutdown() throws NacosException;
    
}
