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

package com.alibaba.nacos.ai.form.importer;

import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchRequest;
import com.alibaba.nacos.api.exception.api.NacosApiException;

/**
 * Form for searching external AI resource import candidates.
 * <p>搜索外部 AI 资源导入候选的表单，支持 query 关键字过滤、cursor 分页游标与 limit 条数限制。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportSearchForm extends AbstractAiResourceImportForm {
    
    private static final long serialVersionUID = 1L;
    
    /** 搜索关键字，用于过滤外部来源中的资源名称或描述。 */
    private String query;
    
    /** 分页游标，上一页响应返回的 nextCursor。 */
    private String cursor;
    
    /** 单页返回条数上限。 */
    private Integer limit;
    
    @Override
    public void validate() throws NacosApiException {
        validateSource();
    }
    
    /**
     * Convert form data to import search request.
     * <p>将表单字段转换为 {@link AiResourceImportSearchRequest} 供服务层检索候选资源。</p>
     *
     * @return import search request
     * @throws NacosApiException if options can't be parsed
     */
    public AiResourceImportSearchRequest toRequest() throws NacosApiException {
        AiResourceImportSearchRequest request = new AiResourceImportSearchRequest();
        request.setNamespaceId(getNamespaceId());
        request.setResourceType(getResourceType());
        request.setSourceId(getSourceId());
        request.setQuery(query);
        request.setCursor(cursor);
        request.setLimit(limit);
        request.setOptions(parseOptions());
        return request;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getCursor() {
        return cursor;
    }
    
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
