/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.namespace.model;

/**
 * 命名空间类型枚举：区分全局配置、自定义业务命名空间及 AI MCP 专用空间。
 * <p>修改枚举值时需同步更新前端（如 Namespace.js）。</p>
 * Namespace type enum.
 * Note: Changes to this enum may require updates to the frontend code (e.g., Namespace.js).
 *
 * @author chenglu
 * @date 2021-05-25 17:01
 */
public enum NamespaceTypeEnum {
    
    /**
     * 全局配置命名空间（type=0）。
     */
    GLOBAL(0, "Global configuration"),
    
    /**
     * 用户自定义命名空间，用于配置中心与服务发现（type=1）。
     */
    CUSTOM(1, "Custom namespace for naming and config"),
    
    /**
     * Nacos AI 模块 MCP 默认私有命名空间（type=2）。
     */
    AI_MCP(2, "Default private namespace");
    
    /**
     * 类型数值编码。
     * the namespace type.
     */
    private final int type;
    
    /**
     * 类型英文描述。
     * the description.
     */
    private final String description;
    
    NamespaceTypeEnum(int type, String description) {
        this.type = type;
        this.description = description;
    }
    
    public int getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
}
