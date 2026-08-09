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
package org.redisson.api.atomic;

/**
 * 数值比较并删除操作使用的比较条件。
 * <p>
 * 各常量对应 Lua 侧的比较运算符。
 *
 * @author Nikita Koksharov
 *
 */
public enum ComparisonCondition {

    LESS("<"),
    LESS_OR_EQUAL("<="),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    EQUAL("=="),
    NOT_EQUAL("~=");

    private final String operator;

    ComparisonCondition(String luaOperator) {
        this.operator = luaOperator;
    }

    public String getOperator() {
        return operator;
    }

}
