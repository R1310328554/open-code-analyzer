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

import com.alibaba.nacos.api.ai.model.a2a.AgentEndpoint;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.ai.remote.AiGrpcClient;
import com.alibaba.nacos.client.naming.remote.gprc.redo.data.NamingRedoData;
import com.alibaba.nacos.client.redo.data.RedoData;
import com.alibaba.nacos.client.redo.service.AbstractRedoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nacos AI 模块定时重做任务。
 *
 * <p>继承 {@link AbstractRedoTask}，周期性重放 MCP 服务端点与 Agent 端点的注册/注销/移除操作，确保 gRPC 连接恢复后数据与服务端一致。</p>
 *
 * @author xiweng.yy
 */
public class AiRedoScheduledTask extends AbstractRedoTask<AiGrpcRedoService> {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(AiRedoScheduledTask.class);
    
    /** 关联的 AI gRPC 客户端，用于执行重做 RPC。 */
    private final AiGrpcClient aiGrpcClient;
    
    /**
     * 构造 AI 重做定时任务。
     *
     * @param redoService  重做服务
     * @param aiGrpcClient AI gRPC 客户端
     */
        super(LOGGER, redoService);
        this.aiGrpcClient = aiGrpcClient;
    }
    
    @Override
    /** 执行 MCP 与 Agent 端点的重做逻辑。 */
    protected void redoData() throws NacosException {
        try {
            redoForMcpSeverEndpoint();
            redoForAgentEndpoint();
        } catch (Exception e) {
            LOGGER.warn("Redo task run with unexpected exception: ", e);
        }
    }
    
    /** 遍历并重做所有 Agent 端点操作。 */
    private void redoForAgentEndpoint() {
        for (RedoData<AgentEndpointWrapper> each : getRedoService().findAgentEndpointRedoData()) {
            AgentEndpointRedoData redoData = (AgentEndpointRedoData) each;
            try {
                redoForAgentEndpoint(redoData);
            } catch (NacosException e) {
                LOGGER.error("Redo agent endpoint operation {} for {}} failed. ",
                    each.getRedoType(),
                    redoData.getAgentName(), e);
            }
        }
    }
    
    /** 根据重做类型执行单条 Agent 端点重做。 */
    private void redoForAgentEndpoint(AgentEndpointRedoData redoData) throws NacosException {
        NamingRedoData.RedoType redoType = redoData.getRedoType();
        String agentName = redoData.getAgentName();
        LOGGER.info("Redo agent endpoint operation {} for {}.", redoType, agentName);
        AgentEndpointWrapper wrapper = redoData.get();
        switch (redoType) {
            case REGISTER:
                if (!aiGrpcClient.isEnable()) {
                    return;
                }
                if (wrapper.isBatch()) {
                    aiGrpcClient.doRegisterAgentEndpoint(agentName, wrapper.getBatchData());
                } else {
                    aiGrpcClient.doRegisterAgentEndpoint(agentName, wrapper.getData());
                }
                break;
            case UNREGISTER:
                if (!aiGrpcClient.isEnable()) {
                    return;
                }
                AgentEndpoint endpoint =
                    wrapper.isBatch() ? wrapper.getBatchData().stream().findFirst().get()
                        : wrapper.getData();
                aiGrpcClient.doDeregisterAgentEndpoint(agentName, endpoint);
                break;
            case REMOVE:
                getRedoService().removeAgentEndpointForRedo(agentName);
                break;
            default:
        }
    }
    
    /** 遍历并重做所有 MCP 服务端点操作。 */
    private void redoForMcpSeverEndpoint() {
        for (RedoData<McpServerEndpoint> each : getRedoService().findMcpServerEndpointRedoData()) {
            McpServerEndpointRedoData redoData = (McpServerEndpointRedoData) each;
            try {
                redoForMcpServerEndpoint(redoData);
            } catch (NacosException e) {
                LOGGER.error("Redo mcp server endpoint operation {} for {}} failed. ",
                    each.getRedoType(),
                    redoData.getMcpName(), e);
            }
        }
    }
    
    /** 根据重做类型执行单条 MCP 服务端点重做。 */
    private void redoForMcpServerEndpoint(McpServerEndpointRedoData redoData)
        throws NacosException {
        NamingRedoData.RedoType redoType = redoData.getRedoType();
        String mcpName = redoData.getMcpName();
        LOGGER.info("Redo mcp server endpoint operation {} for {}.", redoType, mcpName);
        McpServerEndpoint endpoint = redoData.get();
        switch (redoType) {
            case REGISTER:
                if (!aiGrpcClient.isEnable()) {
                    return;
                }
                aiGrpcClient.doRegisterMcpServerEndpoint(mcpName, endpoint.getAddress(),
                    endpoint.getPort(),
                    endpoint.getVersion());
                break;
            case UNREGISTER:
                if (!aiGrpcClient.isEnable()) {
                    return;
                }
                aiGrpcClient.doDeregisterMcpServerEndpoint(mcpName, endpoint.getAddress(),
                    endpoint.getPort());
                break;
            case REMOVE:
                getRedoService().removeMcpServerEndpointForRedo(mcpName);
                break;
            default:
        }
    }
}
