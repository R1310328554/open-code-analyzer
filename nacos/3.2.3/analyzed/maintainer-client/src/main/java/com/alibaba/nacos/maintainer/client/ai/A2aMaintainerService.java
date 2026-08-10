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
 *
 */

package com.alibaba.nacos.maintainer.client.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardVersionInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentVersionDetail;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.List;

/**
 * A2A（Agent-to-Agent）维护服务接口：管理 Agent Card 的注册、查询、更新与删除。
 *
 * <p>提供默认命名空间与注册类型（URL/Service）的重载，支持版本化 Agent Card 与分页检索。</p>
 *
 * @author nacos
 */
public interface A2aMaintainerService {
    
    /**
     * 在默认命名空间注册 Agent。
     *
     * @param agentCard the agent card detail to register
     * @return true if the agent is registered successfully, false otherwise
     * @throws NacosException if the agent registration fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default boolean registerAgent(AgentCard agentCard) throws NacosException {
        return registerAgent(agentCard, AiConstants.A2a.A2A_DEFAULT_NAMESPACE);
    }
    
    /**
     * 在指定命名空间注册 Agent。
     *
     * @param agentCard the agent card detail to register
     * @param namespaceId the namespace id
     * @return true if the agent is registered successfully, false otherwise
     * @throws NacosException if the agent registration fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default boolean registerAgent(AgentCard agentCard, String namespaceId) throws NacosException {
        return registerAgent(agentCard, namespaceId, AiConstants.A2a.A2A_ENDPOINT_TYPE_URL);
    }
    
    /**
     * Register agent.
     *
     * @param agentCard the agent card detail to register
     * @param namespaceId the namespace id
     * @param registrationType {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_URL} or {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_SERVICE}
     * @return true if the agent is registered successfully, false otherwise
     * @throws NacosException if the agent registration fails due to invalid input or internal error
      * <p>A2A Agent 维护 API；详见接口说明。</p>
     */
    @Since("3.1.0")
    boolean registerAgent(AgentCard agentCard, String namespaceId, String registrationType)
        throws NacosException;
    
    /**
     * 从默认命名空间获取 Agent Card。
     *
     * @param agentName   the agent name
     * @return agent card
     * @throws NacosException if the agent get fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default AgentCardDetailInfo getAgentCard(String agentName) throws NacosException {
        return getAgentCard(agentName, AiConstants.A2a.A2A_DEFAULT_NAMESPACE);
    }
    
    /**
     * 获取 Agent Card（可指定命名空间、注册类型与版本）。
     *
     * @param agentName   the agent name
     * @param namespaceId the namespace id
     * @return agent card
     * @throws NacosException if the agent get fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default AgentCardDetailInfo getAgentCard(String agentName, String namespaceId)
        throws NacosException {
        return getAgentCard(agentName, namespaceId, StringUtils.EMPTY);
    }
    
    /**
     * Get agent card.
     *
     * @param agentName   the agent name
     * @param namespaceId the namespace id
     * @param registrationType {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_URL} or {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_SERVICE}
     * @return agent card
     * @throws NacosException if the agent get fails due to invalid input or internal error
      * <p>A2A Agent 维护 API；详见接口说明。</p>
     */
    @Since("3.1.0")
    default AgentCardDetailInfo getAgentCard(String agentName, String namespaceId,
        String registrationType)
        throws NacosException {
        return getAgentCard(agentName, namespaceId, registrationType, StringUtils.EMPTY);
    }
    
    /**
     * 按版本获取 Agent Card 详情。
     *
     * @param agentName        the agent name
     * @param namespaceId      the namespace id
     * @param registrationType the registration type
     * @param version          the version
     * @return the agent card
     * @throws NacosException the nacos exception
     */
    @Since("3.1.2")
    AgentCardDetailInfo getAgentCard(String agentName, String namespaceId, String registrationType,
        String version)
        throws NacosException;
    
    /**
     * 在默认命名空间更新 Agent Card。
     *
     * @param agentCard the agent card detail to update
     * @return true if the agent is updated successfully, false otherwise
     * @throws NacosException if the agent update fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default boolean updateAgentCard(AgentCard agentCard) throws NacosException {
        return updateAgentCard(agentCard, AiConstants.A2a.A2A_DEFAULT_NAMESPACE);
    }
    
    /**
     * 更新 Agent Card 并设为最新版本。
     *
     * @param agentCard the agent card detail to update
     * @param namespaceId the namespace id
     * @return true if the agent is updated successfully, false otherwise
     * @throws NacosException if the agent update fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default boolean updateAgentCard(AgentCard agentCard, String namespaceId) throws NacosException {
        return updateAgentCard(agentCard, namespaceId, true);
    }
    
    /**
     * 更新 Agent Card（可控制是否设为 latest）。
     *
     * @param agentCard the agent card detail to update
     * @param namespaceId the namespace id
     * @param setAsLatest whether set as latest version
     * @return true if the agent is updated successfully, false otherwise
     * @throws NacosException if the agent update fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default boolean updateAgentCard(AgentCard agentCard, String namespaceId, boolean setAsLatest)
        throws NacosException {
        return updateAgentCard(agentCard, namespaceId, setAsLatest, StringUtils.EMPTY);
    }
    
    /**
     * Update agent card.
     *
     * @param agentCard         the agent card detail to update
     * @param namespaceId       the namespace id
     * @param setAsLatest       whether set as latest version
     * @param registrationType  {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_URL} or {@link AiConstants.A2a#A2A_ENDPOINT_TYPE_SERVICE}
     * @return true if the agent is updated successfully, false otherwise
     * @throws NacosException if the agent update fails due to invalid input or internal error
      * <p>A2A Agent 维护 API；详见接口说明。</p>
     */
    @Since("3.1.0")
    boolean updateAgentCard(AgentCard agentCard, String namespaceId, boolean setAsLatest,
        String registrationType)
        throws NacosException;
    
    /**
     * 从默认命名空间删除 Agent。
     *
     * @param agentName   the agent name
     * @return true if the agent is deleted successfully, false otherwise
     * @throws NacosException if the agent delete fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default boolean deleteAgent(String agentName) throws NacosException {
        return deleteAgent(agentName, AiConstants.A2a.A2A_DEFAULT_NAMESPACE);
    }
    
    /**
     * 删除指定命名空间下的 Agent。
     *
     * @param agentName   the agent name
     * @param namespaceId the namespace id
     * @return true if the agent is deleted successfully, false otherwise
     * @throws NacosException if the agent delete fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default boolean deleteAgent(String agentName, String namespaceId) throws NacosException {
        return deleteAgent(agentName, namespaceId, StringUtils.EMPTY);
    }
    
    /**
     * 删除 Agent 指定版本；version 为空时删除全部版本。
     *
     * @param agentName   the agent name
     * @param namespaceId the namespace id
     * @param version     the version of agent card, if empty or null, delete all agent versions
     * @return true if the agent is deleted successfully, false otherwise
     * @throws NacosException if the agent delete fails due to invalid input or internal error
     */
    @Since("3.1.0")
    boolean deleteAgent(String agentName, String namespaceId, String version) throws NacosException;
    
    /**
     * 列出目标 Agent 的全部版本信息。
     *
     * @param agentName agent name
     * @return list of agent versions
     * @throws NacosException if the agent version query fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default List<AgentVersionDetail> listAllVersionOfAgent(String agentName) throws NacosException {
        return listAllVersionOfAgent(agentName, AiConstants.A2a.A2A_DEFAULT_NAMESPACE);
    }
    
    /**
     * List all versions for target agent.
     *
     * @param agentName agent name
     * @param namespaceId the namespace id
     * @return list of agent versions
     * @throws NacosException if the agent version query fails due to invalid input or internal error
      * <p>A2A Agent 维护 API；详见接口说明。</p>
     */
    @Since("3.1.0")
    List<AgentVersionDetail> listAllVersionOfAgent(String agentName, String namespaceId)
        throws NacosException;
    
    /**
     * 在默认命名空间按名称模糊搜索 Agent Card（最多 100 条）。
     *
     * @param agentNamePattern agent name pattern
     * @return page of agent cards
     * @throws NacosException if the agent search fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default Page<AgentCardVersionInfo> searchAgentCardsByName(String agentNamePattern)
        throws NacosException {
        return searchAgentCardsByName(agentNamePattern, 1, 100);
    }
    
    /**
     * 在默认命名空间分页搜索 Agent Card。
     *
     * @param agentNamePattern  agent name pattern
     * @param pageNo            page number
     * @param pageSize          size per page
     * @return page of agent cards
     * @throws NacosException if the agent search fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default Page<AgentCardVersionInfo> searchAgentCardsByName(String agentNamePattern, int pageNo,
        int pageSize)
        throws NacosException {
        return searchAgentCardsByName(AiConstants.A2a.A2A_DEFAULT_NAMESPACE, agentNamePattern,
            pageNo, pageSize);
    }
    
    /**
     * 在目标命名空间分页搜索 Agent Card。
     *
     * @param namespaceId       namespace id
     * @param agentNamePattern  agent name pattern
     * @param pageNo            page number
     * @param pageSize          size per page
     * @return page of agent cards
     * @throws NacosException if the agent search fails due to invalid input or internal error
     */
    @Since("3.1.0")
    Page<AgentCardVersionInfo> searchAgentCardsByName(String namespaceId, String agentNamePattern,
        int pageNo,
        int pageSize) throws NacosException;
    
    /**
     * 列出默认命名空间 Agent Card（最多 100 条）。
     *
     * @return page of agent cards
     * @throws NacosException if the agent list fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default Page<AgentCardVersionInfo> listAgentCards() throws NacosException {
        return listAgentCards(1, 100);
    }
    
    /**
     * 在默认命名空间分页列出 Agent Card。
     *
     * @param pageNo        page number
     * @param pageSize      size per page
     * @return page of agent cards
     * @throws NacosException if the agent list fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default Page<AgentCardVersionInfo> listAgentCards(int pageNo, int pageSize)
        throws NacosException {
        return listAgentCards(AiConstants.A2a.A2A_DEFAULT_NAMESPACE, pageNo, pageSize);
    }
    
    /**
     * 在目标命名空间分页列出 Agent Card。
     *
     * @param namespaceId   namespace id
     * @param pageNo        page number
     * @param pageSize      size per page
     * @return page of agent cards
     * @throws NacosException if the agent list fails due to invalid input or internal error
     */
    @Since("3.1.0")
    default Page<AgentCardVersionInfo> listAgentCards(String namespaceId, int pageNo, int pageSize)
        throws NacosException {
        return listAgentCards(namespaceId, StringUtils.EMPTY, pageNo, pageSize);
    }
    
    /**
     * 在目标命名空间按精确名称分页列出 Agent Card。
     *
     * @param namespaceId   namespace id
     * @param agentName     agent name, if empty or null, list all agent cards
     * @param pageNo        page number
     * @param pageSize      size per page
     * @return page of agent cards
     * @throws NacosException if the agent list fails due to invalid input or internal error
     */
    @Since("3.1.0")
    Page<AgentCardVersionInfo> listAgentCards(String namespaceId, String agentName, int pageNo,
        int pageSize)
        throws NacosException;
}
