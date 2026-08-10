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

package com.alibaba.nacos.config.server.model.capacity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 配置容量基类：记录配额（quota）、已用量（usage）及单条/聚合大小上限，
 * 供 {@link GroupCapacity} 与 {@link NamespaceCapacity} 继承扩展。
 * Capacity.
 *
 * @author hexu.hxy
 * @date 2018/3/13
 */
public class Capacity implements Serializable {
    
    private static final long serialVersionUID = 77343194329627468L;
    
    @JsonSerialize(using = ToStringSerializer.class)
    /** 容量记录主键 ID */
    private Long id;
    
    /** 允许的配置条目数量上限（配额） */
    private Integer quota;
    
    /** 当前已使用的配置条目数 */
    private Integer usage;
    
    /** 单条配置内容最大字节数 */
    private Integer maxSize;
    
    /** 单条配置允许的最大聚合子项数量 */
    private Integer maxAggrCount;
    
    /** 单条配置聚合内容总大小上限（字节） */
    private Integer maxAggrSize;
    
    /** 记录创建时间 */
    private Timestamp gmtCreate;
    
    /** 记录最近修改时间 */
    private Timestamp gmtModified;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getQuota() {
        return quota;
    }
    
    public void setQuota(Integer quota) {
        this.quota = quota;
    }
    
    public Integer getUsage() {
        return usage;
    }
    
    public void setUsage(Integer usage) {
        this.usage = usage;
    }
    
    public Integer getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }
    
    public Integer getMaxAggrCount() {
        return maxAggrCount;
    }
    
    public void setMaxAggrCount(Integer maxAggrCount) {
        this.maxAggrCount = maxAggrCount;
    }
    
    public Integer getMaxAggrSize() {
        return maxAggrSize;
    }
    
    public void setMaxAggrSize(Integer maxAggrSize) {
        this.maxAggrSize = maxAggrSize;
    }
    
    public Timestamp getGmtCreate() {
        if (gmtCreate == null) {
            return null;
        }
        return new Timestamp(gmtCreate.getTime());
    }
    
    public void setGmtCreate(Timestamp gmtCreate) {
        if (gmtCreate == null) {
            this.gmtCreate = null;
        } else {
            this.gmtCreate = new Timestamp(gmtCreate.getTime());
        }
        
    }
    
    public Timestamp getGmtModified() {
        if (gmtModified == null) {
            return null;
        }
        return new Timestamp(gmtModified.getTime());
    }
    
    public void setGmtModified(Timestamp gmtModified) {
        if (gmtModified == null) {
            this.gmtModified = null;
        } else {
            this.gmtModified = new Timestamp(gmtModified.getTime());
        }
    }
}
