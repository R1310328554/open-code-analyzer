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

package com.alibaba.nacos.console.proxy.ai;

import com.alibaba.nacos.ai.form.AiResourceFilterableForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecDraftCreateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecBizTagsUpdateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecLabelsUpdateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecListForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecOnlineForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecPublishForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecScopeForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecSubmitForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecUpdateForm;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecMeta;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.ai.AgentSpecHandler;
import com.alibaba.nacos.core.model.form.PageForm;
import org.springframework.stereotype.Component;

/**
 * AgentSpec 代理：将 Agent 规格草稿、发布、上下线与 ZIP 导入等全生命周期操作委托给 {@link AgentSpecHandler}。
 * AgentSpec proxy.
 *
 * @author nacos
 */
@Component
public class AgentSpecProxy {
    
    /** AgentSpec Handler 实现 */
    private final AgentSpecHandler agentSpecHandler;
    
    /** 注入 AgentSpec Handler。 */
    public AgentSpecProxy(AgentSpecHandler agentSpecHandler) {
        this.agentSpecHandler = agentSpecHandler;
    }
    
    /** 获取 AgentSpec 元信息（最新或指定版本）。 */
    public AgentSpecMeta getAgentSpec(AgentSpecForm form) throws NacosException {
        return agentSpecHandler.getAgentSpec(form);
    }
    
    /** 获取 AgentSpec 指定版本完整内容。 */
    public AgentSpec getAgentSpecVersion(AgentSpecForm form) throws NacosException {
        return agentSpecHandler.getAgentSpecVersion(form);
    }
    
    /** 删除 AgentSpec 及其全部版本。 */
    public void deleteAgentSpec(AgentSpecForm form) throws NacosException {
        agentSpecHandler.deleteAgentSpec(form);
    }
    
    /** 分页列出 AgentSpec，支持资源过滤。 */
    public Page<AgentSpecSummary> listAgentSpecs(AgentSpecListForm agentSpecListForm,
        AiResourceFilterableForm filterableForm, PageForm pageForm) throws NacosException {
        return agentSpecHandler.listAgentSpecs(agentSpecListForm, filterableForm, pageForm);
    }
    
    /** 从 ZIP 包上传 AgentSpec（默认不覆盖）。 */
    public String uploadAgentSpecFromZip(String namespaceId, byte[] zipBytes)
        throws NacosException {
        return uploadAgentSpecFromZip(namespaceId, zipBytes, false);
    }
    
    /** 从 ZIP 包上传 AgentSpec，可指定是否覆盖已有资源。 */
    public String uploadAgentSpecFromZip(String namespaceId, byte[] zipBytes, boolean overwrite)
        throws NacosException {
        return agentSpecHandler.uploadAgentSpecFromZip(namespaceId, zipBytes, overwrite);
    }
    
    /** 创建 AgentSpec 草稿。 */
    public String createDraft(AgentSpecDraftCreateForm form) throws NacosException {
        return agentSpecHandler.createDraft(form);
    }
    
    /** 更新 AgentSpec 草稿内容。 */
    public void updateDraft(AgentSpecUpdateForm form) throws NacosException {
        agentSpecHandler.updateDraft(form);
    }
    
    /** 删除 AgentSpec 草稿。 */
    public void deleteDraft(AgentSpecForm form) throws NacosException {
        agentSpecHandler.deleteDraft(form);
    }
    
    /** 提交 AgentSpec 审核。 */
    public String submit(AgentSpecSubmitForm form) throws NacosException {
        return agentSpecHandler.submit(form);
    }
    
    /** 发布 AgentSpec 版本。 */
    public void publish(AgentSpecPublishForm form) throws NacosException {
        agentSpecHandler.publish(form);
    }
    
    /** 强制发布 AgentSpec 版本（跳过审核约束）。 */
    public void forcePublish(AgentSpecPublishForm form) throws NacosException {
        agentSpecHandler.forcePublish(form);
    }
    
    /** 将已发布版本退回草稿状态。 */
    public void redraft(AgentSpecPublishForm form) throws NacosException {
        agentSpecHandler.redraft(form);
    }
    
    /** 更新 AgentSpec 标签。 */
    public void updateLabels(AgentSpecLabelsUpdateForm form) throws NacosException {
        agentSpecHandler.updateLabels(form);
    }
    
    /** 更新 AgentSpec 业务标签。 */
    public void updateBizTags(AgentSpecBizTagsUpdateForm form) throws NacosException {
        agentSpecHandler.updateBizTags(form);
    }
    
    /** 切换 AgentSpec 上下线状态。 */
    public void changeOnlineStatus(AgentSpecOnlineForm form, boolean online) throws NacosException {
        agentSpecHandler.changeOnlineStatus(form, online);
    }
    
    /** 更新 AgentSpec 可见范围。 */
    public void updateScope(AgentSpecScopeForm form) throws NacosException {
        agentSpecHandler.updateScope(form);
    }
    
    /** 将 AgentSpec 设为上线。 */
    public void online(AgentSpecOnlineForm form) throws NacosException {
        agentSpecHandler.changeOnlineStatus(form, true);
    }
    
    /** 将 AgentSpec 设为下线。 */
    public void offline(AgentSpecOnlineForm form) throws NacosException {
        agentSpecHandler.changeOnlineStatus(form, false);
    }
}
