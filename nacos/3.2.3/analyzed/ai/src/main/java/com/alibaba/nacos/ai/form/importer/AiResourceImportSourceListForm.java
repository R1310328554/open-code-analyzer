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

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;

import java.io.Serializable;

/**
 * Form for listing AI resource import sources.
 * <p>列出可用 AI 资源导入来源的表单，可按 resourceType 过滤返回匹配的 SPI 导入源列表。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportSourceListForm implements NacosForm, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 资源类型过滤条件，为空时返回全部类型的导入源。 */
    private String resourceType;
    
    @Override
    /** 来源列表查询无必填参数，校验为空实现。 */
    public void validate() throws NacosApiException {
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}
