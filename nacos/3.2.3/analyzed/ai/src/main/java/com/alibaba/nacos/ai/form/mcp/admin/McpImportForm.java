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

package com.alibaba.nacos.ai.form.mcp.admin;

import com.alibaba.nacos.ai.enums.ExternalDataTypeEnum;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.Serial;

/**
 * Nacos AI MCP Server import request form.
 * <p>MCP 批量导入请求表单，支持 json/url/file 三种 {@link com.alibaba.nacos.ai.enums.ExternalDataTypeEnum}；可仅校验（validateOnly）、跳过无效项（skipInvalid）或覆盖已有资源（overrideExisting）。</p>
 *
 * @author WangzJi
 */
public class McpImportForm extends McpForm {
    
    @Serial
    private static final long serialVersionUID = 8016131725604983671L;
    
    /** 导入类型：json、url 或 file，对应 ExternalDataTypeEnum。 */
    private String importType;
    
    /** 导入数据源：JSON 字符串、URL 或文件路径/内容标识。 */
    private String data;
    
    /** 是否覆盖已存在的 MCP 资源。 */
    private boolean overrideExisting = false;
    
    /** 为 true 时仅执行校验，不写入持久化存储。 */
    private boolean validateOnly = false;
    
    /**
     * Whether to skip invalid servers when executing import.
     * <p>执行导入时是否跳过解析/校验失败的服务端条目，继续处理其余项。</p>
     */
    private boolean skipInvalid = false;
    
    /** 用户勾选的待导入服务端名称列表，为空时导入全部。 */
    private String[] selectedServers;
    
    /**
     * Optional start cursor for URL-based import pagination.
     * <p>基于 URL 导入时的分页游标，用于拉取下一页注册表数据。</p>
     */
    private String cursor;
    
    /**
     * Optional page size for URL-based import (items per page).
     * <p>URL 导入每页条数上限。</p>
     */
    private Integer limit;
    
    /**
     * Optional fuzzy search keyword for registry import listing.
     * Only used when importType is 'url'.
     * <p>注册表导入列表的模糊搜索关键词，仅 importType 为 url 时生效。</p>
     */
    private String search;
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultValue();
        if (StringUtils.isEmpty(importType)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'importType' is not present");
        }
        if (StringUtils.isEmpty(data)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'data' is not present");
        }
        if (ExternalDataTypeEnum.parseType(importType) == null) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "importType must be one of: json, url, file");
        }
    }
    
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
    
    public boolean isSkipInvalid() {
        return skipInvalid;
    }
    
    public void setSkipInvalid(boolean skipInvalid) {
        this.skipInvalid = skipInvalid;
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
}
