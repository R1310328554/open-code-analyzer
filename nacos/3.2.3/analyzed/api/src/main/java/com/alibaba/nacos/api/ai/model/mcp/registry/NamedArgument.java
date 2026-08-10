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
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * MCP Registry 命名参数模型，对应 components.schemas.NamedArgument。
 *
 * <p>继承 {@link InputWithVariables} 并实现 {@link Argument}，
 * 通过 {@link com.fasterxml.jackson.annotation.JsonTypeName @JsonTypeName("named")} 标识多态类型。</p>
 *
 * @author xinluo
 */
@JsonTypeName("named")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NamedArgument extends InputWithVariables implements Argument {
    
    /** 参数类型，固定为 {@code named}。 */
    private String type = "named";
    
    /** 命令行参数名（如 {@code --port}）。 */
    private String name;
    
    /** 是否可重复出现（如多个 {@code --include}）。 */
    private Boolean isRepeated;
    
    /** 取值提示或占位说明。 */
    private String valueHint;
    
    /**
     * 获取参数类型标识。
     *
     * @return 类型字符串，通常为 {@code named}
     */
    public String getType() {
        return type;
    }
    
    /**
     * 设置参数类型标识。
     *
     * @param type 类型字符串
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * 获取命名参数的名称。
     *
     * @return 参数名
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置命名参数的名称。
     *
     * @param name 参数名
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取是否可重复标志。
     *
     * @return 为 {@code true} 表示该参数可出现多次
     */
    public Boolean getIsRepeated() {
        return isRepeated;
    }
    
    /**
     * 设置是否可重复标志。
     *
     * @param isRepeated 是否可重复
     */
    public void setIsRepeated(Boolean isRepeated) {
        this.isRepeated = isRepeated;
    }
    
    /**
     * 获取取值提示。
     *
     * @return 提示文本
     */
    public String getValueHint() {
        return valueHint;
    }
    
    /**
     * 设置取值提示。
     *
     * @param valueHint 提示文本
     */
    public void setValueHint(String valueHint) {
        this.valueHint = valueHint;
    }
}
