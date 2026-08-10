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

package com.alibaba.nacos.ai.model;

import java.sql.Timestamp;

/**
 * Entity of ai_resource_version.
 * <p>AI 资源版本实体，对应表 {@code ai_resource_version}，记录某一资源的具体版本内容、存储位置与发布流水线信息。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public class AiResourceVersion {
    
    /** 主键 ID。 */
    private Long id;
    
    /** 创建时间。 */
    private Timestamp gmtCreate;
    
    /** 最后修改时间。 */
    private Timestamp gmtModified;
    
    /** 资源类型。 */
    private String type;
    
    /** 版本作者。 */
    private String author;
    
    /** 资源名称。 */
    private String name;
    
    /** 版本描述。 */
    private String desc;
    
    /** 版本状态（草稿、审核中、已发布等）。 */
    private String status;
    
    /** 语义化版本号。 */
    private String version;
    
    /** 所属命名空间 ID。 */
    private String namespaceId;
    
    /** 存储位置描述（配置 dataId/group 或对象存储路径）。 */
    private String storage;
    
    /** 发布流水线执行信息（JSON）。 */
    private String publishPipelineInfo;
    
    /** 该版本累计下载次数。 */
    private Long downloadCount;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Timestamp getGmtCreate() {
        return gmtCreate;
    }
    
    public void setGmtCreate(Timestamp gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
    
    public Timestamp getGmtModified() {
        return gmtModified;
    }
    
    public void setGmtModified(Timestamp gmtModified) {
        this.gmtModified = gmtModified;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getStorage() {
        return storage;
    }
    
    public void setStorage(String storage) {
        this.storage = storage;
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
