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

package com.alibaba.nacos.plugin.ai.importer.model;

import java.util.Map;

/**
 * 用户从候选列表中勾选、待导入插件拉取的外部资源条目。
 *
 * <p>由控制台或 API 在用户确认导入时构造，作为
 * {@link com.alibaba.nacos.plugin.ai.importer.spi.AiResourceImportService#fetch} 的定位参数。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportItem {
    
    /** 外部系统唯一标识。 */
    private String externalId;
    
    /** 资源名称。 */
    private String name;
    
    /** 资源版本。 */
    private String version;
    
    /** 用户选择时附带的元数据。 */
    private Map<String, String> metadata;
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
