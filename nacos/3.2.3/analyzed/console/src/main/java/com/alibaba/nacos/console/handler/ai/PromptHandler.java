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

package com.alibaba.nacos.console.handler.ai;

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

import java.util.List;
import java.util.Map;

/**
 * Prompt 控制台处理器接口：治理元数据、版本生命周期、草稿/提审/发布及上下线等全链路操作。
 * Prompt handler interface.
 *
 * @author nacos
 */
public interface PromptHandler {
    
    // ========== 通用 API ==========
    
    /**
      * 删除 Prompt。
     * Delete prompt.
     */
    boolean deletePrompt(PromptForm form, String srcUser, String srcIp) throws NacosException;
    
    /**
      * 分页列举 Prompt 摘要。
     * List prompts with pagination.
     */
    Page<PromptMetaSummary> listPrompts(PromptListForm form) throws NacosException;
    
    /**
      * 分页列举 Prompt 版本历史。
     * List prompt versions page.
     */
    Page<PromptVersionSummary> listPromptVersions(PromptHistoryForm form) throws NacosException;
    
    // ========== 生命周期 API ==========
    
    /**
      * 获取 Prompt 治理元数据详情。
     * Get prompt governance detail.
     */
    PromptMetaInfo getPromptGovernanceDetail(String namespaceId, String promptKey)
        throws NacosException;
    
    /**
      * 获取 Prompt 指定版本详情。
     * Get specific version detail.
     */
    PromptVersionInfo getVersionDetail(String namespaceId, String promptKey, String version)
        throws NacosException;
    
    /**
      * 下载 Prompt 指定版本（触发下载计数递增）。
     * Download a specific prompt version (triggers download count increment).
     */
    PromptVersionInfo downloadPromptVersion(String namespaceId, String promptKey, String version)
        throws NacosException;
    
    /**
      * 创建草稿版本。
     * Create draft version.
     */
    String createDraft(String namespaceId, String promptKey, String basedOnVersion,
        String targetVersion,
        String template, List<PromptVariable> variables, String commitMsg, String description,
        String bizTags)
        throws NacosException;
    
    /**
      * 更新草稿内容。
     * Update draft content.
     */
    void updateDraft(String namespaceId, String promptKey, String template,
        List<PromptVariable> variables,
        String commitMsg) throws NacosException;
    
    /**
      * 删除草稿。
     * Delete draft.
     */
    void deleteDraft(String namespaceId, String promptKey) throws NacosException;
    
    /**
      * 提交至流水线审核。
     * Submit for review.
     */
    String submit(String namespaceId, String promptKey, String version) throws NacosException;
    
    /**
      * 发布已审核版本。
     * Publish a reviewed version.
     */
    void publish(String namespaceId, String promptKey, String version, boolean updateLatestLabel)
        throws NacosException;
    
    /**
      * 强制发布，跳过流水线校验。
     * Force-publish bypassing pipeline.
     */
    void forcePublish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException;
    
    /**
      * 将已审核版本退回草稿状态重新编辑。
     * Re-edit a reviewed version, transitioning it back to draft status.
     *
     * @param namespaceId 命名空间 ID
     * @param promptKey   Prompt 键名
     * @param version     待重新编辑的版本号
     * @throws NacosException if operation failed
     */
    void redraft(String namespaceId, String promptKey, String version) throws NacosException;
    
    /**
      * 上线或下线指定版本。
     * Online or offline a version.
     */
    void changeOnlineStatus(String namespaceId, String promptKey, String version, boolean online)
        throws NacosException;
    
    /**
      * 更新运行时路由标签。
     * Update labels.
     */
    void updateLabels(String namespaceId, String promptKey, Map<String, String> labels)
        throws NacosException;
    
    /**
      * 更新 Prompt 描述。
     * Update prompt description.
     */
    void updateDescription(String namespaceId, String promptKey, String description)
        throws NacosException;
    
    /**
      * 更新业务标签。
     * Update biz tags.
     */
    void updateBizTags(String namespaceId, String promptKey, String bizTags) throws NacosException;
}
