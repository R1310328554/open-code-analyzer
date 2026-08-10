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

package com.alibaba.nacos.copilot.model;

import java.io.Serializable;

/**
 * 优化变更信息：记录 Prompt 或 Skill 优化过程中某一字段的改动详情。
 * Optimization change information.
 *
 * @author nacos
 */
public class OptimizationChange implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 变更字段名，如 instruction、description。
     */
    private String field;
    
    /**
     * 变更类型，如 improved、added、removed。
     */
    private String type;
    
    /**
     * 变更描述。
     */
    private String description;
    
    /**
     * 变更原因说明。
     */
    private String reason;
    
    public OptimizationChange() {
    }
    
    public OptimizationChange(String field, String type, String description, String reason) {
        this.field = field;
        this.type = type;
        this.description = description;
        this.reason = reason;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
