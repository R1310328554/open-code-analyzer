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

package com.alibaba.nacos.config.server.enums;

/**
 * 配置变更操作类型枚举，持久化与审计日志中标识增删改。
 * Operation type enum.
 *
 * @author dirtybit
 */
public enum OperationType {
    
    /**
     * 新增配置（I）。
     * Insert.
     */
    INSERT("I"),
    
    /**
     * 更新配置（U）。
     * Update.
     */
    UPDATE("U"),
    
    /**
     * 删除配置（D）。
     * Delete.
     */
    DELETE("D");
    
    /** 操作类型单字符编码（I/U/D） */
    /** operation type value. */
    
    private String value;
    
    OperationType(String value) {
        this.value = value;
    }
    
    /** 设置操作类型编码 */
    public void setValue(String value) {
        this.value = value;
    }
    
    /** 获取操作类型编码 */
    public String getValue() {
        return this.value;
    }
    
}
