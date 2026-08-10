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

package com.alibaba.nacos.ai.form.agentspecs.client;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * AgentSpec search form for client runtime.
 * <p>客户端运行时 AgentSpec 关键字搜索表单，keyword 为可选过滤条件，命名空间为空时使用默认值。</p>
 *
 * @author nacos
 */
public class AgentSpecSearchForm {
    
    private String namespaceId;
    
    /** 搜索关键字，可选，用于模糊匹配 AgentSpec 名称或描述。 */
    private String keyword;
    
    /**
     * Validate and normalize query parameters.
     * <p>校验并规范化查询参数，补全默认命名空间。</p>
     */
    public void validate() {
        // keyword 为可选参数，允许空值表示列出全部
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = Constants.AgentSpecs.AGENTSPEC_DEFAULT_NAMESPACE;
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
