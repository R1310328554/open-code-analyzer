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

package com.alibaba.nacos.api.ai.model.skills;

import java.util.Map;

/**
 * Skill 管理端列表摘要，包含基础信息与治理元数据。
 *
 * @author nacos
 */
public class SkillSummary extends SkillBasicInfo {
    
    /** Skill 资源所有者。 */
    private String owner;
    
    /** 是否全局启用；true 表示启用，false 表示禁用。 */
    private boolean enable;
    
    /** 业务标签（JSON 字符串），如 ["tag1","tag2"]。 */
    private String bizTags;
    
    /** 来源标记，用于 IP 归属追踪（如 local/import/sync）。 */
    private String from;
    
    /** Skill 元数据可见范围，如 PUBLIC 或 PRIVATE。 */
    private String scope;
    
    /** 标签到版本的映射，如 {"latest":"v3","stable":"v2"}。 */
    private Map<String, String> labels;
    
    /** 当前正在编辑的草稿版本。 */
    private String editingVersion;
    
    /** 当前处于发布流水线审核中的版本。 */
    private String reviewingVersion;
    
    /** 已上线版本数量。 */
    private Integer onlineCnt;
    
    /** 全部版本的累计下载次数。 */
    private Long downloadCount;
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public boolean isEnable() {
        return enable;
    }
    
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    
    public String getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(String bizTags) {
        this.bizTags = bizTags;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    public String getEditingVersion() {
        return editingVersion;
    }
    
    public void setEditingVersion(String editingVersion) {
        this.editingVersion = editingVersion;
    }
    
    public String getReviewingVersion() {
        return reviewingVersion;
    }
    
    public void setReviewingVersion(String reviewingVersion) {
        this.reviewingVersion = reviewingVersion;
    }
    
    public Integer getOnlineCnt() {
        return onlineCnt;
    }
    
    public void setOnlineCnt(Integer onlineCnt) {
        this.onlineCnt = onlineCnt;
    }
    
    public Long getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }
}
