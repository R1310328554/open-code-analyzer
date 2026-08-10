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

package com.alibaba.nacos.api.ai.model.importer;

import java.io.Serializable;
import java.util.Map;

/**
 * 从已配置导入源搜索外部 AI 资源候选的请求体。
 *
 * <p>支持关键字 {@link #query}、游标 {@link #cursor} 分页与 {@link #limit} 限流，
 * 用于控制台“从外部导入”向导的第一步资源发现。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportSearchRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String namespaceId;
    
    private String resourceType;
    
    private String sourceId;
    
    private String query;
    
    private String cursor;
    
    private Integer limit;
    
    private Map<String, String> options;
    
    /** 返回目标命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** 返回要搜索的 AI 资源类型。 */
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    /** 返回导入源 ID。 */
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    /** 返回搜索关键字。 */
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    /** 返回分页游标（首页可为 null）。 */
    public String getCursor() {
        return cursor;
    }
    
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
    
    /** 返回单页最大返回条数。 */
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    /** 返回搜索扩展选项。 */
    public Map<String, String> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
