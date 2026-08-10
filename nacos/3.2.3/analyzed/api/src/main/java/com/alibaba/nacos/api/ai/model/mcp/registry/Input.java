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

package com.alibaba.nacos.api.ai.model.mcp.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * MCP Registry 通用输入项模型，描述配置表单项的类型、默认值与约束。
 *
 * <p>与 components.schemas.Input 对齐，用于 Registry 包安装/运行参数表单。</p>
 *
 * @author xinluo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Input {
    
    /** 输入项说明文字。 */
    private String description;
    
    /** 是否必填。 */
    private Boolean isRequired;
    
    /** 输入格式约束（如 url、password 等）。 */
    private String format;
    
    /** 当前值或固定值。 */
    private String value;
    
    /** 是否为敏感/密钥类输入。 */
    private Boolean isSecret;
    
    /** 默认值。 */
    private String defaultValue;
    
    /** 可选枚举值列表。 */
    private List<String> choices;
    
    /** 表单占位提示文本。 */
    private String placeholder;
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsRequired() {
        return isRequired;
    }
    
    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public Boolean getIsSecret() {
        return isSecret;
    }
    
    public void setIsSecret(Boolean isSecret) {
        this.isSecret = isSecret;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public List<String> getChoices() {
        return choices;
    }
    
    public void setChoices(List<String> choices) {
        this.choices = choices;
    }
    
    /**
     * Get placeholder.
     *
     * @return placeholder
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public String getPlaceholder() {
        return placeholder;
    }
    
    /**
     * Set placeholder.
     *
     * @param placeholder placeholder
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
