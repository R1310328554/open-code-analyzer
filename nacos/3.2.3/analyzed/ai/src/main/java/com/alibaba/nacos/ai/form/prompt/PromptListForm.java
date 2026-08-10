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

package com.alibaba.nacos.ai.form.prompt;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Prompt list query form.
 * <p>Prompt 分页列表查询表单，支持按 promptKey、bizTags 过滤，以及精确/模糊搜索模式与分页参数。</p>
 *
 * @author nacos
 */
public class PromptListForm implements NacosForm {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 单页最大条数上限。 */
    private static final int MAX_PAGE_SIZE = 50;
    
    /** 命名空间 ID，为空时使用 Prompt 默认命名空间。 */
    private String namespaceId;
    
    /**
     * Optional promptKey filter.
     * <p>可选的 promptKey 过滤条件，用于精确或模糊匹配。</p>
     */
    private String promptKey;
    
    /**
     * Search mode: "accurate" or "blur".
     * <p>搜索模式：{@code accurate} 精确匹配或 {@code blur} 模糊匹配。</p>
     */
    private String search;
    
    /**
     * Optional biz tags filter (comma-separated).
     * <p>可选业务标签过滤，逗号分隔多个标签。</p>
     */
    private String bizTags;
    
    /**
     * Page number (1-based).
     * <p>页码，从 1 开始计数，小于 1 时自动修正为 1。</p>
     */
    private int pageNo = 1;
    
    /**
     * Page size.
     * <p>每页条数，默认 10，最大不超过 {@link #MAX_PAGE_SIZE}。</p>
     */
    private int pageSize = 10;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultNamespaceId();
        
        if (StringUtils.isNotBlank(search)
            && !Constants.Prompt.SEARCH_ACCURATE.equalsIgnoreCase(search)
            && !Constants.Prompt.SEARCH_BLUR.equalsIgnoreCase(search)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Request parameter 'search' should be 'accurate' or 'blur'.");
        }
        
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }
    
    /** 命名空间为空时填充 Prompt 默认命名空间。 */
    private void fillDefaultNamespaceId() {
        if (StringUtils.isEmpty(namespaceId)) {
            namespaceId = Constants.Prompt.PROMPT_DEFAULT_NAMESPACE;
        }
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getPromptKey() {
        return promptKey;
    }
    
    public void setPromptKey(String promptKey) {
        this.promptKey = promptKey;
    }
    
    public String getSearch() {
        return search;
    }
    
    public void setSearch(String search) {
        this.search = search;
    }
    
    public String getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(String bizTags) {
        this.bizTags = bizTags;
    }
    
    public int getPageNo() {
        return pageNo;
    }
    
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
