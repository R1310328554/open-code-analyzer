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

import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.client.ai.remote.AiGrpcClient;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.redo.data.RedoData;
import com.alibaba.nacos.client.redo.service.AbstractRedoService;
import com.alibaba.nacos.client.redo.service.AbstractRedoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Nacos AI gRPC 重做服务。
 *
 * <p>继承 {@link AbstractRedoService}，缓存 MCP 服务端点与 Agent 端点的注册/注销操作，在 gRPC 连接恢复后由 {@link AiRedoScheduledTask} 自动重放。</p>
 *
 * @author xiweng.yy
 */
public class AiGrpcRedoService extends AbstractRedoService {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(AiGrpcRedoService.class);
    
    /** 关联的 AI gRPC 客户端。 */
    private final AiGrpcClient aiGrpcClient;
    
    /**
     * 构造 AI 重做服务并启动定时重做任务。
     *
     * @param properties   客户端配置
     * @param aiGrpcClient AI gRPC 客户端
     */
        super(LOGGER, properties, RemoteConstants.LABEL_MODULE_AI);
        this.aiGrpcClient = aiGrpcClient;
        startRedoTask();
    }
    
    @Override
    /** 构建 AI 模块定时重做任务。 */
    protected AbstractRedoTask buildRedoTask() {
        return new AiRedoScheduledTask(this, aiGrpcClient);
    }
    
    /**
     * 缓存 MCP 服务端点注册信息以供重做。
     *
     * @param mcpName MCP 服务名称
     * @param address 端点地址
     * @param port    端点端口
     * @param version 端点版本
     */
    public void cachedMcpServerEndpointForRedo(String mcpName, String address, int port,
        String version) {
        RedoData<McpServerEndpoint> redoData =
            buildMcpServerEndpointRedoData(mcpName, address, port, version);
        super.cachedRedoData(mcpName, redoData, McpServerEndpoint.class);
    }
    
    /** 移除 MCP 服务端点的重做缓存。 */
    public void removeMcpServerEndpointForRedo(String mcpName) {
        super.removeRedoData(mcpName, McpServerEndpoint.class);
    }
    
    /** 标记 MCP 服务端点已成功注册。 */
    public void mcpServerEndpointRegistered(String mcpName) {
        super.dataRegistered(mcpName, McpServerEndpoint.class);
    }
    
    /** 标记 MCP 服务端点待注销。 */
    public void mcpServerEndpointDeregister(String mcpName) {
        super.dataDeregister(mcpName, McpServerEndpoint.class);
    }
    
    /** 标记 MCP 服务端点已成功注销。 */
    public void mcpServerEndpointDeregistered(String mcpName) {
        super.dataDeregistered(mcpName, McpServerEndpoint.class);
    }
    
    /** 判断 MCP 服务端点是否已注册。 */
    public boolean isMcpServerEndpointRegistered(String mcpName) {
        return super.isDataRegistered(mcpName, McpServerEndpoint.class);
    }
    
    /** 查找所有 MCP 服务端点重做数据。 */
    public Set<RedoData<McpServerEndpoint>> findMcpServerEndpointRedoData() {
        return super.findRedoData(McpServerEndpoint.class);
    }
    
    /** 获取指定 MCP 的服务端点重做数据。 */
    public McpServerEndpoint getMcpServerEndpoint(String mcpName) {
        RedoData<McpServerEndpoint> redoData = super.getRedoData(mcpName, McpServerEndpoint.class);
        return redoData == null ? null : redoData.get();
    }
    
    /** 构建 MCP 服务端点重做数据对象。 */
    private RedoData<McpServerEndpoint> buildMcpServerEndpointRedoData(String mcpName,
        String address, int port,
        String version) {
        McpServerEndpoint mcpServerEndpoint = new McpServerEndpoint(address, port, version);
        McpServerEndpointRedoData result = new McpServerEndpointRedoData(mcpName);
        result.set(mcpServerEndpoint);
        return result;
    }
    
    /** 缓存 Agent 端点注册信息以供重做。 */
    public void cachedAgentEndpointForRedo(String agentName, AgentEndpointWrapper wrapper) {
        AgentEndpointRedoData redoData = new AgentEndpointRedoData(agentName, wrapper);
        super.cachedRedoData(agentName, redoData, AgentEndpointWrapper.class);
    }
    
    /** 移除 Agent 端点的重做缓存。 */
    public void removeAgentEndpointForRedo(String agentName) {
        super.removeRedoData(agentName, AgentEndpointWrapper.class);
    }
    
    /** 标记 Agent 端点已成功注册。 */
    public void agentEndpointRegistered(String agentName) {
        super.dataRegistered(agentName, AgentEndpointWrapper.class);
    }
    
    /** 标记 Agent 端点待注销。 */
    public void agentEndpointDeregister(String agentName) {
        super.dataDeregister(agentName, AgentEndpointWrapper.class);
    }
    
    /** 标记 Agent 端点已成功注销。 */
    public void agentEndpointDeregistered(String agentName) {
        super.dataDeregistered(agentName, AgentEndpointWrapper.class);
    }
    
    /** 判断 Agent 端点是否已注册。 */
    public boolean isAgentEndpointRegistered(String agentName) {
        return super.isDataRegistered(agentName, AgentEndpointWrapper.class);
    }
    
    /** 查找所有 Agent 端点重做数据。 */
    public Set<RedoData<AgentEndpointWrapper>> findAgentEndpointRedoData() {
        return super.findRedoData(AgentEndpointWrapper.class);
    }
    
    /** 获取指定 Agent 的端点重做数据。 */
    public AgentEndpointWrapper getAgentEndpoint(String agentName) {
        RedoData<AgentEndpointWrapper> redoData =
            super.getRedoData(agentName, AgentEndpointWrapper.class);
        return redoData == null ? null : redoData.get();
    }
}
