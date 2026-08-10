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
import com.alibaba.nacos.ai.service.skills.SkillUploadRequest;
import com.alibaba.nacos.api.ai.model.skills.BatchUploadResult;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillMeta;
import com.alibaba.nacos.api.ai.model.skills.SkillSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.ai.EnabledAiHandler;
import com.alibaba.nacos.console.handler.ai.SkillHandler;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import com.alibaba.nacos.core.model.form.PageForm;
import org.springframework.stereotype.Service;

/**
 * Skill 远程 Handler：ZIP 上传、草稿、提审、发布与可见范围管理均通过 {@link NacosMaintainerClientHolder} 调用远端 AI 运维 API。
 * Remote implementation of Skill handler.
 *
 * <p>Calls remote Nacos server through maintainer client for Skill operations.</p>
 *
 * @author nacos
 */
@Service
@EnabledRemoteHandler
@EnabledAiHandler
public class SkillRemoteHandler implements SkillHandler {
    
    /** 运维客户端持有者，提供 AI Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public SkillRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 获取远端 Skill 管理端元数据详情。 */
    @Override
    public SkillMeta getSkill(SkillForm form) throws NacosException {
        return clientHolder.getAiMaintainerService().skill().getSkillMeta(
            form.getNamespaceId(),
            form.getSkillName());
    }
    
    /** 获取远端指定版本的 Skill 完整内容。 */
    @Override
    public Skill getSkillVersion(SkillForm form) throws NacosException {
        return clientHolder.getAiMaintainerService().skill().getSkillVersionDetail(
            form.getNamespaceId(),
            form.getSkillName(),
            form.getVersion());
    }
    
    /** 下载远端指定版本 Skill 内容（委托 getSkillVersion）。 */
    @Override
    public Skill downloadSkillVersion(SkillForm form) throws NacosException {
        return getSkillVersion(form);
    }
    
    /** 删除远端 Skill 及其全部版本。 */
    @Override
    public void deleteSkill(SkillForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill().deleteSkill(
            form.getNamespaceId(),
            form.getSkillName());
    }
    
    /** 分页列出远端 Skill 摘要，支持所有者、范围与业务标签过滤；无结果时返回空页。 */
    @Override
    public Page<SkillSummary> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm,
        PageForm pageForm) throws NacosException {
        Page<SkillSummary> result = clientHolder.getAiMaintainerService().skill().listSkills(
            skillListForm.getNamespaceId(),
            skillListForm.getSkillName(),
            skillListForm.getSearch(),
            skillListForm.getOrderBy(),
            filterableForm.getOwner(),
            filterableForm.getScope(),
            filterableForm.getBizTag(),
            pageForm.getPageNo(),
            pageForm.getPageSize());
        if (result == null) {
            Page<SkillSummary> empty = new Page<>();
            empty.setTotalCount(0);
            empty.setPagesAvailable(0);
            empty.setPageNumber(pageForm.getPageNo());
            empty.setPageItems(new java.util.ArrayList<>());
            return empty;
        }
        return result;
    }
    
    /** 从 ZIP 包上传单个 Skill 到远端命名空间。 */
    @Override
    public String uploadSkillFromZip(SkillUploadRequest request) throws NacosException {
        return clientHolder.getAiMaintainerService().skill().uploadSkillFromZip(
            request.getNamespaceId(), request.getZipBytes(), request.isOverwrite(),
            request.getTargetVersion(), request.getCommitMsg());
    }
    
    /** 批量从 ZIP 上传 Skill 到远端，可选覆盖已有草稿。 */
    @Override
    public BatchUploadResult batchUploadSkillsFromZip(String namespaceId, byte[] zipBytes,
        boolean overwrite)
        throws NacosException {
        return clientHolder.getAiMaintainerService().skill().batchUploadSkillsFromZip(namespaceId,
            zipBytes, overwrite);
    }
    
    /** 在远端基于已有版本创建 Skill 草稿。 */
    @Override
    public String createDraft(SkillDraftCreateForm form) throws NacosException {
        return clientHolder.getAiMaintainerService().skill().createDraft(form.getNamespaceId(),
            form.getSkillName(),
            form.getBasedOnVersion(), form.getTargetVersion(), form.getSkillCard(),
            form.getCommitMsg());
    }
    
    /** 更新远端当前 Skill 草稿内容。 */
    @Override
    public void updateDraft(SkillUpdateForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill().updateDraft(form.getNamespaceId(),
            form.getSkillCard(),
            form.getSetAsLatest(), form.getCommitMsg());
    }
    
    /** 删除远端当前 Skill 草稿。 */
    @Override
    public void deleteDraft(SkillForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill().deleteDraft(form.getNamespaceId(),
            form.getSkillName());
    }
    
    /** 提交远端 Skill 版本进入流水线审核。 */
    @Override
    public String submit(SkillSubmitForm form) throws NacosException {
        return clientHolder.getAiMaintainerService().skill()
            .submit(form.getNamespaceId(), form.getSkillName(), form.getVersion());
    }
    
    /** 发布远端已通过审核的 Skill 版本。 */
    @Override
    public void publish(SkillPublishForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill()
            .publish(form.getNamespaceId(), form.getSkillName(), form.getVersion(),
                form.getUpdateLatestLabel());
    }
    
    /** 强制发布远端 Skill 版本，跳过流水线校验。 */
    @Override
    public void forcePublish(SkillPublishForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill()
            .forcePublish(form.getNamespaceId(), form.getSkillName(), form.getVersion(),
                form.getUpdateLatestLabel());
    }
    
    /** 将远端已审核 Skill 版本重新编辑为草稿。 */
    @Override
    public void redraft(SkillPublishForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill()
            .redraft(form.getNamespaceId(), form.getSkillName(), form.getVersion());
    }
    
    /** 更新远端 Skill 运行时路由标签。 */
    @Override
    public void updateLabels(SkillLabelsUpdateForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill()
            .updateLabels(form.getNamespaceId(), form.getSkillName(), form.getLabels());
    }
    
    /** 更新远端 Skill 业务标签。 */
    @Override
    public void updateBizTags(SkillBizTagsUpdateForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill()
            .updateBizTags(form.getNamespaceId(), form.getSkillName(), form.getBizTags());
    }
    
    /** 切换远端 Skill 指定范围的上线/下线状态。 */
    @Override
    public void changeOnlineStatus(SkillOnlineForm form, boolean online) throws NacosException {
        clientHolder.getAiMaintainerService().skill().changeOnlineStatus(form.getNamespaceId(),
            form.getSkillName(), form.getScope(), form.getVersion(), online);
    }
    
    /** 更新远端 Skill 可见范围。 */
    @Override
    public void updateScope(SkillScopeForm form) throws NacosException {
        clientHolder.getAiMaintainerService().skill().updateScope(form.getNamespaceId(),
            form.getSkillName(),
            form.getScope());
    }
}
