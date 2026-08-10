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

package com.alibaba.nacos.ai.form.skills.client;

import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Skill search form for client runtime.
 * <p>客户端运行时 Skill 搜索表单，keyword 可选用于模糊匹配 Skill 名称或描述。</p>
 *
 * @author nacos
 */
public class SkillSearchForm {
    
    /** 命名空间 ID，为空时默认 public。 */
    private String namespaceId;
    
    /** 搜索关键词，可选，用于模糊匹配。 */
    private String keyword;
    
    /**
     * Validate and normalize query parameters.
     * <p>校验并规范化查询参数，keyword 为可选字段。</p>
     */
    public void validate() {
        // keyword 为可选，仅规范化命名空间
        if (StringUtils.isBlank(namespaceId)) {
            namespaceId = "public"; // 默认 public 命名空间
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
