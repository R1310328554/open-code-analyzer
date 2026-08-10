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

package com.alibaba.nacos.maintainer.client.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaSummary;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionSummary;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;

/**
 * Prompt 维护服务接口：管理提示词模板的列表、版本生命周期与治理元数据。
 *
 * <p>支持草稿创建/更新、流水线 submit/publish/forcePublish、上下线、标签与业务标签等运维能力；
 * 底部保留 3.2.0 兼容的废弃 API。</p>
 *
 * @author nacos
 */
public interface PromptMaintainerService {
    
    /**
     * 分页列出提示词。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key pattern for filtering
     * @param search      search mode: "accurate" or "blur"
     * @param bizTags     biz tags filter (comma-separated)
     * @param pageNo      page number
     * @param pageSize    page size
     * @return paged prompt list
     * @throws NacosException if fail to list prompts
     */
    @Since("3.2.0")
    Page<PromptMetaSummary> listPrompts(String namespaceId, String promptKey, String search,
        String bizTags, int pageNo,
        int pageSize)
        throws NacosException;
    
    /**
     * 在默认命名空间分页列出提示词。
     *
     * @param promptKey prompt key pattern for filtering
     * @param pageNo    page number
     * @param pageSize  page size
     * @return paged prompt list
     * @throws NacosException if fail to list prompts
     */
    @Since("3.2.0")
    default Page<PromptMetaSummary> listPrompts(String promptKey, int pageNo, int pageSize)
        throws NacosException {
        return listPrompts(Constants.DEFAULT_NAMESPACE_ID, promptKey, "blur", null, pageNo,
            pageSize);
    }
    
    /**
     * 删除指定命名空间下的提示词。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @return true if delete success
     * @throws NacosException if fail to delete prompt
     */
    @Since("3.2.0")
    boolean deletePrompt(String namespaceId, String promptKey) throws NacosException;
    
    /**
     * 在默认命名空间删除提示词。
     *
     * @param promptKey prompt key
     * @return true if delete success
     * @throws NacosException if fail to delete prompt
     */
    @Since("3.2.0")
    default boolean deletePrompt(String promptKey) throws NacosException {
        return deletePrompt(Constants.DEFAULT_NAMESPACE_ID, promptKey);
    }
    
    /**
     * 分页列出提示词版本。
     *
     * @param namespaceId the namespace id
     * @param promptKey   the prompt key
     * @param pageNo      the page no
     * @param pageSize    the page size
     * @return the page
     * @throws NacosException the nacos exception
     */
    @Since("3.2.0")
    Page<PromptVersionSummary> listPromptVersions(String namespaceId, String promptKey, int pageNo,
        int pageSize)
        throws NacosException;
    
    /**
     * List prompt versions page.
     *
     * @param promptKey the prompt key
     * @param pageNo    the page no
     * @param pageSize  the page size
     * @return the page
     * @throws NacosException the nacos exception
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    default Page<PromptVersionSummary> listPromptVersions(String promptKey, int pageNo,
        int pageSize)
        throws NacosException {
        return listPromptVersions(Constants.DEFAULT_NAMESPACE_ID, promptKey, pageNo, pageSize);
    }
    
    // ========== 生命周期 API ==========
    
    /**
     * 获取提示词治理详情（含版本治理信息与全部版本摘要）。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @return prompt governance detail
     * @throws NacosException if fail to get detail
     */
    @Since("3.2.1")
    PromptMetaInfo getPromptGovernanceDetail(String namespaceId, String promptKey)
        throws NacosException;
    
    /**
     * 获取指定版本详情。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param version     version string
     * @return prompt version info
     * @throws NacosException if fail to get version detail
     */
    @Since("3.2.1")
    PromptVersionInfo getVersionDetail(String namespaceId, String promptKey, String version)
        throws NacosException;
    
    /**
     * 创建草稿版本。
     *
     * @param namespaceId    namespace ID
     * @param promptKey      prompt key
     * @param basedOnVersion base version to fork from (optional)
     * @param targetVersion  target draft version (optional)
     * @param template       prompt template content
     * @param variables      variable definitions JSON (optional)
     * @param commitMsg      commit message (optional)
     * @param description    prompt description (optional, only on first creation)
     * @param bizTags        biz tags JSON (optional, only on first creation)
     * @return created draft version string
     * @throws NacosException if fail to create draft
     */
    @Since("3.2.1")
    String createDraft(String namespaceId, String promptKey, String basedOnVersion,
        String targetVersion,
        String template, String variables, String commitMsg, String description, String bizTags)
        throws NacosException;
    
    /**
     * 更新当前草稿内容。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param template    updated template content
     * @param variables   updated variable definitions JSON (optional)
     * @param commitMsg   updated commit message (optional)
     * @throws NacosException if fail to update draft
     */
    @Since("3.2.1")
    void updateDraft(String namespaceId, String promptKey, String template, String variables,
        String commitMsg)
        throws NacosException;
    
    /**
     * 删除当前草稿。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @throws NacosException if fail to delete draft
     */
    @Since("3.2.1")
    void deleteDraft(String namespaceId, String promptKey) throws NacosException;
    
    /**
     * 提交版本进入流水线审核。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param version     version to submit (optional, defaults to editing version)
     * @return submitted version string
     * @throws NacosException if fail to submit
     */
    @Since("3.2.1")
    String submit(String namespaceId, String promptKey, String version) throws NacosException;
    
    /**
     * 发布已审核通过的版本。
     *
     * @param namespaceId       namespace ID
     * @param promptKey         prompt key
     * @param version           version to publish
     * @param updateLatestLabel whether to update the latest label
     * @throws NacosException if fail to publish
     */
    @Since("3.2.1")
    void publish(String namespaceId, String promptKey, String version, Boolean updateLatestLabel)
        throws NacosException;
    
    /**
     * 强制发布版本（跳过流水线校验）。
     *
     * @param namespaceId       namespace ID
     * @param promptKey         prompt key
     * @param version           version to force-publish
     * @param updateLatestLabel whether to update the latest label
     * @throws NacosException if fail to force-publish
     */
    @Since("3.2.1")
    void forcePublish(String namespaceId, String promptKey, String version,
        Boolean updateLatestLabel)
        throws NacosException;
    
    /**
     * 将已审核版本退回草稿重新编辑。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param version     version to re-edit
     * @return true if redraft success
     * @throws NacosException if fail to redraft
     */
    @Since("3.2.2")
    boolean redraft(String namespaceId, String promptKey, String version) throws NacosException;
    
    /**
     * 指定版本上下线。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param version     version to operate
     * @param online      true for online, false for offline
     * @throws NacosException if fail to change status
     */
    @Since("3.2.1")
    void changeOnlineStatus(String namespaceId, String promptKey, String version, boolean online)
        throws NacosException;
    
    /**
     * 更新标签映射 JSON。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param labels      labels JSON string
     * @throws NacosException if fail to update labels
     */
    @Since("3.2.1")
    void updateLabels(String namespaceId, String promptKey, String labels) throws NacosException;
    
    /**
     * 更新提示词描述。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param description new description
     * @throws NacosException if fail to update description
     */
    @Since("3.2.1")
    void updateDescription(String namespaceId, String promptKey, String description)
        throws NacosException;
    
    /**
     * 更新业务标签 JSON。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param bizTags     biz tags JSON string
     * @throws NacosException if fail to update biz tags
     */
    @Since("3.2.1")
    void updateBizTags(String namespaceId, String promptKey, String bizTags) throws NacosException;
    
    // ========== 旧版兼容 API（已废弃） ==========
    
    /**
     * 旧版：获取提示词元数据。
     *
     * @deprecated Use {@link #getPromptGovernanceDetail} instead.
     */
    @Since("3.2.0")
    @Deprecated
    PromptMetaInfo getPromptMeta(String namespaceId, String promptKey) throws NacosException;
    
    /**
     * Legacy get prompt metadata with default namespace.
     *
     * @deprecated Use {@link #getPromptGovernanceDetail} instead.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    @Deprecated
    default PromptMetaInfo getPromptMeta(String promptKey) throws NacosException {
        return getPromptMeta(Constants.DEFAULT_NAMESPACE_ID, promptKey);
    }
    
    /**
     * 旧版：按版本/标签/latest 查询提示词详情。
     *
     * @deprecated Use {@link #getVersionDetail} instead.
     */
    @Since("3.2.0")
    @Deprecated
    PromptVersionInfo queryPromptDetail(String namespaceId, String promptKey, String version,
        String label)
        throws NacosException;
    
    /**
     * 旧版：为提示词版本绑定标签。
     *
     * @deprecated Use {@link #updateLabels} instead.
     */
    @Since("3.2.0")
    @Deprecated
    boolean bindLabel(String namespaceId, String promptKey, String label, String version)
        throws NacosException;
    
    /**
     * 旧版：从提示词解绑标签。
     *
     * @deprecated Use {@link #updateLabels} instead.
     */
    @Since("3.2.0")
    @Deprecated
    boolean unbindLabel(String namespaceId, String promptKey, String label) throws NacosException;
    
    /**
     * 旧版：一次性发布提示词新版本。
     *
     * @deprecated Use {@link #createDraft} + {@link #submit} instead.
     */
    @Since("3.2.0")
    @Deprecated
    boolean publishPrompt(String namespaceId, String promptKey, String version, String template,
        String commitMsg, String description, String bizTags) throws NacosException;
    
    /**
     * Legacy publish with variable definitions.
     *
     * @deprecated Use {@link #createDraft} + {@link #submit} instead.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    @Deprecated
    default boolean publishPrompt(String namespaceId, String promptKey, String version,
        String template,
        String commitMsg, String description, String bizTags, String variables)
        throws NacosException {
        return publishPrompt(namespaceId, promptKey, version, template, commitMsg, description,
            bizTags);
    }
    
    /**
     * Legacy publish without tags.
     *
     * @deprecated Use {@link #createDraft} + {@link #submit} instead.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    @Deprecated
    default boolean publishPrompt(String namespaceId, String promptKey, String version,
        String template,
        String commitMsg, String description) throws NacosException {
        return publishPrompt(namespaceId, promptKey, version, template, commitMsg, description,
            (String) null);
    }
    
    /**
     * Legacy publish with default namespace.
     *
     * @deprecated Use {@link #createDraft} + {@link #submit} instead.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    @Deprecated
    default boolean publishPrompt(String promptKey, String version, String template,
        String commitMsg)
        throws NacosException {
        return publishPrompt(Constants.DEFAULT_NAMESPACE_ID, promptKey, version, template,
            commitMsg, null,
            (String) null);
    }
    
    /**
     * 旧版：更新提示词元数据（描述与标签）。
     *
     * @deprecated Use {@link #updateDescription} and {@link #updateBizTags} instead.
     */
    @Since("3.2.0")
    @Deprecated
    boolean updatePromptMetadata(String namespaceId, String promptKey, String description,
        String bizTags)
        throws NacosException;
    
    /**
     * Legacy update prompt metadata (description only).
     *
     * @deprecated Use {@link #updateDescription} instead.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    @Deprecated
    default boolean updatePromptMetadata(String namespaceId, String promptKey, String description)
        throws NacosException {
        return updatePromptMetadata(namespaceId, promptKey, description, null);
    }
}
