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
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * MCP Registry 官方元数据，嵌于 {@code _meta} 扩展命名空间内。
 *
 * <p>对应 {@code io.modelcontextprotocol.registry/official} 键下的结构化信息，
 * 描述 Server 在 Registry 中的发布、更新与状态。</p>
 *
 * @author xinluo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficialMeta {
    
    /** 首次发布时间戳（ISO-8601 字符串）。 */
    private String publishedAt;
    
    /** 最近更新时间戳。 */
    private String updatedAt;
    
    /** 是否为当前最新版本。 */
    private Boolean isLatest;
    
    /** 发布状态（如 active、deprecated）。 */
    private String status;
    
    /**
     * 获取发布时间戳。
     *
     * @return 发布时间
     */
    public String getPublishedAt() {
        return publishedAt;
    }
    
    /**
     * 设置发布时间戳。
     *
     * @param publishedAt 发布时间
     */
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    /**
     * 获取更新时间戳。
     *
     * @return 更新时间
     */
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * 设置更新时间戳。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 获取是否为最新版本。
     *
     * @return 最新版本标志
     */
    public Boolean getIsLatest() {
        return isLatest;
    }
    
    /**
     * 设置是否为最新版本。
     *
     * @param isLatest 最新版本标志
     */
    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest;
    }
    
    /**
     * 获取发布状态。
     *
     * @return 状态字符串
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * 设置发布状态。
     *
     * @param status 状态字符串
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
