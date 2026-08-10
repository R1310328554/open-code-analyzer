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
import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.listener.AbstractNacosAgentCardListener;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentEndpoint;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.utils.StringUtils;

import java.util.Collection;

/**
 * Nacos AI A2A（Agent-to-Agent）客户端服务接口。
 *
 * <p>提供 Agent Card 查询/发布、端点注册与订阅等 A2A 协议能力。</p>
 *
 * @author xiweng.yy
 */
public interface A2aService {
    
    /**
     * 获取最新版本 Agent Card 及 Nacos 扩展详情。
     *
     * @param agentName name of agent card
     * @return agent card with nacos extension detail
     * @throws NacosException if request parameter is invalid or agent card not found or handle error
     */
    @Since("3.1.0")
    default AgentCardDetailInfo getAgentCard(String agentName) throws NacosException {
        return getAgentCard(agentName, StringUtils.EMPTY);
    }
    
    /**
     * 按指定版本获取 Agent Card 及 Nacos 扩展详情。
     *
     * @param agentName name of agent card
     * @param version   target version, if null or empty, get latest version
     * @return agent card with nacos extension detail
     * @throws NacosException if request parameter is invalid or agent card not found or handle error
     */
    @Since("3.1.0")
    default AgentCardDetailInfo getAgentCard(String agentName, String version)
        throws NacosException {
        return getAgentCard(agentName, version, StringUtils.EMPTY);
    }
    
    /**
     * Get agent card with nacos extension detail with target version.
     *
     * @param agentName        name of agent card
     * @param version          target version, if null or empty, get latest version
     * @param registrationType {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_URL} or
     *                         {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_SERVICE} default is empty, means use agent card
     *                         setting in nacos.
     * @return agent card with nacos extension detail
     * @throws NacosException if request parameter is invalid or agent card not found or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    AgentCardDetailInfo getAgentCard(String agentName, String version, String registrationType)
        throws NacosException;
    
    /**
     * 以默认 SERVICE 类型端点发布新 Agent Card 或新版本。
     *
     * <p>
     * If current agent card and version exist, This API will do nothing. If current agent card exist but version not
     * exist, This API will release new version. If current t agent card not exist, This API will release new agent
     * card.
     * </p>
     *
     * @param agentCard agent card need to release
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.1.0")
    default void releaseAgentCard(AgentCard agentCard) throws NacosException {
        releaseAgentCard(agentCard, AiConstants.A2a.A2A_ENDPOINT_TYPE_SERVICE);
    }
    
    /**
     * 发布新 Agent Card 或新版本（可指定端点注册类型）。
     *
     * <p>
     * If current agent card and version exist, This API will do nothing. If current agent card exist but version not
     * exist, This API will release new version. If current t agent card not exist, This API will release new agent
     * card.
     * </p>
     *
     * @param agentCard        agent card need to release
     * @param registrationType {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_URL} or
     *                         {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_SERVICE}
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.1.0")
    default void releaseAgentCard(AgentCard agentCard, String registrationType)
        throws NacosException {
        releaseAgentCard(agentCard, registrationType, false);
    }
    
    /**
     * Release new agent card or new version.
     *
     * <p>
     * If current agent card and version exist, This API will do nothing. If current agent card exist but version not
     * exist, This API will release new version. If current t agent card not exist, This API will release new agent
     * card.
     * </p>
     *
     * @param agentCard        agent card need to release
     * @param registrationType {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_URL} or
     *                         {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_SERVICE}
     * @param setAsLatest      whether set new version as latest, default is false. This parameter is only effect when
     *                         new version is released. If current agent card not exist, whatever this parameter is, it
     *                         will be set as latest.
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    void releaseAgentCard(AgentCard agentCard, String registrationType, boolean setAsLatest)
        throws NacosException;
    
    /**
     * 向 Agent Card 注册服务端点。
     *
     * @param agentName name of agent
     * @param version   version of this endpoint
     * @param address   address for this endpoint
     * @param port      port of this endpoint
     * @throws NacosException if request parameter is invalid or handle error or agent not found
     */
    @Since("3.1.0")
    default void registerAgentEndpoint(String agentName, String version, String address, int port)
        throws NacosException {
        registerAgentEndpoint(agentName, version, address, port,
            AiConstants.A2a.A2A_ENDPOINT_DEFAULT_TRANSPORT);
    }
    
    /**
     * Register endpoint to agent card.
     *
     * @param agentName name of agent
     * @param version   version of this endpoint
     * @param address   address for this endpoint
     * @param port      port of this endpoint
     * @param transport supported transport, according to A2A protocol, it should be `JSONRPC`, `GRPC` and `HTTP+JSON`
     * @throws NacosException if request parameter is invalid or handle error or agent not found
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    default void registerAgentEndpoint(String agentName, String version, String address, int port,
        String transport)
        throws NacosException {
        registerAgentEndpoint(agentName, version, address, port, transport, StringUtils.EMPTY);
    }
    
    /**
     * Register endpoint to agent card.
     *
     * @param agentName name of agent
     * @param version   version of this endpoint
     * @param address   address for this endpoint
     * @param port      port of this endpoint
     * @param transport supported transport, according to A2A protocol, it should be `JSONRPC`, `GRPC` and `HTTP+JSON`
     * @param path      The path of endpoint request
     * @throws NacosException if request parameter is invalid or handle error or agent not found
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    default void registerAgentEndpoint(String agentName, String version, String address, int port,
        String transport,
        String path) throws NacosException {
        registerAgentEndpoint(agentName, version, address, port, transport, path, false);
    }
    
    /**
     * Register endpoint to agent card.
     *
     * @param agentName  name of agent
     * @param version    version of this endpoint
     * @param address    address for this endpoint
     * @param port       port of this endpoint
     * @param transport  supported transport, according to A2A protocol, it should be `JSONRPC`, `GRPC` and `HTTP+JSON`
     * @param path       The path of endpoint request
     * @param supportTls whether support tls
     * @throws NacosException if request parameter is invalid or handle error or agent not found
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    default void registerAgentEndpoint(String agentName, String version, String address, int port,
        String transport,
        String path, boolean supportTls) throws NacosException {
        AgentEndpoint agentEndpoint = new AgentEndpoint();
        agentEndpoint.setAddress(address);
        agentEndpoint.setPort(port);
        agentEndpoint.setTransport(transport);
        agentEndpoint.setPath(path);
        agentEndpoint.setSupportTls(supportTls);
        agentEndpoint.setVersion(version);
        registerAgentEndpoint(agentName, agentEndpoint);
    }
    
    /**
     * Register endpoint to agent card.
     *
     * @param agentName name of agent
     * @param endpoint  endpoint info
     * @throws NacosException if request parameter is invalid or handle error or agent not found
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    void registerAgentEndpoint(String agentName, AgentEndpoint endpoint) throws NacosException;
    
    /**
     * 批量注册 Agent Card 端点（会覆盖此前单条注册的端点）。
     *
     * <p>
     * Conflict with {@link #registerAgentEndpoint(String, AgentEndpoint)}, this API will overwrite all endpoint
     * registered by {@link #registerAgentEndpoint(String, AgentEndpoint)}.
     * </p>
     *
     * @param agentName name of agent
     * @param endpoints collection of endpoints
     * @throws NacosException if request parameter is invalid or handle error or agent not found
     * @since 3.1.1
     */
    @Since("3.1.1")
    void registerAgentEndpoint(String agentName, Collection<AgentEndpoint> endpoints)
        throws NacosException;
    
    /**
     * 注销本客户端注册的 Agent Card 端点。
     *
     * <p>
     * Only endpoint registered by this client can be deregistered. Other endpoint registered by other clients, call
     * this API will no any effect.
     * </p>
     *
     * @param agentName name of agent
     * @param version   version of this endpoint
     * @param address   address for this endpoint
     * @param port      port of this endpoint
     * @throws NacosException if request parameter is invalid or handle error or agent not found
     */
    @Since("3.1.0")
    default void deregisterAgentEndpoint(String agentName, String version, String address, int port)
        throws NacosException {
        AgentEndpoint agentEndpoint = new AgentEndpoint();
        agentEndpoint.setAddress(address);
        agentEndpoint.setPort(port);
        agentEndpoint.setVersion(version);
        deregisterAgentEndpoint(agentName, agentEndpoint);
    }
    
    /**
     * Deregister endpoint from agent card which registered by this client.
     *
     * <p>
     * Only endpoint registered by this client can be deregistered. Other endpoint registered by other clients, call
     * this API will no any effect.
     * </p>
     *
     * @param agentName name of agent
     * @param endpoint  endpoint info
     * @throws NacosException if request parameter is invalid or handle error or agent not found
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    void deregisterAgentEndpoint(String agentName, AgentEndpoint endpoint) throws NacosException;
    
    /**
     * 订阅 Agent Card 变更并接收回调。
     *
     * @param agentName         name of agent
     * @param agentCardListener the callback listener for agent card
     * @return current agent card when subscribe success
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.1.0")
    default AgentCardDetailInfo subscribeAgentCard(String agentName,
        AbstractNacosAgentCardListener agentCardListener)
        throws NacosException {
        return subscribeAgentCard(agentName, StringUtils.EMPTY, agentCardListener);
    }
    
    /**
     * Subscribe agent card.
     *
     * @param agentName         name of agent
     * @param version           version of agent, if empty or null, means subscribe latest version
     * @param agentCardListener the callback listener for agent card
     * @return current agent card when subscribe success, nullable if agent card not found
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    AgentCardDetailInfo subscribeAgentCard(String agentName, String version,
        AbstractNacosAgentCardListener agentCardListener) throws NacosException;
    
    /**
     * 取消 Agent Card 订阅。
     *
     * @param agentName         name of agent
     * @param agentCardListener the callback listener for agent card
     * @throws NacosException if request parameter is invalid or handle error
     */
    @Since("3.1.0")
    default void unsubscribeAgentCard(String agentName,
        AbstractNacosAgentCardListener agentCardListener)
        throws NacosException {
        unsubscribeAgentCard(agentName, StringUtils.EMPTY, agentCardListener);
    }
    
    /**
     * Unsubscribe agent card.
     *
     * @param agentName         name of agent
     * @param version           version of agent, if empty or null, means unsubscribe latest version
     * @param agentCardListener the callback listener for agent card
     * @throws NacosException if request parameter is invalid or handle error
      * <p>Nacos 能力/AI API 模块；详见上方英文说明。</p>
     */
    @Since("3.1.0")
    void unsubscribeAgentCard(String agentName, String version,
        AbstractNacosAgentCardListener agentCardListener)
        throws NacosException;
}
