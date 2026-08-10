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

import com.alibaba.nacos.api.ai.model.importer.AiResourceImportItem;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Base form for AI resource import APIs.
 * <p>AI 资源导入 API 的表单基类，封装命名空间、资源类型、来源 ID 与 options 等公共字段，并提供 JSON 解析与参数缺失异常构造等共享校验逻辑。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public abstract class AbstractAiResourceImportForm implements NacosForm, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 目标命名空间 ID。 */
    private String namespaceId;
    
    /** 资源类型（如 Skill、AgentSpec 等）。 */
    private String resourceType;
    
    /** 外部导入来源标识。 */
    private String sourceId;
    
    /** 扩展选项 JSON 字符串，键值对形式。 */
    private String options;
    
    /** 校验 resourceType 与 sourceId 必填。 */
    protected void validateSource() throws NacosApiException {
        if (StringUtils.isBlank(resourceType)) {
            throw missingParameter("resourceType");
        }
        if (StringUtils.isBlank(sourceId)) {
            throw missingParameter("sourceId");
        }
    }
    
    /** 将 options JSON 字符串解析为键值 Map，空值返回 null。 */
    protected Map<String, String> parseOptions() throws NacosApiException {
        if (StringUtils.isBlank(options)) {
            return null;
        }
        try {
            return JacksonUtils.toObj(options, new TypeReference<Map<String, String>>() {
            });
        } catch (RuntimeException e) {
            throw parseFailed("options", e);
        }
    }
    
    /** 将 selectedItems JSON 解析为 {@link AiResourceImportItem} 列表。 */
    protected List<AiResourceImportItem> parseSelectedItems(String selectedItems)
        throws NacosApiException {
        if (StringUtils.isBlank(selectedItems)) {
            return null;
        }
        try {
            return JacksonUtils.toObj(selectedItems,
                new TypeReference<List<AiResourceImportItem>>() {
                });
        } catch (RuntimeException e) {
            throw parseFailed("selectedItems", e);
        }
    }
    
    /** 构造参数缺失的 {@link NacosApiException}。 */
    protected NacosApiException missingParameter(String name) {
        return new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
            "Required parameter `" + name + "` is not present.");
    }
    
    private NacosApiException parseFailed(String name, RuntimeException cause) {
        return new NacosApiException(NacosException.INVALID_PARAM,
            ErrorCode.PARAMETER_VALIDATE_ERROR, cause,
            "Request parameter `" + name + "` can't be parsed.");
    }
    
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
    
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public String getOptions() {
        return options;
    }
    
    public void setOptions(String options) {
        this.options = options;
    }
}
