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
import com.alibaba.nacos.console.handler.ai.SkillHandler;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillMeta;
import com.alibaba.nacos.api.ai.model.skills.SkillSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.core.model.form.PageForm;
import org.springframework.stereotype.Component;

/**
 * Skill 代理：将 Skill 查询、ZIP 上传、草稿、发布与上下线委托给 {@link SkillHandler}。
 * Skill proxy.
 *
 * @author nacos
 */
@Component
public class SkillProxy {
    
    /** Skill Handler 实现 */
    private final SkillHandler skillHandler;
    
    /** 注入 Skill Handler。 */
    public SkillProxy(SkillHandler skillHandler) {
        this.skillHandler = skillHandler;
    }
    
    /** 获取 Skill 元信息。 */
    public SkillMeta getSkill(SkillForm form) throws NacosException {
        return skillHandler.getSkill(form);
    }
    
    /** 获取 Skill 指定版本内容。 */
    public Skill getSkillVersion(SkillForm form) throws NacosException {
        return skillHandler.getSkillVersion(form);
    }
    
    /** 下载 Skill 指定版本包。 */
    public Skill downloadSkillVersion(SkillForm form) throws NacosException {
        return skillHandler.downloadSkillVersion(form);
    }
    
    /** 删除 Skill 及其全部版本。 */
    public void deleteSkill(SkillForm form) throws NacosException {
        skillHandler.deleteSkill(form);
    }
    
    /** 分页列出 Skill，支持资源过滤。 */
    public Page<SkillSummary> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm,
        PageForm pageForm) throws NacosException {
        return skillHandler.listSkills(skillListForm, filterableForm, pageForm);
    }
    
    /** 从 ZIP 包上传单个 Skill。 */
    public String uploadSkillFromZip(SkillUploadRequest request) throws NacosException {
        return skillHandler.uploadSkillFromZip(request);
    }
    
    /** 从 ZIP 包批量上传 Skill。 */
    public BatchUploadResult batchUploadSkillsFromZip(String namespaceId, byte[] zipBytes,
        boolean overwrite)
        throws NacosException {
        return skillHandler.batchUploadSkillsFromZip(namespaceId, zipBytes, overwrite);
    }
    
    /** 创建 Skill 草稿。 */
    public String createDraft(SkillDraftCreateForm form) throws NacosException {
        return skillHandler.createDraft(form);
    }
    
    /** 更新 Skill 草稿内容。 */
    public void updateDraft(SkillUpdateForm form) throws NacosException {
        skillHandler.updateDraft(form);
    }
    
    /** 删除 Skill 草稿。 */
    public void deleteDraft(SkillForm form) throws NacosException {
        skillHandler.deleteDraft(form);
    }
    
    /** 提交 Skill 审核。 */
    public String submit(SkillSubmitForm form) throws NacosException {
        return skillHandler.submit(form);
    }
    
    /** 发布 Skill 版本。 */
    public void publish(SkillPublishForm form) throws NacosException {
        skillHandler.publish(form);
    }
    
    /** 强制发布 Skill 版本。 */
    public void forcePublish(SkillPublishForm form) throws NacosException {
        skillHandler.forcePublish(form);
    }
    
    /** 将已发布 Skill 版本退回草稿。 */
    public void redraft(SkillPublishForm form) throws NacosException {
        skillHandler.redraft(form);
    }
    
    /** 更新 Skill 标签。 */
    public void updateLabels(SkillLabelsUpdateForm form) throws NacosException {
        skillHandler.updateLabels(form);
    }
    
    /** 更新 Skill 业务标签。 */
    public void updateBizTags(SkillBizTagsUpdateForm form) throws NacosException {
        skillHandler.updateBizTags(form);
    }
    
    /** 将 Skill 设为上线。 */
    public void online(SkillOnlineForm form) throws NacosException {
        skillHandler.changeOnlineStatus(form, true);
    }
    
    /** 将 Skill 设为下线。 */
    public void offline(SkillOnlineForm form) throws NacosException {
        skillHandler.changeOnlineStatus(form, false);
    }
    
    /** 更新 Skill 可见范围。 */
    public void updateScope(SkillScopeForm form) throws NacosException {
        skillHandler.updateScope(form);
    }
}
