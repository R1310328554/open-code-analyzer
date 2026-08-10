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

package com.alibaba.nacos.api.ai.model.prompt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prompt 元信息摘要，用于 Prompt 列表查询响应。
 *
 * <p>汇总单个 Prompt 的关键属性：最新版本、编辑/审核中版本、
 * 在线版本数、标签映射及下载统计等。</p>
 *
 * @author nacos
 */
public class PromptMetaSummary implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 元数据 schema 版本号。 */
    private int schemaVersion = 1;
    
    /** Prompt 唯一键。 */
    private String promptKey;
    
    /** Prompt 描述文本。 */
    private String description;
    
    /** 业务标签列表。 */
    private List<String> bizTags = new ArrayList<>();
    
    /** 业务标签逗号分隔字符串（展示用）。 */
    private String bizTagsStr;
    
    /** 当前最新发布版本号。 */
    private String latestVersion;
    
    /** 最后修改时间戳（毫秒）。 */
    private Long gmtModified;
    
    /** 当前正在编辑的版本（草稿）。 */
    private String editingVersion;
    
    /** 当前处于流水线审核中的版本。 */
    private String reviewingVersion;
    
    /** 已上线版本数量。 */
    private Integer onlineCnt;
    
    /** 标签到版本号的映射，如 {@code {"latest":"1.0.0","stable":"0.9.0"}}。 */
    private Map<String, String> labels;
    
    /** 该 Prompt 累计下载次数（所有版本之和）。 */
    private Long downloadCount;
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
    
    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
    
    public String getPromptKey() {
        return promptKey;
    }
    
    public void setPromptKey(String promptKey) {
        this.promptKey = promptKey;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(List<String> bizTags) {
        this.bizTags = bizTags;
    }
    
    public String getBizTagsStr() {
        return bizTagsStr;
    }
    
    public void setBizTagsStr(String bizTagsStr) {
        this.bizTagsStr = bizTagsStr;
    }
    
    public String getLatestVersion() {
        return latestVersion;
    }
    
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
    
    public Long getGmtModified() {
        return gmtModified;
    }
    
    public void setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
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
    
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    public Long getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }
}
