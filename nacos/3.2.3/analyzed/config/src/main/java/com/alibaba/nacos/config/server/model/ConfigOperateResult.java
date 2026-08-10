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

import java.io.Serializable;

/**
 * 配置写操作结果：标识成功与否，并返回持久化 ID 与最新修改时间。
 * 发布、更新、删除等持久化层统一返回此类型。
 * config operation result.
 * @author shiyiyue
 */
public class ConfigOperateResult implements Serializable {
    
    /** 操作是否成功，默认 true */
    boolean success = true;
    
    /** 配置持久化主键 ID */
    private long id;
    
    /** 操作后的最后修改时间戳 */
    private long lastModified;
    
    /**
     * 成功构造：返回 ID 与修改时间。
     *
     * @param id            配置 ID
     * @param lastModified  修改时间戳
     */
    public ConfigOperateResult(long id, long lastModified) {
        this.id = id;
        this.lastModified = lastModified;
    }
    
    /** 仅指定成功/失败的构造 */
    public ConfigOperateResult(boolean success) {
        this.success = success;
    }
    
    /** 无参构造，success 默认为 true */
    public ConfigOperateResult() {
        
    }
    
    /** 是否操作成功 */
    public boolean isSuccess() {
        return success;
    }
    
    /** 设置操作结果标志 */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /** 获取配置 ID */
    public long getId() {
        return id;
    }
    
    /** 设置配置 ID */
    public void setId(long id) {
        this.id = id;
    }
    
    /** 获取最后修改时间 */
    public long getLastModified() {
        return lastModified;
    }
    
    /** 设置最后修改时间 */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
}
