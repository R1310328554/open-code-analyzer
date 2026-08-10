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
 * 单条外部 AI 资源的导入校验结果。
 *
 * <p>标明 {@link AiResourceImportValidationStatus}、是否已存在、冲突类型及
 * 警告/错误明细，帮助用户在执行前修正选择。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportValidationItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String externalId;
    
    private String name;
    
    private String version;
    
    private AiResourceImportValidationStatus status;
    
    private boolean exists;
    
    private String conflictType;
    
    private List<String> warnings;
    
    private List<String> errors;
    
    /** 返回外部资源 ID。 */
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    /** 返回资源名称。 */
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /** 返回资源版本。 */
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 返回校验结论状态。 */
    public AiResourceImportValidationStatus getStatus() {
        return status;
    }
    
    public void setStatus(AiResourceImportValidationStatus status) {
        this.status = status;
    }
    
    /** Nacos 中是否已存在同名/同键资源。 */
    public boolean isExists() {
        return exists;
    }
    
    public void setExists(boolean exists) {
        this.exists = exists;
    }
    
    /** 冲突类型描述（如有）。 */
    public String getConflictType() {
        return conflictType;
    }
    
    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }
    
    /** 返回警告信息列表。 */
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    /** 返回阻止导入的错误列表。 */
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
