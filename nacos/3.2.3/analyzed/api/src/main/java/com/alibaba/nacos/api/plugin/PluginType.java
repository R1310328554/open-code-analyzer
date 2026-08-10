/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.plugin;

/**
 * 插件类型枚举，涵盖 Nacos 全部插件类别。
 *
 * <p>每种类型对应唯一 type 字符串与描述，供 {@link PluginProvider} 与 SPI 发现使用。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public enum PluginType {
    
    /** 认证插件。 */
    AUTH("auth", "Authentication plugin"),
    
    /** 数据源方言插件。 */
    DATASOURCE_DIALECT("datasource-dialect", "Datasource dialect plugin"),
    
    /** 配置变更插件。 */
    CONFIG_CHANGE("config-change", "Config change plugin"),
    
    /** 加密插件。 */
    ENCRYPTION("encryption", "Encryption plugin"),
    
    /** 链路追踪插件。 */
    TRACE("trace", "Trace plugin"),
    
    /** 环境变量插件。 */
    ENVIRONMENT("environment", "Environment plugin"),
    
    /** 流量控制插件。 */
    CONTROL("control", "Control plugin"),
    
    /** 可见性插件。 */
    VISIBILITY("visibility", "Visibility plugin"),
    
    /** AI 发布流水线插件。 */
    AI_PIPELINE("ai-pipeline", "AI publish pipeline plugin"),
    
    /** AI 资源存储插件。 */
    AI_STORAGE("ai-storage", "AI resource storage plugin"),
    
    /** AI 资源导入插件。 */
    AI_RESOURCE_IMPORT("ai-resource-import", "AI resource import plugin");
    
    /** 类型标识字符串。 */
    private final String type;
    
    /** 类型描述。 */
    private final String description;
    
    PluginType(String type, String description) {
        this.type = type;
        this.description = description;
    }
    
    /** 返回类型标识字符串。 */
    public String getType() {
        return type;
    }
    
    /** 返回类型描述。 */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据 type 字符串解析 PluginType。
     *
     * @param type 类型字符串
     * @return 对应的 PluginType
     * @throws IllegalArgumentException 未知类型时抛出
     */
    public static PluginType fromType(String type) {
        for (PluginType pt : values()) {
            if (pt.type.equals(type)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("Unknown plugin type: " + type);
    }
}
