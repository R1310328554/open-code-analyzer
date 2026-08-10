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

package com.alibaba.nacos.console.handler.impl.noop.ai;

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
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.console.handler.ai.SkillHandler;
import com.alibaba.nacos.core.model.form.PageForm;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * Skill 空实现 Handler：AI 模块未启用或 naming/config 未同时可用时注册，全部接口返回 {@link ErrorCode#API_FUNCTION_DISABLED}。
 * Noop implementation of Skill handler.
 * Used when AI module is not enabled or both `naming` and `config` modules are not available.
 *
 * @author nacos
 */
@Service
@ConditionalOnMissingBean(value = SkillHandler.class, ignored = SkillNoopHandler.class)
public class SkillNoopHandler implements SkillHandler {
    
    /** Skill 功能未启用时的统一错误提示文案 */
    private static final String SKILL_NOT_ENABLED_MESSAGE =
        "Nacos AI Skill module and API required both `naming` and `config` module.";
    
    /** 获取 Skill 元信息 — 功能未启用时抛出异常 */
    @Override
    public SkillMeta getSkill(SkillForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 获取 Skill 指定版本 — 功能未启用时抛出异常 */
    @Override
    public Skill getSkillVersion(SkillForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 下载 Skill 版本包 — 功能未启用时抛出异常 */
    @Override
    public Skill downloadSkillVersion(SkillForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 删除 Skill — 功能未启用时抛出异常 */
    @Override
    public void deleteSkill(SkillForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 分页列出 Skill — 功能未启用时抛出异常 */
    @Override
    public Page<SkillSummary> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm,
        PageForm pageForm) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 从 ZIP 上传 Skill — 功能未启用时抛出异常 */
    @Override
    public String uploadSkillFromZip(SkillUploadRequest request) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 批量从 ZIP 上传 Skill — 功能未启用时抛出异常 */
    @Override
    public BatchUploadResult batchUploadSkillsFromZip(String namespaceId, byte[] zipBytes,
        boolean overwrite)
        throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 创建 Skill 草稿 — 功能未启用时抛出异常 */
    @Override
    public String createDraft(SkillDraftCreateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新 Skill 草稿 — 功能未启用时抛出异常 */
    @Override
    public void updateDraft(SkillUpdateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 删除 Skill 草稿 — 功能未启用时抛出异常 */
    @Override
    public void deleteDraft(SkillForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 提交 Skill 审核 — 功能未启用时抛出异常 */
    @Override
    public String submit(SkillSubmitForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 发布 Skill 版本 — 功能未启用时抛出异常 */
    @Override
    public void publish(SkillPublishForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新 Skill 标签 — 功能未启用时抛出异常 */
    @Override
    public void updateLabels(SkillLabelsUpdateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新 Skill 业务标签 — 功能未启用时抛出异常 */
    @Override
    public void updateBizTags(SkillBizTagsUpdateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 切换 Skill 上下线状态 — 功能未启用时抛出异常 */
    @Override
    public void changeOnlineStatus(SkillOnlineForm form, boolean online) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新 Skill 可见范围 — 功能未启用时抛出异常 */
    @Override
    public void updateScope(SkillScopeForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 强制发布 Skill — 功能未启用时抛出异常 */
    @Override
    public void forcePublish(SkillPublishForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
    
    /** 将已发布 Skill 退回草稿 — 功能未启用时抛出异常 */
    @Override
    public void redraft(SkillPublishForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            SKILL_NOT_ENABLED_MESSAGE);
    }
}
