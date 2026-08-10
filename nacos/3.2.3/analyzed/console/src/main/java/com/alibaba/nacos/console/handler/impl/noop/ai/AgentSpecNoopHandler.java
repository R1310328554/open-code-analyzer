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
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.console.handler.ai.AgentSpecHandler;
import com.alibaba.nacos.core.model.form.PageForm;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * AgentSpec 空实现 Handler：AI 模块未启用或 naming/config 未同时可用时注册，全部接口返回 {@link ErrorCode#API_FUNCTION_DISABLED}。
 * Noop implementation of AgentSpec handler.
 * Used when AI module is not enabled or both `naming` and `config` modules are not available.
 *
 * @author nacos
 */
@Service
@ConditionalOnMissingBean(value = AgentSpecHandler.class, ignored = AgentSpecNoopHandler.class)
public class AgentSpecNoopHandler implements AgentSpecHandler {
    
    /** AgentSpec 功能未启用时的统一错误提示文案 */
    private static final String AGENTSPEC_NOT_ENABLED_MESSAGE =
        "Nacos AI AgentSpec module and API required both `naming` and `config` module.";
    
    /** 获取 AgentSpec — 功能未启用时抛出异常 */
    @Override
    public AgentSpecMeta getAgentSpec(AgentSpecForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 获取 AgentSpec 版本 — 功能未启用时抛出异常 */
    @Override
    public AgentSpec getAgentSpecVersion(AgentSpecForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 删除 AgentSpec — 功能未启用时抛出异常 */
    @Override
    public void deleteAgentSpec(AgentSpecForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 分页列出 AgentSpec — 功能未启用时抛出异常 */
    @Override
    public Page<AgentSpecSummary> listAgentSpecs(AgentSpecListForm agentSpecListForm,
        AiResourceFilterableForm filterableForm, PageForm pageForm) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 从 ZIP 上传 AgentSpec — 功能未启用时抛出异常 */
    @Override
    public String uploadAgentSpecFromZip(String namespaceId, byte[] zipBytes, boolean overwrite)
        throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 创建草稿 — 功能未启用时抛出异常 */
    @Override
    public String createDraft(AgentSpecDraftCreateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新草稿 — 功能未启用时抛出异常 */
    @Override
    public void updateDraft(AgentSpecUpdateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 删除草稿 — 功能未启用时抛出异常 */
    @Override
    public void deleteDraft(AgentSpecForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 提交审核 — 功能未启用时抛出异常 */
    @Override
    public String submit(AgentSpecSubmitForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 发布版本 — 功能未启用时抛出异常 */
    @Override
    public void publish(AgentSpecPublishForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 强制发布 — 功能未启用时抛出异常 */
    @Override
    public void forcePublish(AgentSpecPublishForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 重新编辑为草稿 — 功能未启用时抛出异常 */
    @Override
    public void redraft(AgentSpecPublishForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新标签 — 功能未启用时抛出异常 */
    @Override
    public void updateLabels(AgentSpecLabelsUpdateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新业务标签 — 功能未启用时抛出异常 */
    @Override
    public void updateBizTags(AgentSpecBizTagsUpdateForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 切换上下线 — 功能未启用时抛出异常 */
    @Override
    public void changeOnlineStatus(AgentSpecOnlineForm form, boolean online) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
    
    /** 更新可见范围 — 功能未启用时抛出异常 */
    @Override
    public void updateScope(AgentSpecScopeForm form) throws NacosException {
        throw new NacosApiException(NacosException.SERVER_NOT_IMPLEMENTED,
            ErrorCode.API_FUNCTION_DISABLED,
            AGENTSPEC_NOT_ENABLED_MESSAGE);
    }
}
