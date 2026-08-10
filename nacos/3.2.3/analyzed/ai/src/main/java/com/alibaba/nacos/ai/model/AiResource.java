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

import com.alibaba.nacos.plugin.visibility.model.VisibilityResource;

import java.sql.Timestamp;

/**
 * Entity of ai_resource.
 * <p>AI 资源实体，对应数据库表 {@code ai_resource}，继承 {@link VisibilityResource} 以支持可见性控制；涵盖 Prompt、Skill、MCP 等 AI 资源的元数据与版本摘要。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public class AiResource extends VisibilityResource {
    
    /** 主键 ID。 */
    private Long id;
    
    /** 创建时间。 */
    private Timestamp gmtCreate;
    
    /** 最后修改时间。 */
    private Timestamp gmtModified;
    
    /** 资源名称（全局唯一标识的一部分）。 */
    private String name;
    
    /** 资源类型，如 prompt、skill、mcp 等。 */
    private String type;
    
    /** 资源描述。 */
    private String desc;
    
    /** 资源状态（草稿、已发布、下线等）。 */
    private String status;
    
    /** 所属命名空间 ID。 */
    private String namespaceId;
    
    /** 业务标签，逗号分隔。 */
    private String bizTags;
    
    /** 扩展 JSON 字段。 */
    private String ext;
    
    /** 资源来源标识。 */
    private String from;
    
    /** 版本摘要信息（JSON 或结构化文本）。 */
    private String versionInfo;
    
    /** 元数据乐观锁版本号。 */
    private Long metaVersion;
    
    /** 累计下载次数。 */
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
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
    
    @Override
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    @Override
    public String getResourceName() {
        return name;
    }
    
    @Override
    public String getResourceType() {
        return type;
    }
    
    public String getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(String bizTags) {
        this.bizTags = bizTags;
    }
    
    public String getExt() {
        return ext;
    }
    
    public void setExt(String ext) {
        this.ext = ext;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getVersionInfo() {
        return versionInfo;
    }
    
    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }
    
    public Long getMetaVersion() {
        return metaVersion;
    }
    
    public void setMetaVersion(Long metaVersion) {
        this.metaVersion = metaVersion;
    }
    
    public Long getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }
}
