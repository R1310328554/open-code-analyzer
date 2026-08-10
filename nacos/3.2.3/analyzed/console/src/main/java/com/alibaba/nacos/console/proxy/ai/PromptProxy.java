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
import com.alibaba.nacos.console.handler.ai.PromptHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Prompt 代理：将 Prompt 查询、草稿、发布与治理操作委托给 {@link PromptHandler}。
 * Prompt proxy for console.
 *
 * @author nacos
 */
@Component
public class PromptProxy {
    
    /** Prompt Handler 实现 */
    private final PromptHandler promptHandler;
    
    /** 注入 Prompt Handler。 */
    public PromptProxy(PromptHandler promptHandler) {
        this.promptHandler = promptHandler;
    }
    
    // ========== 通用 API ==========
    
    /** 删除 Prompt 及其全部版本。 */
    public boolean deletePrompt(PromptForm form, String srcUser, String srcIp)
        throws NacosException {
        return promptHandler.deletePrompt(form, srcUser, srcIp);
    }
    
    /** 分页列出 Prompt 元信息摘要。 */
    public Page<PromptMetaSummary> listPrompts(PromptListForm form) throws NacosException {
        return promptHandler.listPrompts(form);
    }
    
    /** 分页列出 Prompt 版本历史。 */
    public Page<PromptVersionSummary> listPromptVersions(PromptHistoryForm form)
        throws NacosException {
        return promptHandler.listPromptVersions(form);
    }
    
    // ========== 生命周期 API ==========
    
    /** 查询 Prompt 治理元信息详情。 */
    public PromptMetaInfo getPromptGovernanceDetail(String namespaceId, String promptKey)
        throws NacosException {
        return promptHandler.getPromptGovernanceDetail(namespaceId, promptKey);
    }
    
    /** 查询 Prompt 指定版本详情。 */
    public PromptVersionInfo getVersionDetail(String namespaceId, String promptKey, String version)
        throws NacosException {
        return promptHandler.getVersionDetail(namespaceId, promptKey, version);
    }
    
    /** 下载 Prompt 指定版本内容。 */
    public PromptVersionInfo downloadPromptVersion(String namespaceId, String promptKey,
        String version)
        throws NacosException {
        return promptHandler.downloadPromptVersion(namespaceId, promptKey, version);
    }
    
    /** 创建 Prompt 草稿。 */
    public String createDraft(String namespaceId, String promptKey, String basedOnVersion,
        String targetVersion,
        String template, List<PromptVariable> variables, String commitMsg, String description,
        String bizTags)
        throws NacosException {
        return promptHandler.createDraft(namespaceId, promptKey, basedOnVersion, targetVersion,
            template, variables,
            commitMsg, description, bizTags);
    }
    
    /** 更新 Prompt 草稿模板与变量。 */
    public void updateDraft(String namespaceId, String promptKey, String template,
        List<PromptVariable> variables,
        String commitMsg) throws NacosException {
        promptHandler.updateDraft(namespaceId, promptKey, template, variables, commitMsg);
    }
    
    /** 删除 Prompt 草稿。 */
    public void deleteDraft(String namespaceId, String promptKey) throws NacosException {
        promptHandler.deleteDraft(namespaceId, promptKey);
    }
    
    /** 提交 Prompt 版本审核。 */
    public String submit(String namespaceId, String promptKey, String version)
        throws NacosException {
        return promptHandler.submit(namespaceId, promptKey, version);
    }
    
    /** 发布 Prompt 版本。 */
    public void publish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        promptHandler.publish(namespaceId, promptKey, version, updateLatestLabel);
    }
    
    /** 强制发布 Prompt 版本。 */
    public void forcePublish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        promptHandler.forcePublish(namespaceId, promptKey, version, updateLatestLabel);
    }
    
    /** 将已发布 Prompt 版本退回草稿。 */
    public void redraft(String namespaceId, String promptKey, String version)
        throws NacosException {
        promptHandler.redraft(namespaceId, promptKey, version);
    }
    
    /** 切换 Prompt 版本上下线状态。 */
    public void changeOnlineStatus(String namespaceId, String promptKey, String version,
        boolean online)
        throws NacosException {
        promptHandler.changeOnlineStatus(namespaceId, promptKey, version, online);
    }
    
    /** 更新 Prompt 标签。 */
    public void updateLabels(String namespaceId, String promptKey, Map<String, String> labels)
        throws NacosException {
        promptHandler.updateLabels(namespaceId, promptKey, labels);
    }
    
    /** 更新 Prompt 描述信息。 */
    public void updateDescription(String namespaceId, String promptKey, String description)
        throws NacosException {
        promptHandler.updateDescription(namespaceId, promptKey, description);
    }
    
    /** 更新 Prompt 业务标签。 */
    public void updateBizTags(String namespaceId, String promptKey, String bizTags)
        throws NacosException {
        promptHandler.updateBizTags(namespaceId, promptKey, bizTags);
    }
}
