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

/**
 * AI 资源导入校验响应。
 *
 * <p>为每条选中资源返回 {@link AiResourceImportValidationItem}，并颁发
 * {@link #validationToken} 供后续 {@link AiResourceImportExecuteRequest} 绑定同一会话。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportValidateResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String sourceId;
    
    private String resourceType;
    
    private String validationToken;
    
    private List<AiResourceImportValidationItem> items;
    
    /** 返回导入源 ID。 */
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    /** 返回资源类型。 */
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    /** 返回校验令牌，执行导入时需回传。 */
    public String getValidationToken() {
        return validationToken;
    }
    
    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }
    
    /** 返回逐条校验结果。 */
    public List<AiResourceImportValidationItem> getItems() {
        return items;
    }
    
    public void setItems(List<AiResourceImportValidationItem> items) {
        this.items = items;
    }
}
