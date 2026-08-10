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

package com.alibaba.nacos.api.ai.model.agentspecs;

import java.util.Map;

/**
 * 管理端 AgentSpec 列表项摘要，继承 {@link AgentSpecBasicInfo} 并扩展治理元数据。
 *
 * <p>除名称、描述等基础字段外，还携带启用状态、业务标签、来源、作用域、标签、
 * 编辑/审核中版本号、在线版本数与下载次数等运营指标，供控制台分页展示与筛选。</p>
 *
 * @author nacos
 */
public class AgentSpecSummary extends AgentSpecBasicInfo {
    
    private boolean enable;
    
    private String bizTags;
    
    private String from;
    
    private String scope;
    
    private Map<String, String> labels;
    
    private String editingVersion;
    
    private String reviewingVersion;
    
    private Integer onlineCnt;
    
    private Long downloadCount;
    
    /** 是否启用该 AgentSpec。 */
    public boolean isEnable() {
        return enable;
    }
    
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    
    /** 返回业务标签（逗号分隔或 JSON 字符串，依服务端约定）。 */
    public String getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(String bizTags) {
        this.bizTags = bizTags;
    }
    
    /** 返回 AgentSpec 来源标识（如上传、内置、导入等）。 */
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    /** 返回作用域（命名空间或租户级可见范围）。 */
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    /** 返回自定义标签键值对。 */
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    /** 返回当前处于编辑中的版本号。 */
    public String getEditingVersion() {
        return editingVersion;
    }
    
    public void setEditingVersion(String editingVersion) {
        this.editingVersion = editingVersion;
    }
    
    /** 返回当前处于审核中的版本号。 */
    public String getReviewingVersion() {
        return reviewingVersion;
    }
    
    public void setReviewingVersion(String reviewingVersion) {
        this.reviewingVersion = reviewingVersion;
    }
    
    /** 返回已上线版本数量。 */
    public Integer getOnlineCnt() {
        return onlineCnt;
    }
    
    public void setOnlineCnt(Integer onlineCnt) {
        this.onlineCnt = onlineCnt;
    }
    
    /** 返回累计下载次数。 */
    public Long getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }
}
