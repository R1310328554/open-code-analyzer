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
 * MCP Registry 位置参数模型，对应 components.schemas.PositionalArgument。
 *
 * <p>按命令行中的出现顺序解析，无需显式名称；
 * 继承 {@link InputWithVariables} 并实现 {@link Argument}。</p>
 *
 * @author xinluo
 */
@JsonTypeName("positional")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionalArgument extends InputWithVariables implements Argument {
    
    /** 参数类型，固定为 {@code positional}。 */
    private String type = "positional";
    
    /** 取值提示或占位说明。 */
    private String valueHint;
    
    /** 是否可重复出现。 */
    private Boolean isRepeated;
    
    /**
     * 获取参数类型标识。
     *
     * @return 类型字符串，通常为 {@code positional}
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
    
    /**
     * 获取是否可重复标志。
     *
     * @return 可重复标志
     */
    public Boolean getIsRepeated() {
        return isRepeated;
    }
    
    /**
     * 设置是否可重复标志。
     *
     * @param isRepeated 可重复标志
     */
    public void setIsRepeated(Boolean isRepeated) {
        this.isRepeated = isRepeated;
    }
}
