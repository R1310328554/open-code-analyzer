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

package com.alibaba.nacos.console.handler.impl.inner.ai;

import com.alibaba.nacos.ai.form.prompt.PromptForm;
import com.alibaba.nacos.ai.form.prompt.PromptHistoryForm;
import com.alibaba.nacos.ai.form.prompt.PromptListForm;
import com.alibaba.nacos.ai.service.prompt.PromptOperationService;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaSummary;
import com.alibaba.nacos.api.ai.model.prompt.PromptVariable;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.ai.PromptHandler;
import com.alibaba.nacos.console.handler.impl.inner.EnabledInnerHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Prompt 控制台内嵌 Handler：草稿、提审、发布与元数据维护均委托 {@link PromptOperationService}。
 * Prompt inner handler implementation.
 *
 * @author nacos
 */
@Component
@EnabledInnerHandler
@EnabledAiHandler
public class PromptInnerHandler implements PromptHandler {
    
    /** Prompt 运维服务，封装全生命周期操作 */
    private final PromptOperationService promptOperationService;
    
    /** 注入 Prompt 运维服务 */
    public PromptInnerHandler(PromptOperationService promptOperationService) {
        this.promptOperationService = promptOperationService;
    }
    
    // ========== 通用 API：删除、列表与版本历史 ==========
    
    /** 删除指定 Prompt 及其全部版本 */
    @Override
    public boolean deletePrompt(PromptForm form, String srcUser, String srcIp)
        throws NacosException {
        promptOperationService.deletePrompt(form.getNamespaceId(), form.getPromptKey());
        return true;
    }
    
    /** 分页列出 Prompt 元数据摘要，支持关键字与业务标签过滤 */
    @Override
    public Page<PromptMetaSummary> listPrompts(PromptListForm form) throws NacosException {
        return promptOperationService.listPrompts(form.getNamespaceId(), form.getPromptKey(),
            form.getSearch(),
            form.getBizTags(), form.getPageNo(), form.getPageSize());
    }
    
    /** 分页列出指定 Prompt 的版本历史摘要 */
    @Override
    public Page<PromptVersionSummary> listPromptVersions(PromptHistoryForm form)
        throws NacosException {
        return promptOperationService.listPromptVersions(form.getNamespaceId(), form.getPromptKey(),
            form.getPageNo(), form.getPageSize());
    }
    
    // ========== 生命周期 API：治理详情、草稿、提审与发布 ==========
    
    /** 获取 Prompt 治理端元数据详情（含标签、描述等） */
    @Override
    public PromptMetaInfo getPromptGovernanceDetail(String namespaceId, String promptKey)
        throws NacosException {
        return promptOperationService.getPromptDetail(namespaceId, promptKey);
    }
    
    /** 获取指定版本的 Prompt 完整内容 */
    @Override
    public PromptVersionInfo getVersionDetail(String namespaceId, String promptKey, String version)
        throws NacosException {
        return promptOperationService.getPromptVersionDetail(namespaceId, promptKey, version);
    }
    
    /** 下载指定版本 Prompt 内容（供导出或离线编辑） */
    @Override
    public PromptVersionInfo downloadPromptVersion(String namespaceId, String promptKey,
        String version)
        throws NacosException {
        return promptOperationService.downloadPromptVersion(namespaceId, promptKey, version);
    }
    
    /** 基于已有版本创建新草稿，含模板、变量与业务标签 */
    @Override
    public String createDraft(String namespaceId, String promptKey, String basedOnVersion,
        String targetVersion,
        String template, List<PromptVariable> variables, String commitMsg, String description,
        String bizTags)
        throws NacosException {
        return promptOperationService.createDraft(namespaceId, promptKey, basedOnVersion,
            targetVersion, template,
            variables, commitMsg, description, bizTags);
    }
    
    /** 更新当前草稿的模板内容与变量 */
    @Override
    public void updateDraft(String namespaceId, String promptKey, String template,
        List<PromptVariable> variables,
        String commitMsg) throws NacosException {
        promptOperationService.updateDraft(namespaceId, promptKey, template, variables, commitMsg);
    }
    
    /** 删除当前草稿版本 */
    @Override
    public void deleteDraft(String namespaceId, String promptKey) throws NacosException {
        promptOperationService.deleteDraft(namespaceId, promptKey);
    }
    
    /** 提交版本进入流水线审核 */
    @Override
    public String submit(String namespaceId, String promptKey, String version)
        throws NacosException {
        return promptOperationService.submit(namespaceId, promptKey, version);
    }
    
    /** 发布已通过审核的版本，可选更新 latest 标签 */
    @Override
    public void publish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        promptOperationService.publish(namespaceId, promptKey, version, updateLatestLabel);
    }
    
    /** 强制发布版本，跳过流水线校验 */
    @Override
    public void forcePublish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        promptOperationService.forcePublish(namespaceId, promptKey, version, updateLatestLabel);
    }
    
    /** 将已审核版本重新编辑为草稿状态 */
    @Override
    public void redraft(String namespaceId, String promptKey, String version)
        throws NacosException {
        promptOperationService.redraft(namespaceId, promptKey, version);
    }
    
    /** 切换 Prompt 版本上线/下线状态 */
    @Override
    public void changeOnlineStatus(String namespaceId, String promptKey, String version,
        boolean online)
        throws NacosException {
        promptOperationService.changeOnlineStatus(namespaceId, promptKey, version, online);
    }
    
    /** 更新运行时路由标签，不改变版本状态 */
    @Override
    public void updateLabels(String namespaceId, String promptKey, Map<String, String> labels)
        throws NacosException {
        promptOperationService.updateLabels(namespaceId, promptKey, labels);
    }
    
    /** 更新 Prompt 描述信息 */
    @Override
    public void updateDescription(String namespaceId, String promptKey, String description)
        throws NacosException {
        promptOperationService.updateDescription(namespaceId, promptKey, description);
    }
    
    /** 更新 Prompt 业务标签 */
    @Override
    public void updateBizTags(String namespaceId, String promptKey, String bizTags)
        throws NacosException {
        promptOperationService.updateBizTags(namespaceId, promptKey, bizTags);
    }
}
