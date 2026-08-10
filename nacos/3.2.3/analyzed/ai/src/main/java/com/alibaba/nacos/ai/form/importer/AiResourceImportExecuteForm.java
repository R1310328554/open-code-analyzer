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

import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteRequest;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Form for executing AI resource import.
 * <p>执行 AI 资源导入的表单，在验证通过后批量写入所选资源；支持覆盖已有资源、跳过无效项及携带 validationToken 防重放。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportExecuteForm extends AbstractAiResourceImportForm {
    
    private static final long serialVersionUID = 1L;
    
    /** 待导入项 JSON 列表，每项为 {@link AiResourceImportItem}。 */
    private String selectedItems;
    
    /** 目标命名空间已存在同名资源时是否覆盖。 */
    private boolean overwriteExisting;
    
    /** 遇到无效项时是否跳过继续导入其余项。 */
    private boolean skipInvalid;
    
    /** 预校验接口返回的令牌，用于关联校验与执行步骤。 */
    private String validationToken;
    
    @Override
    public void validate() throws NacosApiException {
        validateSource();
        if (StringUtils.isBlank(selectedItems)) {
            throw missingParameter("selectedItems");
        }
    }
    
    /**
     * Convert form data to import execute request.
     * <p>将表单字段转换为 {@link AiResourceImportExecuteRequest} 供服务层执行导入。</p>
     *
     * @return import execute request
     * @throws NacosApiException if form JSON fields can't be parsed
     */
    public AiResourceImportExecuteRequest toRequest() throws NacosApiException {
        AiResourceImportExecuteRequest request = new AiResourceImportExecuteRequest();
        request.setNamespaceId(getNamespaceId());
        request.setResourceType(getResourceType());
        request.setSourceId(getSourceId());
        request.setSelectedItems(parseSelectedItems(selectedItems));
        request.setOverwriteExisting(overwriteExisting);
        request.setSkipInvalid(skipInvalid);
        request.setValidationToken(validationToken);
        request.setOptions(parseOptions());
        return request;
    }
    
    public String getSelectedItems() {
        return selectedItems;
    }
    
    public void setSelectedItems(String selectedItems) {
        this.selectedItems = selectedItems;
    }
    
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }
    
    public boolean isSkipInvalid() {
        return skipInvalid;
    }
    
    public void setSkipInvalid(boolean skipInvalid) {
        this.skipInvalid = skipInvalid;
    }
    
    public String getValidationToken() {
        return validationToken;
    }
    
    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }
}
