/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 * MCP Server 版本详情，描述某一发布版本的元信息。
 *
 * <p>字段名与 Registry OpenAPI schema 保持一致（snake_case），
 * 用于版本列表或历史查询响应。</p>
 *
 * @author xinluo
 */
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:ParameterName", "checkstyle:MemberName",
    "checkstyle:SummaryJavadoc"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerVersionDetail {
    
    /** 版本号字符串。 */
    private String version;
    
    /** 发布日期。 */
    private String release_date;
    
    /** 是否为最新版本。 */
    private Boolean is_latest;
    
    public String getRelease_date() {
        return release_date;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setRelease_date(String releaseDate) {
        this.release_date = releaseDate;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void setIs_latest(Boolean is_latest) {
        this.is_latest = is_latest;
    }
    
    public Boolean getIs_latest() {
        return is_latest;
    }
}
