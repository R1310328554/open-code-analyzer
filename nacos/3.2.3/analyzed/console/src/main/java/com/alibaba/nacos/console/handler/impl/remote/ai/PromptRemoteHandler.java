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

package com.alibaba.nacos.console.handler.impl.remote.ai;

import com.alibaba.nacos.ai.form.prompt.PromptForm;
import com.alibaba.nacos.ai.form.prompt.PromptHistoryForm;
import com.alibaba.nacos.ai.form.prompt.PromptListForm;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaSummary;
import com.alibaba.nacos.api.ai.model.prompt.PromptVariable;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.ai.PromptHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Prompt 远程 Handler：草稿、提审、发布与元数据维护均通过 {@link NacosMaintainerClientHolder} 调用远端 AI 运维 API。
 * Remote implementation of Prompt handler.
 *
 * <p>Calls remote Nacos server through maintainer client for Prompt operations.</p>
 *
 * @author nacos
 */
@Service
@EnabledRemoteHandler
@EnabledAiHandler
public class PromptRemoteHandler implements PromptHandler {
    
    /** 运维客户端持有者，提供 AI Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public PromptRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    // ========== 通用 API：删除、列表与版本历史 ==========
    
    /** 删除远端指定 Prompt 及其全部版本。 */
    @Override
    public boolean deletePrompt(PromptForm form, String srcUser, String srcIp)
        throws NacosException {
        return clientHolder.getAiMaintainerService().prompt().deletePrompt(form.getNamespaceId(),
            form.getPromptKey());
    }
    
    /** 分页列出远端 Prompt 元数据摘要，支持关键字与业务标签过滤。 */
    @Override
    public Page<PromptMetaSummary> listPrompts(PromptListForm form) throws NacosException {
        return clientHolder.getAiMaintainerService().prompt().listPrompts(form.getNamespaceId(),
            form.getPromptKey(),
            form.getSearch(), form.getBizTags(), form.getPageNo(), form.getPageSize());
    }
    
    /** 分页列出远端指定 Prompt 的版本历史摘要。 */
    @Override
    public Page<PromptVersionSummary> listPromptVersions(PromptHistoryForm form)
        throws NacosException {
        return clientHolder.getAiMaintainerService().prompt().listPromptVersions(
            form.getNamespaceId(),
            form.getPromptKey(), form.getPageNo(), form.getPageSize());
    }
    
    // ========== 生命周期 API：治理详情、草稿、提审与发布 ==========
    
    /** 获取远端 Prompt 治理端元数据详情（含标签、描述等）。 */
    @Override
    public PromptMetaInfo getPromptGovernanceDetail(String namespaceId, String promptKey)
        throws NacosException {
        return clientHolder.getAiMaintainerService().prompt().getPromptGovernanceDetail(namespaceId,
            promptKey);
    }
    
    /** 获取远端指定版本的 Prompt 完整内容。 */
    @Override
    public PromptVersionInfo getVersionDetail(String namespaceId, String promptKey, String version)
        throws NacosException {
        return clientHolder.getAiMaintainerService().prompt().getVersionDetail(namespaceId,
            promptKey, version);
    }
    
    /** 下载远端指定版本 Prompt 内容（委托 getVersionDetail，下载计数在目标服务端统计）。 */
    @Override
    public PromptVersionInfo downloadPromptVersion(String namespaceId, String promptKey,
        String version)
        throws NacosException {
        // 远程 Handler 委托 getVersionDetail；下载次数由目标服务端统计。
        return clientHolder.getAiMaintainerService().prompt().getVersionDetail(namespaceId,
            promptKey, version);
    }
    
    /** 在远端基于已有版本创建新草稿，含模板、变量与业务标签。 */
    @Override
    public String createDraft(String namespaceId, String promptKey, String basedOnVersion,
        String targetVersion,
        String template, List<PromptVariable> variables, String commitMsg, String description,
        String bizTags)
        throws NacosException {
        String variablesJson = variables != null ? JacksonUtils.toJson(variables) : null;
        return clientHolder.getAiMaintainerService().prompt().createDraft(namespaceId, promptKey,
            basedOnVersion,
            targetVersion, template, variablesJson, commitMsg, description, bizTags);
    }
    
    /** 更新远端当前草稿的模板内容与变量。 */
    @Override
    public void updateDraft(String namespaceId, String promptKey, String template,
        List<PromptVariable> variables,
        String commitMsg) throws NacosException {
        String variablesJson = variables != null ? JacksonUtils.toJson(variables) : null;
        clientHolder.getAiMaintainerService().prompt().updateDraft(namespaceId, promptKey, template,
            variablesJson,
            commitMsg);
    }
    
    /** 删除远端当前草稿版本。 */
    @Override
    public void deleteDraft(String namespaceId, String promptKey) throws NacosException {
        clientHolder.getAiMaintainerService().prompt().deleteDraft(namespaceId, promptKey);
    }
    
    /** 提交远端 Prompt 版本进入流水线审核。 */
    @Override
    public String submit(String namespaceId, String promptKey, String version)
        throws NacosException {
        return clientHolder.getAiMaintainerService().prompt().submit(namespaceId, promptKey,
            version);
    }
    
    /** 发布远端已通过审核的 Prompt 版本，可选更新 latest 标签。 */
    @Override
    public void publish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        clientHolder.getAiMaintainerService().prompt().publish(namespaceId, promptKey, version,
            updateLatestLabel);
    }
    
    /** 强制发布远端 Prompt 版本，跳过流水线校验。 */
    @Override
    public void forcePublish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        clientHolder.getAiMaintainerService().prompt().forcePublish(namespaceId, promptKey, version,
            updateLatestLabel);
    }
    
    /** 将远端已审核 Prompt 版本重新编辑为草稿状态。 */
    @Override
    public void redraft(String namespaceId, String promptKey, String version)
        throws NacosException {
        clientHolder.getAiMaintainerService().prompt().redraft(namespaceId, promptKey, version);
    }
    
    /** 切换远端 Prompt 版本上线/下线状态。 */
    @Override
    public void changeOnlineStatus(String namespaceId, String promptKey, String version,
        boolean online)
        throws NacosException {
        clientHolder.getAiMaintainerService().prompt().changeOnlineStatus(namespaceId, promptKey,
            version, online);
    }
    
    /** 更新远端 Prompt 运行时路由标签，不改变版本状态。 */
    @Override
    public void updateLabels(String namespaceId, String promptKey, Map<String, String> labels)
        throws NacosException {
        clientHolder.getAiMaintainerService().prompt().updateLabels(namespaceId, promptKey,
            JacksonUtils.toJson(labels));
    }
    
    /** 更新远端 Prompt 描述信息。 */
    @Override
    public void updateDescription(String namespaceId, String promptKey, String description)
        throws NacosException {
        clientHolder.getAiMaintainerService().prompt().updateDescription(namespaceId, promptKey,
            description);
    }
    
    /** 更新远端 Prompt 业务标签。 */
    @Override
    public void updateBizTags(String namespaceId, String promptKey, String bizTags)
        throws NacosException {
        clientHolder.getAiMaintainerService().prompt().updateBizTags(namespaceId, promptKey,
            bizTags);
    }
}
