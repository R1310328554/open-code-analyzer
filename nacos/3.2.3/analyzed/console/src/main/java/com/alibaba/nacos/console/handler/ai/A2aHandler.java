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

package com.alibaba.nacos.console.handler.ai;

import com.alibaba.nacos.ai.form.a2a.admin.AgentCardForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentCardUpdateForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentListForm;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardVersionInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentVersionDetail;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.core.model.form.PageForm;

import java.util.List;

/**
 * Agent-to-Agent（A2A）控制台处理器接口：注册、查询、更新与删除 Agent 及其 Card 版本。
 * A2a handler.
 *
 * @author KiteSoar
 */
public interface A2aHandler {
    
    /**
     * 注册 Agent 及其 Agent Card。
     * Register agent.
     *
     * @param agentCard     待注册的 Agent Card 实体
     * @param agentCardForm Agent Card 表单参数
     * @throws NacosException Nacos 业务异常
     */
    void registerAgent(AgentCard agentCard, AgentCardForm agentCardForm) throws NacosException;
    
    /**
     * 查询 Agent Card 详情及关联版本列表。
     * Get agent card with versions.
     *
     * @param form Agent 定位表单
     * @return 含版本信息的 Agent Card 详情
     * @throws NacosException Nacos 业务异常
     */
    AgentCardDetailInfo getAgentCardWithVersions(AgentForm form) throws NacosException;
    
    /**
     * 删除指定 Agent。
     * Delete agent.
     *
     * @param form Agent 定位表单
     * @throws NacosException Nacos 业务异常
     */
    void deleteAgent(AgentForm form) throws NacosException;
    
    /**
     * 更新 Agent Card 内容与元数据。
     * Update agent card.
     *
     * @param agentCard 待更新的 Agent Card
     * @param form      Agent 更新表单
     * @throws NacosException Nacos 业务异常
     */
    void updateAgentCard(AgentCard agentCard, AgentCardUpdateForm form) throws NacosException;
    
    /**
     * 分页列出 Agent 及其 Card 版本摘要。
     * List agents.
     *
     * @param agentListForm Agent 列表查询表单
     * @param pageForm 分页参数
     * @return Agent Card 版本分页结果
     * @throws NacosException Nacos 业务异常
     */
    Page<AgentCardVersionInfo> listAgents(AgentListForm agentListForm, PageForm pageForm)
        throws NacosException;
    
    /**
     * 列出指定 Agent 的全部版本详情。
     * List agent versions.
     * @param namespaceId 目标 Agent 所在命名空间 ID
     * @param name        目标 Agent 名称
     * @return Agent 版本详情列表
     * @throws NacosException Nacos 业务异常
     */
    List<AgentVersionDetail> listAgentVersions(String namespaceId, String name)
        throws NacosException;
}
