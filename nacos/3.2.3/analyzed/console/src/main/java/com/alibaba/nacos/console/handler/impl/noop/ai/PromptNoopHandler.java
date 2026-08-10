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

import com.alibaba.nacos.ai.form.prompt.PromptForm;
import com.alibaba.nacos.ai.form.prompt.PromptHistoryForm;
import com.alibaba.nacos.ai.form.prompt.PromptListForm;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaSummary;
import com.alibaba.nacos.api.ai.model.prompt.PromptVariable;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionSummary;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.console.handler.ai.PromptHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Prompt 空实现 Handler：AI 模块未启用或 naming/config 未同时可用时注册，全部接口返回 {@link ErrorCode#API_FUNCTION_DISABLED}。
 * Noop implementation of Prompt handler. Used when AI module is not enabled.
 *
 * @author nacos
 */
@Service
@ConditionalOnMissingBean(value = PromptHandler.class, ignored = PromptNoopHandler.class)
public class PromptNoopHandler implements PromptHandler {
    
    /** Prompt 功能未启用时的统一错误提示文案 */
    private static final String PROMPT_NOT_ENABLED_MESSAGE =
        "Nacos AI Prompt module and API required both `naming` and `config` module.";
    
    /** 构造 API 功能未启用的统一异常 */
    private NacosApiException notImplemented() {
        return new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            PROMPT_NOT_ENABLED_MESSAGE);
    }
    
    // ========== 通用 API ==========
    
    /** 删除 Prompt — 功能未启用时抛出异常 */
    @Override
    public boolean deletePrompt(PromptForm form, String srcUser, String srcIp)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 分页列出 Prompt — 功能未启用时抛出异常 */
    @Override
    public Page<PromptMetaSummary> listPrompts(PromptListForm form) throws NacosException {
        throw notImplemented();
    }
    
    /** 分页列出 Prompt 版本历史 — 功能未启用时抛出异常 */
    @Override
    public Page<PromptVersionSummary> listPromptVersions(PromptHistoryForm form)
        throws NacosException {
        throw notImplemented();
    }
    
    // ========== 生命周期 API ==========
    
    /** 查询 Prompt 治理元信息 — 功能未启用时抛出异常 */
    @Override
    public PromptMetaInfo getPromptGovernanceDetail(String namespaceId, String promptKey)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 查询指定版本详情 — 功能未启用时抛出异常 */
    @Override
    public PromptVersionInfo getVersionDetail(String namespaceId, String promptKey, String version)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 下载 Prompt 版本内容 — 功能未启用时抛出异常 */
    @Override
    public PromptVersionInfo downloadPromptVersion(String namespaceId, String promptKey,
        String version)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 创建 Prompt 草稿 — 功能未启用时抛出异常 */
    @Override
    public String createDraft(String namespaceId, String promptKey, String basedOnVersion,
        String targetVersion,
        String template, List<PromptVariable> variables, String commitMsg, String description,
        String bizTags)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 更新 Prompt 草稿 — 功能未启用时抛出异常 */
    @Override
    public void updateDraft(String namespaceId, String promptKey, String template,
        List<PromptVariable> variables,
        String commitMsg) throws NacosException {
        throw notImplemented();
    }
    
    /** 删除 Prompt 草稿 — 功能未启用时抛出异常 */
    @Override
    public void deleteDraft(String namespaceId, String promptKey) throws NacosException {
        throw notImplemented();
    }
    
    /** 提交 Prompt 审核 — 功能未启用时抛出异常 */
    @Override
    public String submit(String namespaceId, String promptKey, String version)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 发布 Prompt 版本 — 功能未启用时抛出异常 */
    @Override
    public void publish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 强制发布 Prompt 版本 — 功能未启用时抛出异常 */
    @Override
    public void forcePublish(String namespaceId, String promptKey, String version,
        boolean updateLatestLabel)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 将已发布版本退回草稿 — 功能未启用时抛出异常 */
    @Override
    public void redraft(String namespaceId, String promptKey, String version)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 切换 Prompt 上下线状态 — 功能未启用时抛出异常 */
    @Override
    public void changeOnlineStatus(String namespaceId, String promptKey, String version,
        boolean online)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 更新 Prompt 标签 — 功能未启用时抛出异常 */
    @Override
    public void updateLabels(String namespaceId, String promptKey, Map<String, String> labels)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 更新 Prompt 描述 — 功能未启用时抛出异常 */
    @Override
    public void updateDescription(String namespaceId, String promptKey, String description)
        throws NacosException {
        throw notImplemented();
    }
    
    /** 更新 Prompt 业务标签 — 功能未启用时抛出异常 */
    @Override
    public void updateBizTags(String namespaceId, String promptKey, String bizTags)
        throws NacosException {
        throw notImplemented();
    }
}
