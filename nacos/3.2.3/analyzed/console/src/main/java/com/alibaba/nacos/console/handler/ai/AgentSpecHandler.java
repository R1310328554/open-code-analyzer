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
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecDraftCreateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecBizTagsUpdateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecLabelsUpdateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecListForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecOnlineForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecPublishForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecScopeForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecSubmitForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecUpdateForm;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecMeta;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.core.model.form.PageForm;

/**
 * AgentSpec 控制台处理器接口：规格草稿、提审、发布、标签与可见范围等全生命周期管理。
 * AgentSpec handler.
 *
 * @author nacos
 */
public interface AgentSpecHandler {
    
    /**
     * 获取 AgentSpec 管理端元数据详情。
     * Get agentspec.
     *
     * @param form AgentSpec 查询表单
     * @return 管理端 AgentSpec 元数据
     * @throws NacosException Nacos 业务异常
     */
    AgentSpecMeta getAgentSpec(AgentSpecForm form) throws NacosException;
    
    /**
     * 获取指定版本的 AgentSpec 完整内容。
     * Get agentspec version detail. Returns full agentspec content for a specific version.
     *
     * @param form 含版本号的 AgentSpec 表单
     * @return 完整 AgentSpec 正文
     * @throws NacosException Nacos 业务异常
     */
    AgentSpec getAgentSpecVersion(AgentSpecForm form) throws NacosException;
    
    /**
     * 删除 AgentSpec 及其关联版本。
     * Delete agentspec.
     *
     * @param form AgentSpec 定位表单
     * @throws NacosException Nacos 业务异常
     */
    void deleteAgentSpec(AgentSpecForm form) throws NacosException;
    
    /**
     * 分页列出 AgentSpec 摘要，支持资源过滤。
     * List agentspecs.
     *
     * @param agentSpecListForm 列表查询表单
     * @param pageForm 分页参数
     * @return AgentSpec 摘要分页
     * @throws NacosException Nacos 业务异常
     */
    Page<AgentSpecSummary> listAgentSpecs(AgentSpecListForm agentSpecListForm,
        AiResourceFilterableForm filterableForm, PageForm pageForm) throws NacosException;
    
    /**
     * 从 ZIP 包上传 AgentSpec（默认不覆盖已有草稿）。
     * Upload agentspec from zip file.
     *
     * @param namespaceId 命名空间 ID
     * @param zipBytes ZIP 文件字节
     * @return 导入后的 AgentSpec 名称
     * @throws NacosException 上传失败
     */
    default String uploadAgentSpecFromZip(String namespaceId, byte[] zipBytes)
        throws NacosException {
        return uploadAgentSpecFromZip(namespaceId, zipBytes, false);
    }
    
    /**
     * Upload agentspec from zip file.
     *
     * @param namespaceId namespace ID
     * @param zipBytes zip file bytes
     * @param overwrite 当 AgentSpec 已存在时是否覆盖当前可编辑草稿
     * @return agentspec name
     * @throws NacosException if upload failed
     */
    String uploadAgentSpecFromZip(String namespaceId, byte[] zipBytes, boolean overwrite)
        throws NacosException;
    
    /**
     * 基于最新版或指定版本创建新的草稿版本。
     * Create draft version based on latest or a specified version.
     *
     * @param form 草稿创建表单
     * @return 新草稿版本号
     * @throws NacosException 操作失败
     */
    String createDraft(AgentSpecDraftCreateForm form) throws NacosException;
    
    /**
     * 更新当前草稿内容。
     * Update current draft content.
     *
     * @param form 更新表单
     * @throws NacosException 操作失败
     */
    void updateDraft(AgentSpecUpdateForm form) throws NacosException;
    
    /**
     * 删除当前草稿版本。
     * Delete current draft version.
     *
     * @param form AgentSpec 定位表单
     * @throws NacosException 操作失败
     */
    void deleteDraft(AgentSpecForm form) throws NacosException;
    
    /**
     * 提交版本进入流水线审核。
     * Submit a version for pipeline review.
     *
     * @param form 提交表单
     * @return 提交结果（如 pipeline id）
     * @throws NacosException 操作失败
     */
    String submit(AgentSpecSubmitForm form) throws NacosException;
    
    /**
     * 发布已通过审核的版本。
     * Publish an approved reviewing version.
     *
     * @param form 发布表单
     * @throws NacosException 操作失败
     */
    void publish(AgentSpecPublishForm form) throws NacosException;
    
    /**
     * 强制发布版本，跳过流水线校验。
     * Force-publish a version, bypassing pipeline validation.
     *
     * @param form 发布表单
     * @throws NacosException 操作失败
     */
    void forcePublish(AgentSpecPublishForm form) throws NacosException;
    
    /**
     * 将已审核版本重新编辑为草稿状态。
     * Re-edit a reviewed version, transitioning it back to draft status.
     *
     * @param form 含命名空间、名称与版本的表单
     * @throws NacosException 操作失败
     */
    void redraft(AgentSpecPublishForm form) throws NacosException;
    
    /**
     * 更新运行时路由标签，不改变版本状态。
     * Update runtime route labels without changing version status.
     *
     * @param form 标签更新表单
     * @throws NacosException 操作失败
     */
    void updateLabels(AgentSpecLabelsUpdateForm form) throws NacosException;
    
    /**
     * 更新 AgentSpec 业务标签，不改变版本状态。
     * Update agentspec biz tags without changing version status.
     *
     * @param form 业务标签更新表单
     * @throws NacosException 操作失败
     */
    void updateBizTags(AgentSpecBizTagsUpdateForm form) throws NacosException;
    
    /**
     * 切换 AgentSpec 上线/下线状态。
     * Change online/offline status.
     *
     * @param form 上下线表单
     * @param online true 表示上线，false 表示下线
     * @throws NacosException 操作失败
     */
    void changeOnlineStatus(AgentSpecOnlineForm form, boolean online) throws NacosException;
    
    /**
     * 更新 AgentSpec 可见范围。
     * Update agentspec visibility scope.
     *
     * @param form 可见范围更新表单
     * @throws NacosException 操作失败
     */
    void updateScope(AgentSpecScopeForm form) throws NacosException;
}
