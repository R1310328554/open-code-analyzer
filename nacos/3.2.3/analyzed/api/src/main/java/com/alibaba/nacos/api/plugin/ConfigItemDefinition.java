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

import java.io.Serializable;
import java.util.List;

/**
 * 插件配置项定义。
 *
 * <p>描述单个可配置属性的键、显示名、类型、默认值及是否必填等信息，供控制台渲染与校验。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class ConfigItemDefinition implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 配置键（持久化标识）。 */
    private String key;
    
    /** 界面展示名称。 */
    private String name;
    
    /** 配置项说明。 */
    private String description;
    
    /** 默认值。 */
    private String defaultValue;
    
    /** 配置项类型。 */
    private ConfigItemType type;
    
    /** 是否必填。 */
    private boolean required;
    
    /** 枚举可选值（type 为 ENUM 时使用）。 */
    private List<String> enumValues;
    
    /** 无参构造，供序列化框架使用。 */
    public ConfigItemDefinition() {
    }
    
    /**
     * 构造基础配置项定义。
     *
     * @param key  配置键
     * @param name 显示名
     * @param type 配置类型
     */
    public ConfigItemDefinition(String key, String name, ConfigItemType type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }
    
    /** 返回配置键。 */
    public String getKey() {
        return key;
    }
    
    /** 设置配置键。 */
    public void setKey(String key) {
        this.key = key;
    }
    
    /** 返回显示名。 */
    public String getName() {
        return name;
    }
    
    /** 设置显示名。 */
    public void setName(String name) {
        this.name = name;
    }
    
    /** 返回配置说明。 */
    public String getDescription() {
        return description;
    }
    
    /** 设置配置说明。 */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /** 返回默认值。 */
    public String getDefaultValue() {
        return defaultValue;
    }
    
    /** 设置默认值。 */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /** 返回配置项类型。 */
    public ConfigItemType getType() {
        return type;
    }
    
    /** 设置配置项类型。 */
    public void setType(ConfigItemType type) {
        this.type = type;
    }
    
    /** 是否必填。 */
    public boolean isRequired() {
        return required;
    }
    
    /** 设置是否必填。 */
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    /** 返回枚举可选值列表。 */
    public List<String> getEnumValues() {
        return enumValues;
    }
    
    /** 设置枚举可选值列表。 */
    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }
    
    /**
     * {@link ConfigItemDefinition} 构建器。
     */
    public static class Builder {
        
        /** 正在组装的配置项定义。 */
        private final ConfigItemDefinition definition;
        
        /**
         * 创建构建器并初始化必填字段。
         *
         * @param key  配置键
         * @param name 显示名
         * @param type 配置类型
         */
        public Builder(String key, String name, ConfigItemType type) {
            this.definition = new ConfigItemDefinition(key, name, type);
        }
        
        /** 设置配置说明。 */
        public Builder description(String description) {
            definition.setDescription(description);
            return this;
        }
        
        /** 设置默认值。 */
        public Builder defaultValue(String defaultValue) {
            definition.setDefaultValue(defaultValue);
            return this;
        }
        
        /** 设置是否必填。 */
        public Builder required(boolean required) {
            definition.setRequired(required);
            return this;
        }
        
        /** 设置枚举可选值。 */
        public Builder enumValues(List<String> enumValues) {
            definition.setEnumValues(enumValues);
            return this;
        }
        
        /** 构建不可变配置项定义。 */
        public ConfigItemDefinition build() {
            return definition;
        }
    }
}
