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
 * 单条 AI 资源导入执行结果。
 *
 * <p>关联外部 ID 与导入后的 Nacos 资源名，并携带 {@link AiResourceImportResultStatus}、
 * 错误信息与警告列表，供前端逐条展示。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportResultItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String externalId;
    
    private String resourceName;
    
    private String version;
    
    private AiResourceImportResultStatus status;
    
    private String errorMessage;
    
    private List<String> warnings;
    
    /** 返回外部资源 ID。 */
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    /** 返回导入后在 Nacos 中的资源名称。 */
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    /** 返回导入版本。 */
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 返回本条导入状态。 */
    public AiResourceImportResultStatus getStatus() {
        return status;
    }
    
    public void setStatus(AiResourceImportResultStatus status) {
        this.status = status;
    }
    
    /** 失败时的错误信息。 */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /** 返回非致命警告列表。 */
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
