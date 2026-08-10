/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.mcp.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * MCP Server 源码或制品仓库信息，对应 components.schemas.Repository。
 *
 * <p>用于 Registry 元数据中声明 Server 的来源仓库地址与标识。</p>
 *
 * @author xinluo
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    
    /** 仓库访问 URL。 */
    private String url;
    
    /** 仓库来源类型（如 github、gitlab）。 */
    private String source;
    
    /** 仓库唯一 ID。 */
    private String id;
    
    /** 仓库内子目录路径（Monorepo 场景）。 */
    private String subfolder;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSubfolder() {
        return subfolder;
    }
    
    public void setSubfolder(String subfolder) {
        this.subfolder = subfolder;
    }
}
