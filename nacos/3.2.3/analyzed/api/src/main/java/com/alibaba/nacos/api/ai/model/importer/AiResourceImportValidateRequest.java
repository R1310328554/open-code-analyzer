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
 * 校验选中外部 AI 资源能否导入的请求体。
 *
 * <p>在执行真正导入前预检命名冲突、格式合法性与覆盖策略，
 * 结果用于生成 {@link AiResourceImportValidateResponse#validationToken}。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportValidateRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String namespaceId;
    
    private String resourceType;
    
    private String sourceId;
    
    private List<AiResourceImportItem> selectedItems;
    
    private boolean overwriteExisting;
    
    private Map<String, String> options;
    
    /** 返回目标命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** 返回 AI 资源类型。 */
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
    
    /** 返回待校验的外部资源条目。 */
    public List<AiResourceImportItem> getSelectedItems() {
        return selectedItems;
    }
    
    public void setSelectedItems(List<AiResourceImportItem> selectedItems) {
        this.selectedItems = selectedItems;
    }
    
    /** 冲突时是否允许覆盖已有资源。 */
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }
    
    /** 返回校验扩展选项。 */
    public Map<String, String> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
