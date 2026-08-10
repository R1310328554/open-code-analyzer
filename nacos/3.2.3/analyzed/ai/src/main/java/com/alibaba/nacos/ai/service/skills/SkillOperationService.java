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

package com.alibaba.nacos.ai.service.skills;

import com.alibaba.nacos.api.ai.model.skills.BatchUploadResult;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillBasicInfo;
import com.alibaba.nacos.api.ai.model.skills.SkillMeta;
import com.alibaba.nacos.api.ai.model.skills.SkillSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;

import java.util.Map;

/**
 * Skill operation service.
 * <p>Skill 资源操作接口：管理端 CRUD、客户端查询、草稿/发布流水线及可见性 scope。</p>
 *
 * @author nacos
 */
public interface SkillOperationService {
    
    // ========== 管理端 API ==========
    
    /**
     * Upload skill from zip file.
     * <p>从 ZIP 上传 Skill。</p>
     *
     * @param request upload request
     * @return skill name
     * @throws NacosException if upload failed
     */
    String uploadSkillFromZip(SkillUploadRequest request) throws NacosException;
    
    /**
     * Batch upload multiple skills from a single zip archive. The zip must contain one-level subdirectories,
     * <p>从单个 ZIP 批量上传多个 Skill（一级子目录各含 SKILL.md），尽力处理并返回成功/失败列表。</p>
     * each with its own SKILL.md. Uses best-effort strategy: processes all skills individually, returning
     * succeeded and failed lists.
     *
     * @param namespaceId namespace ID
     * @param zipBytes zip file bytes containing multiple skill subdirectories
     * @param overwrite whether to overwrite existing drafts
     * @return batch upload result with succeeded and failed skill names
     * @throws NacosException if zip parsing fails entirely (e.g. invalid format, no SKILL.md found)
     */
    BatchUploadResult batchUploadSkillsFromZip(String namespaceId, byte[] zipBytes,
        boolean overwrite)
        throws NacosException;
    
    /**
     * Bootstrap skill from zip file as an online skill.
     * <p>从 ZIP 引导内置 Skill 为在线状态，跳过草稿/流水线。</p>
     *
     * <p>This is intended for server-side built-in data initialization and bypasses draft/pipeline flow.</p>
     *
     * @param namespaceId namespace ID
     * @param zipBytes zip file bytes
     * @throws NacosException if bootstrap failed
     */
    void bootstrapSkillFromZip(String namespaceId, byte[] zipBytes) throws NacosException;
    
    /**
     * Bootstrap skill from zip file as an online skill with source metadata.
     *
     * @param namespaceId namespace ID
     * @param zipBytes zip file bytes
     * @param from source identifier, e.g. github.com/nacos
     * @throws NacosException if bootstrap failed
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    default void bootstrapSkillFromZip(String namespaceId, byte[] zipBytes, String from)
        throws NacosException {
        bootstrapSkillFromZip(namespaceId, zipBytes);
    }
    
    /**
     * Get skill detail for admin usage. Returns version governance metadata and all version summaries.
     * <p>管理端获取 Skill 治理元数据与各版本摘要。</p>
     *
     * @param namespaceId namespace ID
     * @param skillName skill name
     * @return skill admin detail (governance info + version summaries)
     * @throws NacosException if skill not found
     */
    SkillMeta getSkillDetail(String namespaceId, String skillName) throws NacosException;
    
    /**
     * Get skill version detail for admin usage. Returns full skill content for a specific version, used for viewing or editing.
     * <p>管理端获取指定版本的完整 Skill 内容。</p>
     *
     * @param namespaceId namespace ID
     * @param skillName skill name
     * @param version target version
     * @return full skill content for the specified version
     * @throws NacosException if skill or version not found
     */
    Skill getSkillVersionDetail(String namespaceId, String skillName, String version)
        throws NacosException;
    
    /**
     * Download skill version. Semantically identical to {@link #getSkillVersionDetail} but provides a separate
     * <p>下载指定版本 Skill，语义同 getSkillVersionDetail，独立入口便于统计下载次数。</p>
     * entry point so that download events can be tracked independently (e.g. download count statistics).
     *
     * @param namespaceId namespace ID
     * @param skillName skill name
     * @param version target version
     * @return full skill content for the specified version
     * @throws NacosException if skill or version not found
     */
    Skill downloadSkillVersion(String namespaceId, String skillName, String version)
        throws NacosException;
    
    /**
     * Delete skill.
     * <p>删除整个 Skill 资源。</p>
     *
     * @param namespaceId namespace ID
     * @param skillName skill name
     * @throws NacosException if delete failed
     */
    void deleteSkill(String namespaceId, String skillName) throws NacosException;
    
    /**
     * List skills with pagination for admin usage. Returns full governance metadata.
     * <p>管理端分页列出 Skill，含完整治理元数据。</p>
     *
     * @param namespaceId namespace ID
     * @param skillName skill name (for search)
     * @param search search type (accurate/blur)
     * @param pageNo page number
     * @param pageSize page size
     * @return skill admin list page with governance metadata
     * @throws NacosException if query failed
     */
    Page<SkillSummary> listSkills(String namespaceId, String skillName, String search, int pageNo,
        int pageSize) throws NacosException;
    
    /**
     * List skills with pagination and optional ordering for admin usage.
     * <p>管理端分页列表，支持 orderBy 排序。</p>
     *
     * @param namespaceId namespace ID
     * @param skillName skill name (for search)
     * @param search search type (accurate/blur)
     * @param orderBy sort field (e.g. "download_count"), null defaults to gmt_modified
     * @param pageNo page number
     * @param pageSize page size
     * @return skill admin list page with governance metadata
     * @throws NacosException if query failed
     */
    Page<SkillSummary> listSkills(String namespaceId, String skillName, String search,
        String orderBy,
        int pageNo, int pageSize) throws NacosException;
    
    /**
     * List skills with pagination, optional ordering, and additional filter criteria for admin usage.
     * <p>扩展分页列表：支持 owner、scope 过滤。</p>
     *
     * <p>Backward-compatible: when {@code owner} and {@code scope} are both {@code null}/empty,
     * the behaviour is identical to
     * {@link #listSkills(String, String, String, String, int, int)}.</p>
     *
     * @param namespaceId namespace ID
     * @param skillName   skill name (for search)
     * @param search      search type (accurate/blur)
     * @param orderBy     sort field (e.g. "download_count"), null defaults to gmt_modified
     * @param owner       optional filter by resource owner; null or empty means no owner filter
     * @param scope       optional filter by visibility scope ("PUBLIC"/"PRIVATE"); null or empty means no scope filter
     * @param pageNo      page number
     * @param pageSize    page size
     * @return skill admin list page with governance metadata
     * @throws NacosException if query failed
     */
    default Page<SkillSummary> listSkills(String namespaceId, String skillName, String search,
        String orderBy,
        String owner, String scope, int pageNo, int pageSize) throws NacosException {
        return listSkills(namespaceId, skillName, search, orderBy, owner, scope, null, pageNo,
            pageSize);
    }
    
    /**
     * List skills with pagination, optional ordering, and additional filter criteria including bizTag for admin usage.
     * <p>扩展分页列表：额外支持 bizTag 模糊过滤。</p>
     *
     * <p>Backward-compatible: when {@code owner}, {@code scope} and {@code bizTag} are all {@code null}/empty,
     * the behaviour is identical to
     * {@link #listSkills(String, String, String, String, int, int)}.</p>
     *
     * @param namespaceId namespace ID
     * @param skillName   skill name (for search)
     * @param search      search type (accurate/blur)
     * @param orderBy     sort field (e.g. "download_count"), null defaults to gmt_modified
     * @param owner       optional filter by resource owner; null or empty means no owner filter
     * @param scope       optional filter by visibility scope ("PUBLIC"/"PRIVATE"); null or empty means no scope filter
     * @param bizTag      optional filter by business tag (fuzzy match on bizTags column); null or empty means no filter
     * @param pageNo      page number
     * @param pageSize    page size
     * @return skill admin list page with governance metadata
     * @throws NacosException if query failed
     */
    Page<SkillSummary> listSkills(String namespaceId, String skillName, String search,
        String orderBy,
        String owner, String scope, String bizTag, int pageNo, int pageSize) throws NacosException;
    
    /**
     * Create a new draft version.
     * <p>创建新草稿版本；全新 Skill 或无基线时需 initialContent。</p>
     * <p>
     * {@code initialContent} is required for a brand-new skill or when no published version exists to fork from.
     * When forking from an existing version, {@code initialContent} must be null and content is copied from the base
     * version.
     * </p>
     *
     * @param namespaceId namespace ID
     * @param name skill name
     * @param basedOnVersion base version to fork from (optional; defaults per server rules when resolving base)
     * @param targetVersion target draft version to create (optional; auto-generated when empty)
     * @param initialContent full skill from {@code skillCard}, or null when forking
     * @param commitMsg version-level commit message describing what changed (optional; stored empty when not provided,
     *                  not derived from skill description)
     * @return created draft version
     */
    String createDraft(String namespaceId, String name, String basedOnVersion, String targetVersion,
        Skill initialContent, String commitMsg)
        throws NacosException;
    
    /**
     * Update existing draft content.
     * <p>更新当前草稿版本的完整 Skill 内容。</p>
     *
     * @param namespaceId namespace ID
     * @param draftSkill full skill content to write into draft
     * @param commitMsg version-level commit message describing what changed (optional; updates version desc when not blank)
     */
    void updateDraft(String namespaceId, Skill draftSkill, String commitMsg) throws NacosException;
    
    /**
     * Delete current draft and release working pointer.
     * <p>删除当前草稿并释放 working 指针。</p>
     */
    void deleteDraft(String namespaceId, String name) throws NacosException;
    
    /**
     * Submit a draft version for publish. If no pipeline plugins configured, will directly publish.
     * <p>提交草稿进入发布流程；无流水线插件时直接发布。</p>
     *
     * @return submit result identifier or current version
     */
    String submit(String namespaceId, String name, String version) throws NacosException;
    
    /**
     * Publish a reviewing version. Must have pipeline all passed when pipeline exists.
     * <p>发布审核中版本；存在流水线时需全部节点通过。</p>
     */
    void publish(String namespaceId, String name, String version, boolean updateLatestLabel)
        throws NacosException;
    
    /**
     * Force-publish a skill version, bypassing pipeline validation.
     * <p>强制发布指定版本，跳过流水线校验；仅管理员可用。</p>
     * Accepts draft, reviewing, and reviewed versions.
     * Should only be invoked by admin users.
     *
     * @param namespaceId      namespace ID
     * @param name             skill name
     * @param version          version to force-publish
     * @param updateLatestLabel whether to update the "latest" label
     */
    void forcePublish(String namespaceId, String name, String version, boolean updateLatestLabel)
        throws NacosException;
    
    /**
     * Re-edit a reviewed version, transitioning it back to draft.
     * <p>将已审核版本退回草稿状态以便重新编辑。</p>
     *
     * @param namespaceId namespace
     * @param name        skill name
     * @param version     version to re-edit
     */
    void redraft(String namespaceId, String name, String version) throws NacosException;
    
    /**
     * Update labels mapping (label -> version) without changing any version status.
     * <p>更新 label→version 映射，不改变各版本状态。</p>
     */
    void updateLabels(String namespaceId, String name, Map<String, String> labels)
        throws NacosException;
    
    /**
     * Update skill biz tags JSON.
     * <p>更新 Skill 业务标签 JSON。</p>
     */
    void updateBizTags(String namespaceId, String name, String bizTags) throws NacosException;
    
    /**
     * Online/offline operation.
     * <p>上线/下线：scope 为 skill 时全局启停，否则针对指定版本。</p>
     *
     * @param scope "skill" for global enable/disable, otherwise version scope
     * @param version version to operate when scope is version-level
     * @param online true means online/enable, false means offline/disable
     */
    void changeOnlineStatus(String namespaceId, String name, String scope, String version,
        boolean online) throws NacosException;
    
    /**
     * Update skill visibility scope (PUBLIC or PRIVATE). Only the owner or users with explicit write permission can
     * <p>更新可见性 scope（PUBLIC/PRIVATE）；仅 owner 或有写权限用户可操作。</p>
     * change the scope.
     *
     * @param namespaceId namespace ID
     * @param name        skill name
     * @param scope       target scope: PUBLIC or PRIVATE
     * @throws NacosException if skill not found or no permission
     */
    void updateScope(String namespaceId, String name, String scope) throws NacosException;
    
    // ========== 客户端 API ==========
    
    /**
     * Search skills for runtime client usage. Only returns enabled skills that have at least one online version.
     * <p>客户端运行时搜索：仅返回已启用且至少有一个在线版本的 Skill 摘要。</p>
     * Returns only name and description for client consumption.
     *
     * @param namespaceId namespace ID
     * @param keyword keyword (optional)
     * @param pageNo page number
     * @param pageSize page size
     */
    Page<SkillBasicInfo> searchSkills(String namespaceId, String keyword, int pageNo, int pageSize)
        throws NacosException;
    
    /**
     * Query skill for runtime client usage. Priority: label > version > latest(label).
     * <p>客户端查询 Skill，解析优先级：label &gt; version &gt; latest(label)。</p>
     *
     * @param namespaceId namespace ID
     * @param name skill name
     * @param version explicit version (optional)
     * @param label route label, e.g. latest/stable (optional)
     */
    Skill querySkill(String namespaceId, String name, String version, String label)
        throws NacosException;
}
