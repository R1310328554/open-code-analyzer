/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.mcp;

import java.io.Serializable;

/**
 * MCP Server 批量导入请求体，支持文件、URL 与 JSON 多种导入方式。
 *
 * <p>可配置覆盖策略、仅校验预览、跳过无效项及 URL 分页参数，
 * 用于从外部 Registry 或本地文件批量注册 MCP Server。</p>
 *
 * @author nacos
 */
public class McpServerImportRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 导入类型：file、url 或 json。 */
    private String importType;
    
    /** 导入源数据（文件内容、URL 或 JSON 字符串）。 */
    private String data;
    
    /** 是否覆盖已存在的同名 Server。 */
    private boolean overrideExisting = false;
    
    /** 是否仅校验不实际导入（预览模式）。 */
    private boolean validateOnly = false;
    
    /**
     * 是否跳过无效 Server 并继续导入有效项。
     * 默认 {@code false}，存在任意无效项时快速失败。
     */
    private boolean skipInvalid = false;
    
    /** 选择性导入时指定的 Server ID 列表。 */
    private String[] selectedServers;
    
    /** URL 导入分页起始游标，仅 importType=url 时生效。 */
    private String cursor;
    
    /** URL 导入每页条数上限，仅 importType=url 时生效；为 null 时使用服务端默认值。 */
    private Integer limit;
    
    /**
     * Registry 列表模糊搜索关键字，仅 importType=url 时生效；
     * 后端将其作为 {@code search} 参数追加到 Registry 查询串。
     */
    private String search;
    
    public String getImportType() {
        return importType;
    }
    
    public void setImportType(String importType) {
        this.importType = importType;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public boolean isOverrideExisting() {
        return overrideExisting;
    }
    
    public void setOverrideExisting(boolean overrideExisting) {
        this.overrideExisting = overrideExisting;
    }
    
    public boolean isValidateOnly() {
        return validateOnly;
    }
    
    public void setValidateOnly(boolean validateOnly) {
        this.validateOnly = validateOnly;
    }
    
    public String[] getSelectedServers() {
        return selectedServers;
    }
    
    public void setSelectedServers(String[] selectedServers) {
        this.selectedServers = selectedServers;
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
    
    public String getSearch() {
        return search;
    }
    
    public void setSearch(String search) {
        this.search = search;
    }
    
    public boolean isSkipInvalid() {
        return skipInvalid;
    }
    
    public void setSkipInvalid(boolean skipInvalid) {
        this.skipInvalid = skipInvalid;
    }
}
