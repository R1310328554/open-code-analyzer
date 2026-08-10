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

import com.alibaba.nacos.ai.form.AiResourceFilterableForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillBizTagsUpdateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillDraftCreateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillLabelsUpdateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillListForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillOnlineForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillPublishForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillScopeForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillSubmitForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillUpdateForm;
import com.alibaba.nacos.ai.service.skills.SkillUploadRequest;
import com.alibaba.nacos.api.ai.model.skills.BatchUploadResult;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillMeta;
import com.alibaba.nacos.api.ai.model.skills.SkillSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.core.model.form.PageForm;

/**
 * Skill 控制台处理器接口：元数据查询、ZIP 上传、草稿/提审/发布及标签与可见范围管理。
 * Skill handler.
 *
 * @author nacos
 */
public interface SkillHandler {
    
    /**
      * 查询 Skill 元数据。
     * Get skill.
     *
     * @param form Skill 查询表单
     * @return skill
     * @throws NacosException nacos exception
     */
    SkillMeta getSkill(SkillForm form) throws NacosException;
    
    /**
      * 查询 Skill 指定版本完整内容。
     * Get skill version detail. Returns full skill content for a specific version.
     *
     * @param form Skill 查询表单 (with version)
     * @return full skill content
     * @throws NacosException nacos exception
     */
    Skill getSkillVersion(SkillForm form) throws NacosException;
    
    /**
      * 下载 Skill 版本（独立入口以便统计下载事件）。
     * Download skill version. Provides a separate entry point from {@link #getSkillVersion} so that download events can
     * be tracked independently.
     *
     * @param form Skill 查询表单 (with version)
     * @return full skill content
     * @throws NacosException nacos exception
     */
    Skill downloadSkillVersion(SkillForm form) throws NacosException;
    
    /**
      * 删除 Skill。
     * Delete skill.
     *
     * @param form Skill 查询表单
     * @throws NacosException nacos exception
     */
    void deleteSkill(SkillForm form) throws NacosException;
    
    /**
      * 分页列举 Skill 摘要。
     * List skills.
     *
     * @param skillListForm Skill 列表查询表单
     * @param pageForm      分页参数
     * @return skill list
     * @throws NacosException nacos exception
     */
    Page<SkillSummary> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm,
        PageForm pageForm) throws NacosException;
    
    /**
      * 从 ZIP 文件上传 Skill。
     * Upload skill from zip file.
     *
     * @param request 上传请求
     * @return skill name
     * @throws NacosException if upload failed
     */
    String uploadSkillFromZip(SkillUploadRequest request) throws NacosException;
    
    /**
      * 从含多个 Skill 子目录的 ZIP 批量上传。
     * Batch upload multiple skills from a single zip file containing multiple skill subdirectories.
     *
     * @param namespaceId 命名空间 ID
     * @param zipBytes    ZIP 文件字节内容
     * @param overwrite   是否覆盖已有草稿
     * @return batch upload result with succeeded and failed lists
     * @throws NacosException if zip parsing fails entirely
     */
    BatchUploadResult batchUploadSkillsFromZip(String namespaceId, byte[] zipBytes,
        boolean overwrite)
        throws NacosException;
    
    /**
      * 基于最新版或指定版本创建草稿。
     * Create draft version based on latest or a specified version.
     *
     * @param form 草稿创建表单
     * @return created draft version
     * @throws NacosException if operation failed
     */
    String createDraft(SkillDraftCreateForm form) throws NacosException;
    
    /**
      * 更新当前草稿内容。
     * Update current draft content.
     *
     * @param form 更新表单
     * @throws NacosException if operation failed
     */
    void updateDraft(SkillUpdateForm form) throws NacosException;
    
    /**
      * 删除当前草稿版本。
     * Delete current draft version.
     *
     * @param form Skill 查询表单
     * @throws NacosException if operation failed
     */
    void deleteDraft(SkillForm form) throws NacosException;
    
    /**
      * 提交版本至流水线审核。
     * Submit a version for pipeline review.
     *
     * @param form 提交表单
     * @return submit result (e.g. pipeline id)
     * @throws NacosException if operation failed
     */
    String submit(SkillSubmitForm form) throws NacosException;
    
    /**
      * 发布已通过审核的版本。
     * Publish an approved reviewing version.
     *
     * @param form 发布表单
     * @throws NacosException if operation failed
     */
    void publish(SkillPublishForm form) throws NacosException;
    
    /**
      * 强制发布 Skill 版本，跳过流水线校验（限管理员）。
     * Force-publish a skill version, bypassing pipeline validation. Accepts draft, reviewing, and reviewed versions.
     * Should only be called by admin users.
     *
     * @param form 发布表单
     * @throws NacosException nacos exception
     */
    void forcePublish(SkillPublishForm form) throws NacosException;
    
    /**
      * 将已审核版本退回草稿状态重新编辑。
     * Re-edit a reviewed version, transitioning it back to draft status.
     *
     * @param form 发布表单 (contains namespace, skill name, version)
     * @throws NacosException if operation failed
     */
    void redraft(SkillPublishForm form) throws NacosException;
    
    /**
      * 更新运行时路由标签（不改变版本状态）。
     * Update runtime route labels without changing version status.
     *
     * @param form 标签更新表单
     * @throws NacosException if operation failed
     */
    void updateLabels(SkillLabelsUpdateForm form) throws NacosException;
    
    /**
      * 更新 Skill 业务标签（不改变版本状态）。
     * Update skill biz tags without changing version status.
     *
     * @param form 业务标签更新表单
     * @throws NacosException if operation failed
     */
    void updateBizTags(SkillBizTagsUpdateForm form) throws NacosException;
    
    /**
      * 切换上线/下线状态。
     * Change online/offline status.
     *
     * @param form   上下线操作表单
     * @param online true 上线，false 下线
     * @throws NacosException if operation failed
     */
    void changeOnlineStatus(SkillOnlineForm form, boolean online) throws NacosException;
    
    /**
      * 更新 Skill 可见性范围（PUBLIC/PRIVATE）。
     * Update skill visibility scope (PUBLIC/PRIVATE).
     *
     * @param form 可见范围更新表单
     * @throws NacosException if operation failed
     */
    void updateScope(SkillScopeForm form) throws NacosException;
}
