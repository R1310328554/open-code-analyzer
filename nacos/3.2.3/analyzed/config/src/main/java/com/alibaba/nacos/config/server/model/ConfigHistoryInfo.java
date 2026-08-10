/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 配置变更历史记录：保存 dataId/group/tenant、操作类型、发布类型、灰度名称、
 * 内容快照及操作者 IP/用户等，供历史查询与审计回溯。
 * ConfigHistoryInfo.
 *
 * @author Nacos
 */
public class ConfigHistoryInfo implements Serializable {
    
    private static final long serialVersionUID = -7827521105376245603L;
    
    @JsonSerialize(using = ToStringSerializer.class)
    /** 历史记录主键 ID */
    private long id;
    
    /** 上一条历史记录 ID，-1 表示无前置版本 */
    private long lastId = -1;
    
    /** 配置 dataId */
    private String dataId;
    
    /** 配置 group */
    private String group;
    
    /** 命名空间 tenant（namespace） */
    private String tenant;
    
    private String appName;
    
    private String md5;
    
    private String content;
    
    private String srcIp;
    
    private String srcUser;
    
    /**
     * 操作类型：插入、更新或删除。
     * Operation type, include inserting, updating and deleting.
     */
    private String opType;
    
    /** 发布类型（正式/灰度/Beta 等） */
    private String publishType;
    
    /** 关联的灰度配置名称，非灰度发布时为 null */
    private String grayName;
    
    private String extInfo;
    
    private Timestamp createdTime;
    
    private Timestamp lastModifiedTime;
    
    /** 加密配置的数据密钥标识 */
    private String encryptedDataKey;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getLastId() {
        return lastId;
    }
    
    public void setLastId(long lastId) {
        this.lastId = lastId;
    }
    
    public String getDataId() {
        return dataId;
    }
    
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSrcIp() {
        return srcIp;
    }
    
    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }
    
    public String getSrcUser() {
        return srcUser;
    }
    
    public void setSrcUser(String srcUser) {
        this.srcUser = srcUser;
    }
    
    public String getOpType() {
        return opType;
    }
    
    public void setOpType(String opType) {
        this.opType = opType;
    }
    
    public String getPublishType() {
        return publishType;
    }
    
    public void setPublishType(String publishType) {
        this.publishType = publishType;
    }
    
    public String getExtInfo() {
        return extInfo;
    }
    
    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }
    
    public Timestamp getCreatedTime() {
        return new Timestamp(createdTime.getTime());
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = new Timestamp(createdTime.getTime());
    }
    
    public Timestamp getLastModifiedTime() {
        return new Timestamp(lastModifiedTime.getTime());
    }
    
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = new Timestamp(lastModifiedTime.getTime());
    }
    
    public String getGrayName() {
        return grayName;
    }
    
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }
    
    public void setEncryptedDataKey(String encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigHistoryInfo that = (ConfigHistoryInfo) o;
        return id == that.id && lastId == that.lastId && Objects.equals(dataId, that.dataId)
            && Objects.equals(group,
                that.group)
            && Objects.equals(tenant, that.tenant) && Objects.equals(appName, that.appName)
            && Objects.equals(md5, that.md5) && Objects.equals(content, that.content)
            && Objects.equals(srcIp,
                that.srcIp)
            && Objects.equals(srcUser, that.srcUser) && Objects.equals(opType, that.opType)
            && Objects.equals(createdTime, that.createdTime) && Objects.equals(lastModifiedTime,
                that.lastModifiedTime)
            && Objects.equals(encryptedDataKey, that.encryptedDataKey);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, lastId, dataId, group, tenant, appName, md5, content, srcIp,
            srcUser, opType,
            createdTime, lastModifiedTime, encryptedDataKey);
    }
}
