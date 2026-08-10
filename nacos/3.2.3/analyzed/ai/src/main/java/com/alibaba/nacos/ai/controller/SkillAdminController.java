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

package com.alibaba.nacos.ai.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.ai.constant.Constants;
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
import com.alibaba.nacos.api.ai.model.skills.BatchUploadResult;
import com.alibaba.nacos.ai.param.SkillHttpParamExtractor;
import com.alibaba.nacos.ai.service.skills.SkillOperationService;
import com.alibaba.nacos.ai.service.skills.SkillUploadRequest;
import com.alibaba.nacos.ai.utils.SkillRequestUtil;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillMeta;
import com.alibaba.nacos.api.ai.model.skills.SkillSummary;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.alibaba.nacos.ai.constant.Constants.Skills.ADMIN_PATH;
import static com.alibaba.nacos.plugin.auth.constant.Constants.Tag.ALLOW_ANONYMOUS;

/**
 * Skill 管理端控制器，提供 Skill 全生命周期治理 API。
 *
 * <p>涵盖详情查询、版本管理、草稿编辑、提审发布、上下线、标签与可见性等操作；上传接口支持单 Skill ZIP 与批量导入。</p>
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Skills.ADMIN_PATH)
@ExtractorManager.Extractor(httpExtractor = SkillHttpParamExtractor.class)
public class SkillAdminController {
    
    /** Skill 写操作与治理服务 */
    private final SkillOperationService skillOperationService;
    
    public SkillAdminController(SkillOperationService skillOperationService) {
        this.skillOperationService = skillOperationService;
    }
    
    /**
     * 管理端获取 Skill 详情（含版本治理信息与全部版本摘要）。
     *
     * @param form the skill form to get
     * @return result of the get operation
     * @throws NacosException if the skill get fails
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<SkillMeta> getSkill(SkillForm form) throws NacosException {
        form.validate();
        return Result.success(
            skillOperationService.getSkillDetail(form.getNamespaceId(), form.getSkillName()));
    }
    
    /**
     * 获取指定 Skill 版本的完整内容，供查看或编辑。
     *
     * @param form the skill form containing skillName and version
     * @return full skill content for the specified version
     * @throws NacosException if the skill or version not found
     */
    @Since("3.2.0")
    @GetMapping("/version")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<Skill> getSkillVersion(SkillForm form) throws NacosException {
        form.validate();
        return Result.success(
            skillOperationService.getSkillVersionDetail(form.getNamespaceId(), form.getSkillName(),
                form.getVersion()));
    }
    
    /**
     * 下载指定 Skill 版本为 ZIP 包。
     *
     * @param form the skill form containing skillName and version
     * @return ZIP file as ResponseEntity
     * @throws NacosException if the skill or version not found
     */
    @Since("3.2.0")
    @GetMapping("/version/download")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public ResponseEntity<byte[]> downloadSkillVersion(SkillForm form) throws NacosException {
        form.validate();
        Skill skill =
            skillOperationService.downloadSkillVersion(form.getNamespaceId(), form.getSkillName(),
                form.getVersion());
        return SkillRequestUtil.buildSkillZipResponse(skill);
    }
    
    /**
     * 删除 Skill 及其全部版本数据。
     *
     * @param form the skill form to delete
     * @return result of the deletion operation
     * @throws NacosException if the skill deletion fails
     */
    @Since("3.2.0")
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> deleteSkill(SkillForm form) throws NacosException {
        form.validate();
        skillOperationService.deleteSkill(form.getNamespaceId(), form.getSkillName());
        return Result.success("ok");
    }
    
    /**
     * 管理端分页列出 Skill（含状态、标签、labels 等治理元数据）。
     *
     * @param skillListForm the skill list form to list
     * @param pageForm      the page form to list
     * @return result of the list operation
     * @throws NacosException if the skill list fails
     */
    @Since("3.2.1")
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.ADMIN_API,
        tags = {ALLOW_ANONYMOUS})
    public Result<Page<SkillSummary>> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm, PageForm pageForm) throws NacosException {
        skillListForm.validate();
        filterableForm.validate();
        pageForm.validate();
        return Result.success(
            skillOperationService.listSkills(skillListForm.getNamespaceId(),
                skillListForm.getSkillName(),
                skillListForm.getSearch(), skillListForm.getOrderBy(),
                filterableForm.getOwner(), filterableForm.getScope(), filterableForm.getBizTag(),
                pageForm.getPageNo(), pageForm.getPageSize()));
    }
    
    /**
     * 从 ZIP 文件上传 Skill。
     *
     * @param request     HTTP servlet request
     * @param namespaceId namespace ID
     * @param commitMsg   version-level commit message
     * @param file        zip file containing skill
     * @return result of the upload operation
     * @throws NacosException if the upload fails
     */
    @Since("3.2.2")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    @ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
    public Result<String> uploadSkill(HttpServletRequest request,
        @RequestParam(value = "namespaceId", required = false) String namespaceId,
        @RequestParam(value = "overwrite", required = false,
            defaultValue = "false") boolean overwrite,
        @RequestParam(value = "targetVersion", required = false) String targetVersion,
        @RequestParam(value = "commitMsg", required = false) String commitMsg,
        @RequestParam("file") MultipartFile file) throws NacosException {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
        byte[] zipBytes = SkillRequestUtil.validateAndExtractZipBytes(file);
        SkillUploadRequest uploadRequest = SkillUploadRequest.builder()
            .namespaceId(namespaceId)
            .zipBytes(zipBytes)
            .overwrite(overwrite)
            .targetVersion(targetVersion)
            .commitMsg(commitMsg)
            .build();
        String skillName = skillOperationService.uploadSkillFromZip(uploadRequest);
        return Result.success(skillName);
    }
    
    /**
     * 从单个 ZIP 批量上传多个 Skill（一级子目录各含 SKILL.md），尽力而为策略。
     *
     * @param request     HTTP servlet request
     * @param namespaceId namespace ID
     * @param overwrite   whether to overwrite existing drafts
     * @param file        zip file containing multiple skill subdirectories
     * @return batch upload result with succeeded and failed lists
     * @throws NacosException if zip parsing fails entirely
     */
    @Since("3.2.2")
    @PostMapping(value = "/upload/batch", consumes = "multipart/form-data")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    @ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
    public Result<BatchUploadResult> batchUploadSkills(HttpServletRequest request,
        @RequestParam(value = "namespaceId", required = false) String namespaceId,
        @RequestParam(value = "overwrite", required = false,
            defaultValue = "false") boolean overwrite,
        @RequestParam("file") MultipartFile file) throws NacosException {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
        byte[] zipBytes = SkillRequestUtil.validateAndExtractZipBytes(file);
        BatchUploadResult result =
            skillOperationService.batchUploadSkillsFromZip(namespaceId, zipBytes, overwrite);
        return Result.success(result);
    }
    
    /**
     * 创建草稿：未指定 {@code basedOnVersion} 时必须提供 {@code skillCard}；可基于已有版本 fork。
     */
    @Since("3.2.0")
    @PostMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> createDraft(SkillDraftCreateForm form) throws NacosException {
        form.prepareCreateDraftRequest();
        String v = skillOperationService.createDraft(form.getNamespaceId(), form.getSkillName(),
            form.getBasedOnVersion(), form.getTargetVersion(), form.getResolvedInitialSkillOrNull(),
            form.getCommitMsg());
        return Result.success(v);
    }
    
    /**
     * 更新当前草稿版本内容。
     */
    @Since("3.2.0")
    @PutMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> updateDraft(SkillUpdateForm form) throws NacosException {
        form.validate();
        Skill skill = SkillRequestUtil.parseSkill(form);
        skillOperationService.updateDraft(form.getNamespaceId(), skill, form.getCommitMsg());
        return Result.success("ok");
    }
    
    /**
     * 删除当前草稿版本。
     */
    @Since("3.2.0")
    @DeleteMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> deleteDraft(SkillForm form) throws NacosException {
        form.validate();
        skillOperationService.deleteDraft(form.getNamespaceId(), form.getSkillName());
        return Result.success("ok");
    }
    
    /**
     * 提交版本进入流水线审核。
     */
    @Since("3.2.0")
    @PostMapping("/submit")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> submit(SkillSubmitForm form) throws NacosException {
        form.validate();
        String result = skillOperationService.submit(form.getNamespaceId(), form.getSkillName(),
            form.getVersion());
        return Result.success(result);
    }
    
    /**
     * 发布已通过审核的版本。
     */
    @Since("3.2.0")
    @PostMapping("/publish")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> publish(SkillPublishForm form) throws NacosException {
        form.validate();
        boolean updateLatest = form.getUpdateLatestLabel() == null || form.getUpdateLatestLabel();
        skillOperationService.publish(form.getNamespaceId(), form.getSkillName(), form.getVersion(),
            updateLatest);
        return Result.success("ok");
    }
    
    /**
     * 强制发布 Skill 版本，跳过流水线校验；仅管理员可调用。
     */
    @Since("3.2.1")
    @PostMapping("/force-publish")
    @Secured(resource = ADMIN_PATH
        + "/force-publish", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.ADMIN_API)
    public Result<String> forcePublish(SkillPublishForm form) throws NacosException {
        form.validate();
        boolean updateLatest = form.getUpdateLatestLabel() == null || form.getUpdateLatestLabel();
        skillOperationService.forcePublish(form.getNamespaceId(), form.getSkillName(),
            form.getVersion(), updateLatest);
        return Result.success("ok");
    }
    
    /**
     * 将已审核版本退回草稿以便重新编辑。
     */
    @Since("3.2.2")
    @PostMapping("/redraft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> redraft(SkillPublishForm form) throws NacosException {
        form.validate();
        skillOperationService.redraft(form.getNamespaceId(), form.getSkillName(),
            form.getVersion());
        return Result.success("ok");
    }
    
    /**
     * 更新运行时路由 labels，不改变版本状态。
     */
    @Since("3.2.0")
    @PutMapping("/labels")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> updateLabels(SkillLabelsUpdateForm form) throws NacosException {
        form.validate();
        Map<String, String> labels = JacksonUtils.toObj(form.getLabels(), Map.class);
        skillOperationService.updateLabels(form.getNamespaceId(), form.getSkillName(), labels);
        return Result.success("ok");
    }
    
    /**
     * 更新 Skill 业务标签，不改变版本状态。
     */
    @Since("3.2.0")
    @PutMapping("/biz-tags")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> updateBizTags(SkillBizTagsUpdateForm form) throws NacosException {
        form.validate();
        skillOperationService.updateBizTags(form.getNamespaceId(), form.getSkillName(),
            form.getBizTags());
        return Result.success("ok");
    }
    
    /**
     * 上线操作（按 scope 支持版本级或 Skill 级）。
     */
    @Since("3.2.0")
    @PostMapping("/online")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> online(SkillOnlineForm form) throws NacosException {
        form.validate();
        skillOperationService.changeOnlineStatus(form.getNamespaceId(), form.getSkillName(),
            form.getScope(),
            form.getVersion(), true);
        return Result.success("ok");
    }
    
    /**
     * 更新 Skill 可见性范围（PUBLIC 或 PRIVATE）。
     *
     * @param form the scope update form
     * @return result of the update operation
     * @throws NacosException if the skill not found or no permission
     */
    @Since("3.2.0")
    @PutMapping("/scope")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> updateScope(SkillScopeForm form) throws NacosException {
        form.validate();
        skillOperationService.updateScope(form.getNamespaceId(), form.getSkillName(),
            form.getScope());
        return Result.success("ok");
    }
    
    /**
     * 下线操作（按 scope 支持版本级或 Skill 级）。
     */
    @Since("3.2.0")
    @PostMapping("/offline")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    public Result<String> offline(SkillOnlineForm form) throws NacosException {
        form.validate();
        skillOperationService.changeOnlineStatus(form.getNamespaceId(), form.getSkillName(),
            form.getScope(),
            form.getVersion(), false);
        return Result.success("ok");
    }
}
