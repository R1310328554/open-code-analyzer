/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.prompt;

import java.io.Serializable;

/**
 * Prompt 模板变量定义，可携带可选默认值与描述信息。
 *
 * <p>表示 Prompt 模板中的占位符（如 {{variableName}}），
 * 并记录该变量的默认值与用途说明。</p>
 *
 * @author nacos
 */
public class PromptVariable implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 变量名，与模板占位符一致（如 {{question}} 对应 "question"）。 */
    private String name;
    
    /** 变量默认值；为 null 表示无默认值（视为必填）。 */
    private String defaultValue;
    
    /** 可选描述，说明变量用途或期望填入的内容。 */
    private String description;
    
    public PromptVariable() {
    }
    
    public PromptVariable(String name, String defaultValue, String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "PromptVariable{" + "name='" + name + '\'' + ", defaultValue='" + defaultValue + '\''
            + ", description='"
            + description + '\'' + '}';
    }
}
