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
import java.util.List;
import java.util.Map;

/**
 * 执行 AI 资源导入的请求体。
 *
 * <p>在校验通过后携带 {@link #validationToken} 与待导入条目列表，
 * 并可通过 {@link #overwriteExisting}、{@link #skipInvalid} 控制冲突覆盖与无效项跳过策略。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportExecuteRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String namespaceId;
    
    private String resourceType;
    
    private String sourceId;
    
    private List<AiResourceImportItem> selectedItems;
    
    private boolean overwriteExisting;
    
    private boolean skipInvalid;
    
    private String validationToken;
    
    private Map<String, String> options;
    
    /** 返回目标命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** 返回 AI 资源类型（如 MCP、Prompt 等）。 */
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
    
    /** 返回待导入的外部资源条目列表。 */
    public List<AiResourceImportItem> getSelectedItems() {
        return selectedItems;
    }
    
    public void setSelectedItems(List<AiResourceImportItem> selectedItems) {
        this.selectedItems = selectedItems;
    }
    
    /** 是否与已有资源冲突时覆盖。 */
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }
    
    /** 是否跳过校验未通过的条目继续导入其余项。 */
    public boolean isSkipInvalid() {
        return skipInvalid;
    }
    
    public void setSkipInvalid(boolean skipInvalid) {
        this.skipInvalid = skipInvalid;
    }
    
    /** 返回校验阶段颁发的令牌，用于绑定本次导入会话。 */
    public String getValidationToken() {
        return validationToken;
    }
    
    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }
    
    /** 返回导入扩展选项。 */
    public Map<String, String> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
