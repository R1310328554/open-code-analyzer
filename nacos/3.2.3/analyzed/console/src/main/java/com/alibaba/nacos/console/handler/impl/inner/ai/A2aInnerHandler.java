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

package com.alibaba.nacos.console.handler.impl.inner.ai;

import com.alibaba.nacos.ai.form.a2a.admin.AgentCardForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentCardUpdateForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentListForm;
import com.alibaba.nacos.ai.service.a2a.A2aServerOperationService;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardVersionInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentVersionDetail;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.ai.A2aHandler;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.impl.inner.EnabledInnerHandler;
import com.alibaba.nacos.core.model.form.PageForm;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A2A（Agent-to-Agent）Inner Handler：委托 {@link A2aServerOperationService} 完成 Agent 注册与 Card 管理。
 * A2a inner handler.
 *
 * @author KiteSoar
 */
@Component
@EnabledInnerHandler
@EnabledAiHandler
public class A2aInnerHandler implements A2aHandler {
    
    /** A2A 服务端操作服务 */
    private final A2aServerOperationService a2aServerOperationService;
    
    /** 注入 A2A 服务端操作服务 */
    public A2aInnerHandler(A2aServerOperationService a2aServerOperationService) {
        this.a2aServerOperationService = a2aServerOperationService;
    }
    
    /** 注册 Agent 及其 Agent Card。 */
    @Override
    public void registerAgent(AgentCard agentCard, AgentCardForm agentCardForm)
        throws NacosException {
        a2aServerOperationService.registerAgent(agentCard, agentCardForm.getNamespaceId(),
            agentCardForm.getRegistrationType());
    }
    
    /** 查询 Agent Card 详情及版本列表。 */
    @Override
    public AgentCardDetailInfo getAgentCardWithVersions(AgentForm form) throws NacosException {
        return a2aServerOperationService.getAgentCard(form.getNamespaceId(), form.getAgentName(),
            form.getVersion(),
            form.getRegistrationType());
    }
    
    /** 删除指定 Agent。 */
    @Override
    public void deleteAgent(AgentForm form) throws NacosException {
        a2aServerOperationService.deleteAgent(form.getNamespaceId(), form.getAgentName(),
            form.getVersion());
    }
    
    /** 更新 Agent Card 内容与元数据。 */
    @Override
    public void updateAgentCard(AgentCard agentCard, AgentCardUpdateForm form)
        throws NacosException {
        a2aServerOperationService.updateAgentCard(agentCard, form.getNamespaceId(),
            form.getRegistrationType(),
            form.getSetAsLatest());
    }
    
    /** 分页列举 Agent 及其 Card 版本摘要。 */
    @Override
    public Page<AgentCardVersionInfo> listAgents(AgentListForm agentListForm, PageForm pageForm)
        throws NacosException {
        return a2aServerOperationService.listAgents(agentListForm.getNamespaceId(),
            agentListForm.getAgentName(),
            agentListForm.getSearch(), pageForm.getPageNo(), pageForm.getPageSize());
    }
    
    /** 列举指定 Agent 的全部版本详情。 */
    @Override
    public List<AgentVersionDetail> listAgentVersions(String namespaceId, String name)
        throws NacosException {
        return a2aServerOperationService.listAgentVersions(namespaceId, name);
    }
}
