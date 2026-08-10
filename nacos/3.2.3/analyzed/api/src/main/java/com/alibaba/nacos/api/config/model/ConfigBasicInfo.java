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

package com.alibaba.nacos.api.config.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;

/**
 * Nacos 配置基本信息模型。
 *
 * <p>用于控制台列表、Open API 等场景展示配置的元数据摘要。</p>
 *
 * @author xiweng.yy
 */
public class ConfigBasicInfo implements Serializable {
    
    private static final long serialVersionUID = 2662049844183052399L;
    
    /**
     * 配置在存储层的实际主键 ID，与业务 dataId 无直接对应关系。
     *
     * <p>不同存储后端 ID 生成策略不同，例如关系型数据库为自增主键。</p>
     * <p>序列化为字符串是为避免前端 JavaScript 处理大整数时精度丢失
     * （如 {@code 862926428394491904} 会被舍入），导致后续操作无法定位配置。</p>
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /** 命名空间 ID。 */
    private String namespaceId;
    
    /** 配置分组名。 */
    private String groupName;
    
    /** 配置 dataId。 */
    private String dataId;
    
    /** 配置内容 MD5 摘要。 */
    private String md5;
    
    /** 配置格式类型（如 text、json、yaml）。 */
    private String type;
    
    /** 关联应用名。 */
    private String appName;
    
    /** 创建时间戳（毫秒）。 */
    private long createTime;
    
    /** 最后修改时间戳（毫秒）。 */
    private long modifyTime;
    
    /** 配置描述。 */
    private String desc;
    
    /** 配置标签，逗号分隔。 */
    private String configTags;
    
    /** 获取存储层主键 ID。 */
    public Long getId() {
        return id;
    }
    
    /** 设置存储层主键 ID。 */
    public void setId(Long id) {
        this.id = id;
    }
    
    /** 获取命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** 获取配置分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置配置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /** 获取配置 dataId。 */
    public String getDataId() {
        return dataId;
    }
    
    /** 设置配置 dataId。 */
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    /** 获取内容 MD5 摘要。 */
    public String getMd5() {
        return md5;
    }
    
    /** 设置内容 MD5 摘要。 */
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    /** 获取配置格式类型。 */
    public String getType() {
        return type;
    }
    
    /** 设置配置格式类型。 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 获取关联应用名。 */
    public String getAppName() {
        return appName;
    }
    
    /** 设置关联应用名。 */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /** 获取创建时间戳。 */
    public long getCreateTime() {
        return createTime;
    }
    
    /** 设置创建时间戳。 */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    /** 获取最后修改时间戳。 */
    public long getModifyTime() {
        return modifyTime;
    }
    
    /** 设置最后修改时间戳。 */
    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
    
    /** 获取配置描述。 */
    public String getDesc() {
        return desc;
    }
    
    /** 设置配置描述。 */
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    /** 获取配置标签。 */
    public String getConfigTags() {
        return configTags;
    }
    
    /** 设置配置标签。 */
    public void setConfigTags(String configTags) {
        this.configTags = configTags;
    }
}
