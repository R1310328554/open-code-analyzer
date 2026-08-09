/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.filter.expression;

/**
 * 属性表达式：从 {@link EvaluationContext} 按名称读取属性值。
 * <p>源自 ActiveMQ 的 PropertyExpression，实现更精简，不在表达式与消息属性间做转换。</p>
 */
public class PropertyExpression implements Expression {
    /** 属性名称。 */
    private final String name;

    /** @param name 要读取的属性名 */
    public PropertyExpression(String name) {
        this.name = name;
    }

    /** 从上下文按 {@link #name} 取值。 */
    /** 从上下文按 {@link #name} 取值。 */
    @Override
    public Object evaluate(EvaluationContext context) throws Exception {
        return context.get(name);
    }

    /** @return 属性名称 */
    /** @return 属性名 */
    public String getName() {
        return name;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {

        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return name.equals(((PropertyExpression) o).name);
    }
}
