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
package org.redisson.api.search;

/**
 * 应用于文档属性上的 RediSearch 表达式。
 * <p>
 * 用于聚合查询的 APPLY 阶段，对字段执行计算并可通过别名引用结果。
 *
 * @author Nikita Koksharov
 *
 */
public final class Expression {

    private final String value;
    private final String as;

    /**
     * 构造搜索表达式。
     *
     * @param expression 表达式内容
     * @param as 结果别名
     */
    public Expression(String expression, String as) {
        this.value = expression;
        this.as = as;
    }

    /**
     * 返回表达式内容。
     *
     * @return 表达式字符串
     */
    public String getValue() {
        return value;
    }

    /**
     * 返回结果别名。
     *
     * @return 别名字符串
     */
    public String getAs() {
        return as;
    }
}
