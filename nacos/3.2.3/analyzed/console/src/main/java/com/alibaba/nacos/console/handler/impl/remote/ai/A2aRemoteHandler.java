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

package com.alibaba.nacos.console.handler.impl.remote.ai;

import com.alibaba.nacos.ai.constant.Constants;
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
import com.alibaba.nacos.console.handler.ai.A2aHandler;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.maintainer.client.ai.A2aMaintainerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A2A 远程 Handler：Console 部署模式下通过 AI Maintainer 客户端代理 A2A Agent 管理 API。
 * A2aRemoteHandler.
 *
 * @author KiteSoar
 */
@Service
@EnabledRemoteHandler
@EnabledAiHandler
public class A2aRemoteHandler implements A2aHandler {
    
    /** Maintainer 客户端持有者，提供 A2A 远程服务 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入 Maintainer 客户端持有者 */
    public A2aRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 注册 A2A Agent — 委托 AI Maintainer 客户端执行 */
    @Override
    public void registerAgent(AgentCard agentCard, AgentCardForm agentCardForm)
        throws NacosException {
        clientHolder.getAiMaintainerService().a2a()
            .registerAgent(agentCard, agentCardForm.getNamespaceId(),
                agentCardForm.getRegistrationType());
    }
    
    /** 查询 Agent Card 及版本列表 — 委托远程服务执行 */
    @Override
    public AgentCardDetailInfo getAgentCardWithVersions(AgentForm form) throws NacosException {
        return clientHolder.getAiMaintainerService().a2a()
            .getAgentCard(form.getAgentName(), form.getNamespaceId(), form.getRegistrationType());
    }
    
    /** 删除 Agent — 委托远程服务执行 */
    @Override
    public void deleteAgent(AgentForm form) throws NacosException {
        clientHolder.getAiMaintainerService().a2a().deleteAgent(form.getAgentName(),
            form.getNamespaceId());
    }
    
    /** 更新 Agent Card — 委托远程服务执行 */
    @Override
    public void updateAgentCard(AgentCard agentCard, AgentCardUpdateForm form)
        throws NacosException {
        clientHolder.getAiMaintainerService().a2a()
            .updateAgentCard(agentCard, form.getNamespaceId(), form.getSetAsLatest(),
                form.getRegistrationType());
    }
    
    /** 分页列出 Agent — 支持模糊搜索或精确列表 */
    @Override
    public Page<AgentCardVersionInfo> listAgents(AgentListForm agentListForm, PageForm pageForm)
        throws NacosException {
        A2aMaintainerService aiMaintainerService = clientHolder.getAiMaintainerService().a2a();
        return Constants.MCP_LIST_SEARCH_BLUR.equalsIgnoreCase(agentListForm.getSearch())
            ? aiMaintainerService.searchAgentCardsByName(agentListForm.getNamespaceId(),
                agentListForm.getAgentName(),
                pageForm.getPageNo(), pageForm.getPageSize())
            : aiMaintainerService.listAgentCards(agentListForm.getNamespaceId(),
                agentListForm.getAgentName(),
                pageForm.getPageNo(), pageForm.getPageSize());
    }
    
    /** 列出 Agent 全部版本 — 委托远程服务执行 */
    @Override
    public List<AgentVersionDetail> listAgentVersions(String namespaceId, String name)
        throws NacosException {
        return clientHolder.getAiMaintainerService().a2a().listAllVersionOfAgent(name, namespaceId);
    }
}
