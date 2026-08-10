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

import java.util.Map;

/**
 * 带变量占位符的输入项，继承 {@link Input} 并附加 variables 映射。
 *
 * <p>与 components.schemas.InputWithVariables 对齐，
 * 用于模板化命令或 URL 中的可替换变量定义。</p>
 *
 * @author xinluo
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputWithVariables extends Input {
    
    /** 变量名到子输入项定义的映射。 */
    private Map<String, Input> variables;
    
    public Map<String, Input> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, Input> variables) {
        this.variables = variables;
    }
}
