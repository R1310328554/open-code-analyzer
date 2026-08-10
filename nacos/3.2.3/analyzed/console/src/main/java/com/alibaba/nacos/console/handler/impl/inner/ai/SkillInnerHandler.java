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

import com.alibaba.nacos.ai.form.AiResourceFilterableForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillDraftCreateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillBizTagsUpdateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillLabelsUpdateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillOnlineForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillPublishForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillScopeForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillListForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillSubmitForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillUpdateForm;
import com.alibaba.nacos.api.ai.model.skills.BatchUploadResult;
import com.alibaba.nacos.ai.service.skills.SkillOperationService;
import com.alibaba.nacos.ai.service.skills.SkillUploadRequest;
import com.alibaba.nacos.ai.utils.SkillRequestUtil;
import com.alibaba.nacos.console.handler.ai.SkillHandler;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillMeta;
import com.alibaba.nacos.api.ai.model.skills.SkillSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.impl.inner.EnabledInnerHandler;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.common.utils.JacksonUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Skill 控制台内嵌 Handler：ZIP 上传、草稿、提审、发布与可见范围管理均委托 {@link SkillOperationService}。
 * Skill inner handler.
 *
 * @author nacos
 */
@Component
@EnabledInnerHandler
@EnabledAiHandler
public class SkillInnerHandler implements SkillHandler {
    
    /** Skill 运维服务，封装全生命周期与批量上传 */
    private final SkillOperationService skillOperationService;
    
    /** 注入 Skill 运维服务 */
    public SkillInnerHandler(SkillOperationService skillOperationService) {
        this.skillOperationService = skillOperationService;
    }
    
    /** 获取 Skill 管理端元数据详情 */
    @Override
    public SkillMeta getSkill(SkillForm form) throws NacosException {
        return skillOperationService.getSkillDetail(form.getNamespaceId(), form.getSkillName());
    }
    
    /** 获取指定版本的 Skill 完整内容 */
    @Override
    public Skill getSkillVersion(SkillForm form) throws NacosException {
        return skillOperationService.getSkillVersionDetail(form.getNamespaceId(),
            form.getSkillName(), form.getVersion());
    }
    
    /** 下载指定版本 Skill 内容 */
    @Override
    public Skill downloadSkillVersion(SkillForm form) throws NacosException {
        return skillOperationService.downloadSkillVersion(form.getNamespaceId(),
            form.getSkillName(), form.getVersion());
    }
    
    /** 删除 Skill 及其全部版本 */
    @Override
    public void deleteSkill(SkillForm form) throws NacosException {
        skillOperationService.deleteSkill(form.getNamespaceId(), form.getSkillName());
    }
    
    /** 分页列出 Skill 摘要，支持所有者、范围与业务标签过滤 */
    @Override
    public Page<SkillSummary> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm,
        PageForm pageForm) throws NacosException {
        return skillOperationService.listSkills(skillListForm.getNamespaceId(),
            skillListForm.getSkillName(),
            skillListForm.getSearch(), skillListForm.getOrderBy(),
            filterableForm.getOwner(), filterableForm.getScope(), filterableForm.getBizTag(),
            pageForm.getPageNo(), pageForm.getPageSize());
    }
    
    /** 从 ZIP 包上传单个 Skill */
    @Override
    public String uploadSkillFromZip(SkillUploadRequest request) throws NacosException {
        return skillOperationService.uploadSkillFromZip(request);
    }
    
    /** 批量从 ZIP 上传 Skill，可选覆盖已有草稿 */
    @Override
    public BatchUploadResult batchUploadSkillsFromZip(String namespaceId, byte[] zipBytes,
        boolean overwrite)
        throws NacosException {
        return skillOperationService.batchUploadSkillsFromZip(namespaceId, zipBytes, overwrite);
    }
    
    /** 基于已有版本创建 Skill 草稿 */
    @Override
    public String createDraft(SkillDraftCreateForm form) throws NacosException {
        return skillOperationService.createDraft(form.getNamespaceId(), form.getSkillName(),
            form.getBasedOnVersion(),
            form.getTargetVersion(), form.getResolvedInitialSkillOrNull(), form.getCommitMsg());
    }
    
    /** 更新当前 Skill 草稿内容 */
    @Override
    public void updateDraft(SkillUpdateForm form) throws NacosException {
        Skill skill = SkillRequestUtil.parseSkill(form);
        skillOperationService.updateDraft(form.getNamespaceId(), skill, form.getCommitMsg());
    }
    
    /** 删除当前 Skill 草稿 */
    @Override
    public void deleteDraft(SkillForm form) throws NacosException {
        skillOperationService.deleteDraft(form.getNamespaceId(), form.getSkillName());
    }
    
    /** 提交 Skill 版本进入流水线审核 */
    @Override
    public String submit(SkillSubmitForm form) throws NacosException {
        return skillOperationService.submit(form.getNamespaceId(), form.getSkillName(),
            form.getVersion());
    }
    
    /** 发布已通过审核的 Skill 版本 */
    @Override
    public void publish(SkillPublishForm form) throws NacosException {
        boolean updateLatest = form.getUpdateLatestLabel() == null || form.getUpdateLatestLabel();
        skillOperationService.publish(form.getNamespaceId(), form.getSkillName(), form.getVersion(),
            updateLatest);
    }
    
    /** 强制发布 Skill 版本，跳过流水线校验 */
    @Override
    public void forcePublish(SkillPublishForm form) throws NacosException {
        boolean updateLatest = form.getUpdateLatestLabel() == null || form.getUpdateLatestLabel();
        skillOperationService.forcePublish(form.getNamespaceId(), form.getSkillName(),
            form.getVersion(), updateLatest);
    }
    
    /** 将已审核 Skill 版本重新编辑为草稿 */
    @Override
    public void redraft(SkillPublishForm form) throws NacosException {
        skillOperationService.redraft(form.getNamespaceId(), form.getSkillName(),
            form.getVersion());
    }
    
    /** 更新 Skill 运行时路由标签 */
    @Override
    public void updateLabels(SkillLabelsUpdateForm form) throws NacosException {
        Map<String, String> labels = JacksonUtils.toObj(form.getLabels(), Map.class);
        skillOperationService.updateLabels(form.getNamespaceId(), form.getSkillName(), labels);
    }
    
    /** 更新 Skill 业务标签 */
    @Override
    public void updateBizTags(SkillBizTagsUpdateForm form) throws NacosException {
        skillOperationService.updateBizTags(form.getNamespaceId(), form.getSkillName(),
            form.getBizTags());
    }
    
    /** 切换 Skill 指定范围的上线/下线状态 */
    @Override
    public void changeOnlineStatus(SkillOnlineForm form, boolean online) throws NacosException {
        skillOperationService.changeOnlineStatus(form.getNamespaceId(), form.getSkillName(),
            form.getScope(),
            form.getVersion(), online);
    }
    
    /** 更新 Skill 可见范围 */
    @Override
    public void updateScope(SkillScopeForm form) throws NacosException {
        skillOperationService.updateScope(form.getNamespaceId(), form.getSkillName(),
            form.getScope());
    }
}
