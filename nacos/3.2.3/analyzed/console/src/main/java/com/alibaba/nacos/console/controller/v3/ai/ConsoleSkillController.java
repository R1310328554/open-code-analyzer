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

package com.alibaba.nacos.console.controller.v3.ai;

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
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.console.proxy.ai.SkillProxy;
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

import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX;

/**
 * 控制台 Skill 全生命周期治理 REST 控制器。
 * 支持 ZIP 上传/下载、草稿编辑、流水线提交发布、上下线及可见性/标签管理等操作。
 *
 * Console skill controller.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Skills.CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = SkillHttpParamExtractor.class)
public class ConsoleSkillController {
    
    /** Skill 业务代理。 */
    private final SkillProxy skillProxy;
    
    /** 构造 Skill 控制器。 */
    public ConsoleSkillController(SkillProxy skillProxy) {
        this.skillProxy = skillProxy;
    }
    
    /**
      * 查询 Skill 元数据。
     * Get skill.
     *
     * @param form the skill form to get
     * @return result of the get operation
     * @throws NacosException if the skill get fails
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<SkillMeta> getSkill(SkillForm form) throws NacosException {
        form.validate();
        return Result.success(skillProxy.getSkill(form));
    }
    
    /**
      * 查询 Skill 指定版本完整内容。
     * Get specific version detail of a skill for viewing or editing.
     *
     * @param form the skill form containing skillName and version
     * @return full skill content for the specified version
     * @throws NacosException if the skill or version not found
     */
    @Since("3.2.0")
    @GetMapping("/version")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<Skill> getSkillVersion(SkillForm form) throws NacosException {
        form.validate();
        return Result.success(skillProxy.getSkillVersion(form));
    }
    
    /**
      * 下载 Skill 指定版本为 ZIP 文件。
     * Download a specific version of a skill as ZIP file.
     *
     * @param form the skill form containing skillName and version
     * @return ZIP file as ResponseEntity
     * @throws NacosException if the skill or version not found
     */
    @Since("3.2.0")
    @GetMapping("/version/download")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public ResponseEntity<byte[]> downloadSkillVersion(SkillForm form) throws NacosException {
        form.validate();
        Skill skill = skillProxy.downloadSkillVersion(form);
        return SkillRequestUtil.buildSkillZipResponse(skill);
    }
    
    /**
      * 删除 Skill。
     * Delete skill.
     *
     * @param form the skill form to delete
     * @return result of the deletion operation
     * @throws NacosException if the skill deletion fails
     */
    @Since("3.2.0")
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> deleteSkill(SkillForm form) throws NacosException {
        form.validate();
        skillProxy.deleteSkill(form);
        return Result.success("ok");
    }
    
    /**
      * 分页列举 Skill 摘要。
     * List skills.
     *
     * @param skillListForm the skill list form to list
     * @param pageForm      the page form to list
     * @return result of the list operation
     * @throws NacosException if the skill list fails
     */
    @Since("3.2.1")
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<Page<SkillSummary>> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm, PageForm pageForm) throws NacosException {
        skillListForm.validate();
        filterableForm.validate();
        pageForm.validate();
        return Result.success(skillProxy.listSkills(skillListForm, filterableForm, pageForm));
    }
    
    /**
      * 从 ZIP 文件上传 Skill。
     * Upload skill from zip file.
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
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
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
        String skillName = skillProxy.uploadSkillFromZip(uploadRequest);
        return Result.success(skillName);
    }
    
    /**
      * 批量上传多个 Skill（ZIP 内需含一级子目录且各含 SKILL.md）。
     * Batch upload multiple skills from a single zip file. The zip must contain one-level subdirectories,
     * each with its own SKILL.md. Uses best-effort strategy.
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
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
    public Result<BatchUploadResult> batchUploadSkills(HttpServletRequest request,
        @RequestParam(value = "namespaceId", required = false) String namespaceId,
        @RequestParam(value = "overwrite", required = false,
            defaultValue = "false") boolean overwrite,
        @RequestParam("file") MultipartFile file) throws NacosException {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
        byte[] zipBytes = SkillRequestUtil.validateAndExtractZipBytes(file);
        BatchUploadResult result =
            skillProxy.batchUploadSkillsFromZip(namespaceId, zipBytes, overwrite);
        return Result.success(result);
    }
    
    /**
      * 创建 Skill 草稿（校验在 Form 中完成，控制器仅委托）。
     * Create draft. {@link SkillDraftCreateForm#prepareCreateDraftRequest()} validates here; handler only delegates.
     */
    @Since("3.2.0")
    @PostMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> createDraft(SkillDraftCreateForm form) throws NacosException {
        form.prepareCreateDraftRequest();
        return Result.success(skillProxy.createDraft(form));
    }
    
    /**
      * 更新当前草稿内容。
     * Update current draft content.
     */
    @Since("3.2.0")
    @PutMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> updateDraft(SkillUpdateForm form) throws NacosException {
        form.validate();
        skillProxy.updateDraft(form);
        return Result.success("ok");
    }
    
    /**
      * 删除当前草稿版本。
     * Delete current draft version.
     */
    @Since("3.2.0")
    @DeleteMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> deleteDraft(SkillForm form) throws NacosException {
        form.validate();
        skillProxy.deleteDraft(form);
        return Result.success("ok");
    }
    
    /**
      * 提交版本至流水线审核。
     * Submit a version for pipeline review.
     */
    @Since("3.2.0")
    @PostMapping("/submit")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> submit(SkillSubmitForm form) throws NacosException {
        form.validate();
        return Result.success(skillProxy.submit(form));
    }
    
    /**
      * 发布已通过审核的版本。
     * Publish an approved reviewing version.
     */
    @Since("3.2.0")
    @PostMapping("/publish")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> publish(SkillPublishForm form) throws NacosException {
        form.validate();
        skillProxy.publish(form);
        return Result.success("ok");
    }
    
    /**
      * 强制发布 Skill 版本，跳过流水线校验（限管理员）。
     * Force-publish a skill version, bypassing pipeline validation. Accepts draft, reviewing, and reviewed versions.
     * Restricted to admin users only (apiType = ADMIN_API enforces global admin check).
     */
    @Since("3.2.1")
    @PostMapping("/force-publish")
    @Secured(resource = CONSOLE_RESOURCE_NAME_PREFIX
        + "skills", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<String> forcePublish(SkillPublishForm form) throws NacosException {
        form.validate();
        skillProxy.forcePublish(form);
        return Result.success("ok");
    }
    
    /**
      * 将已审核 Skill 版本退回草稿。
     * Re-edit a reviewed skill version, transitioning it back to draft status.
     */
    @Since("3.2.2")
    @PostMapping("/redraft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> redraft(SkillPublishForm form) throws NacosException {
        form.validate();
        skillProxy.redraft(form);
        return Result.success("ok");
    }
    
    /**
      * 更新 Skill 运行时路由标签。
     * Update runtime route labels without changing version status.
     */
    @Since("3.2.0")
    @PutMapping("/labels")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> updateLabels(SkillLabelsUpdateForm form) throws NacosException {
        form.validate();
        skillProxy.updateLabels(form);
        return Result.success("ok");
    }
    
    /**
      * 更新 Skill 业务标签。
     * Update skill biz tags without changing version status.
     */
    @Since("3.2.0")
    @PutMapping("/biz-tags")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> updateBizTags(SkillBizTagsUpdateForm form) throws NacosException {
        form.validate();
        skillProxy.updateBizTags(form);
        return Result.success("ok");
    }
    
    /**
      * 上线 Skill（按 scope 支持版本级或 Skill 级）。
     * Online operation (version-level or skill-level by scope).
     */
    @Since("3.2.0")
    @PostMapping("/online")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> online(SkillOnlineForm form) throws NacosException {
        form.validate();
        skillProxy.online(form);
        return Result.success("ok");
    }
    
    /**
      * 下线 Skill（按 scope 支持版本级或 Skill 级）。
     * Offline operation (version-level or skill-level by scope).
     */
    @Since("3.2.0")
    @PostMapping("/offline")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> offline(SkillOnlineForm form) throws NacosException {
        form.validate();
        skillProxy.offline(form);
        return Result.success("ok");
    }
    
    /**
      * 更新 Skill 可见性范围（PUBLIC 或 PRIVATE）。
     * Update skill visibility scope (PUBLIC or PRIVATE).
     */
    @Since("3.2.0")
    @PutMapping("/scope")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    public Result<String> updateScope(SkillScopeForm form) throws NacosException {
        form.validate();
        skillProxy.updateScope(form);
        return Result.success("ok");
    }
}
