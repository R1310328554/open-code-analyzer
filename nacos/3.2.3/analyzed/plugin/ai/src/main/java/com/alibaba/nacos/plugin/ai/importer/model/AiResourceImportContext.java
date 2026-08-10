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

package com.alibaba.nacos.plugin.ai.importer.model;

import java.util.Map;

/**
 * Nacos 导入管理器传递给导入插件的请求上下文。
 *
 * <p>上下文携带已解析的 {@link AiResourceImportSource}，而非用户自行填写的端点信息，
 * 导入插件必须使用 Nacos 选定的可信来源进行搜索与拉取。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportContext {
    
    /** 目标命名空间 ID。 */
    private String namespaceId;
    
    /** 待导入的资源类型。 */
    private String resourceType;
    
    /** 由服务端解析并注入的导入来源配置。 */
    private AiResourceImportSource source;
    
    /** 搜索关键字或过滤条件。 */
    private String query;
    
    /** 分页游标，首页可为空。 */
    private String cursor;
    
    /** 单页返回条数上限。 */
    private int limit;
    
    /** 插件扩展选项键值对。 */
    private Map<String, String> options;
    
    /** 请求追踪 ID，便于日志关联。 */
    private String requestId;
    
    /** 发起导入操作的用户标识。 */
    private String operator;
    
    /** 客户端 IP 地址。 */
    private String clientIp;
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public AiResourceImportSource getSource() {
        return source;
    }
    
    public void setSource(AiResourceImportSource source) {
        this.source = source;
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
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public Map<String, String> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
