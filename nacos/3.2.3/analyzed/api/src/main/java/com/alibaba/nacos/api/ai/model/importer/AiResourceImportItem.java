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
import java.util.Map;

/**
 * 校验或执行阶段选中的外部 AI 资源条目。
 *
 * <p>比 {@link AiResourceImportCandidateItem} 精简，仅保留导入所需的外部 ID、
 * 名称、版本与元数据，用于批量校验与执行请求体。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String externalId;
    
    private String name;
    
    private String version;
    
    private Map<String, String> metadata;
    
    /** 返回外部资源唯一标识。 */
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
    
    /** 返回扩展元数据。 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
