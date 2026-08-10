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
 * 外部 AI 资源搜索返回的候选条目。
 *
 * <p>描述导入源中可被选中并导入 Nacos 的外部资源，含外部 ID、名称、版本、
 * 描述及扩展元数据，供控制台展示与勾选。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportCandidateItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String externalId;
    
    private String name;
    
    private String version;
    
    private String description;
    
    private Map<String, String> metadata;
    
    /** 返回外部资源唯一标识。 */
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    /** 返回资源显示名称。 */
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /** 返回资源版本号。 */
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 返回资源描述文本。 */
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /** 返回扩展元数据键值对。 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
