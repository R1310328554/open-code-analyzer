/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.form;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.visibility.constant.VisibilityConstants;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 资源列表 API 的通用可筛选表单（Skill、AgentSpec 等）。
 *
 * <p>携带适用于全部 AI 资源类型的可选筛选条件；字段可为空，未指定时不应用对应过滤，行为与旧版 API 完全兼容。</p>
 *
 * <p>后续通用筛选字段（如 {@code bizTag}）应集中添加于此，避免在各资源列表表单中重复定义。</p>
 *
 * @author nacos
 */
public class AiResourceFilterableForm implements NacosForm, Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 按资源所有者（创建者身份）可选过滤。
     *
     * <ul>
     *   <li>管理员：可指定任意 owner 或留空查看全部。</li>
     *   <li>非管理员：应仅传自身身份或留空。</li>
     * </ul>
     * {@code null} 或空时不应用 owner 过滤。
     */
    private String owner;
    
    /**
     * 按可见性范围可选过滤。
     *
     * <p>取值 {@code PUBLIC} 或 {@code PRIVATE}（不区分大小写）；为空时不限制，返回调用者有权限看到的公开与私有资源。</p>
     */
    private String scope;
    
    /**
     * 按业务标签可选过滤。
     *
     * <p>指定时仅返回 {@code bizTags} 列包含该值的资源（模糊匹配）；为空时不应用 bizTag 过滤。</p>
     */
    private String bizTag;
    
    @Override
    public void validate() throws NacosApiException {
        if (StringUtils.isNotBlank(scope)
            && !VisibilityConstants.SCOPE_PUBLIC.equalsIgnoreCase(scope)
            && !VisibilityConstants.SCOPE_PRIVATE.equalsIgnoreCase(scope)) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Request parameter `scope` must be PUBLIC or PRIVATE.");
        }
    }
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getBizTag() {
        return bizTag;
    }
    
    public void setBizTag(String bizTag) {
        this.bizTag = bizTag;
    }
}
