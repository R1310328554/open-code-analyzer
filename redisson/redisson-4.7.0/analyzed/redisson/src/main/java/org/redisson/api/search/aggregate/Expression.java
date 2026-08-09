/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.search.aggregate;

/**
 * 聚合查询中对文档属性施加的计算表达式。
 * <p>
 * 通过 {@link AggregationBaseOptions#apply(Expression...)} 注册，表达式结果可指定别名供后续
 * 分组、排序或加载使用。
 *
 * @author Nikita Koksharov
 *
 */
public final class Expression {

    private final String value;
    private final String as;

    /**
     * 构造聚合表达式。
     *
     * @param expression 表达式字符串
     * @param as 结果别名
     */
    public Expression(String expression, String as) {
        this.value = expression;
        this.as = as;
    }

    /** 返回表达式正文。 */
    public String getValue() {
        return value;
    }

    /** 返回结果别名。 */
    public String getAs() {
        return as;
    }
}
