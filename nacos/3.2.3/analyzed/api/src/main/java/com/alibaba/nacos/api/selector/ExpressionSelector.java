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

package com.alibaba.nacos.api.selector;

/**
 * 基于标签表达式的服务实例选择器。
 *
 * <p>类型为 {@link SelectorType#label}，通过 {@link #expression} 描述筛选条件，由服务端解析并应用于实例列表。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
public class ExpressionSelector extends AbstractSelector {
    
    /** 标签筛选表达式。 */
    private String expression;
    
    /** 构造 label 类型选择器。 */
    public ExpressionSelector() {
        super(SelectorType.label.name());
    }
    
    /** 返回标签表达式。 */
    public String getExpression() {
        return expression;
    }
    
    /** 设置标签表达式。 */
    public void setExpression(String expression) {
        this.expression = expression;
    }
}
