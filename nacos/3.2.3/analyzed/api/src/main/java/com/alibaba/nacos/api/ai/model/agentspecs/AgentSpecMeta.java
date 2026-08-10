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

import java.util.List;

/**
 * 管理端 API 返回的 AgentSpec 元数据，含治理信息与全部版本摘要。
 *
 * <p>继承 {@link AgentSpecSummary}，附加 {@link #versions} 列表，
 * 便于控制台展示版本状态、作者与下载量等统计。</p>
 *
 * @author nacos
 */
public class AgentSpecMeta extends AgentSpecSummary {
    
    /** 该 AgentSpec 下所有版本的摘要列表。 */
    private List<AgentSpecVersionSummary> versions;
    
    public List<AgentSpecVersionSummary> getVersions() {
        return versions;
    }
    
    public void setVersions(List<AgentSpecVersionSummary> versions) {
        this.versions = versions;
    }
    
    /** 单个 AgentSpec 版本的摘要，供管理端展示。 */
    
    public static class AgentSpecVersionSummary {
        
        /** 版本号。 */
        private String version;
        
        /** 版本状态（如草稿、已发布等）。 */
        private String status;
        
        /** 版本作者。 */
        private String author;
        
        /** 版本描述。 */
        private String description;
        
        /** 版本创建时间戳。 */
        private Long createTime;
        
        /** 版本最近更新时间戳。 */
        private Long updateTime;
        
        /** 发布流水线信息（JSON 或文本）。 */
        private String publishPipelineInfo;
        
        /** 版本累计下载次数。 */
        private Long downloadCount;
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getAuthor() {
            return author;
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Long getCreateTime() {
            return createTime;
        }
        
        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }
        
        public Long getUpdateTime() {
            return updateTime;
        }
        
        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }
        
        public String getPublishPipelineInfo() {
            return publishPipelineInfo;
        }
        
        public void setPublishPipelineInfo(String publishPipelineInfo) {
            this.publishPipelineInfo = publishPipelineInfo;
        }
        
        public Long getDownloadCount() {
            return downloadCount;
        }
        
        public void setDownloadCount(Long downloadCount) {
            this.downloadCount = downloadCount;
        }
    }
}
