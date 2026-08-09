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

package org.apache.rocketmq.common.filter.impl;

/**
 * TAG 过滤表达式运算符：括号、逻辑与/或，含优先级与可比较性。
 */
public class Operator extends Op {

    /** 左括号。 */
    public static final Operator LEFTPARENTHESIS = new Operator("(", 30, false);
    /** 右括号。 */
    public static final Operator RIGHTPARENTHESIS = new Operator(")", 30, false);
    /** 逻辑与 {@code &&}。 */
    public static final Operator AND = new Operator("&&", 20, true);
    /** 逻辑或 {@code ||}。 */
    public static final Operator OR = new Operator("||", 15, true);

    /** 运算符优先级（数值越大优先级越高）。 */
    private int priority;
    /** 是否参与优先级比较（括号不参与）。 */
    private boolean compareable;

    /** @param symbol 符号 @param priority 优先级 @param compareable 是否可比较 */
    private Operator(String symbol, int priority, boolean compareable) {
        super(symbol);
        this.priority = priority;
        this.compareable = compareable;
    }

    /**
     * 按符号字符串创建预定义运算符实例。
     *
     * @param operator 运算符符号
     * @return 对应 {@link Operator}
     * @throws IllegalArgumentException 不支持的运算符
     */
    public static Operator createOperator(String operator) {
        if (LEFTPARENTHESIS.getSymbol().equals(operator))
            return LEFTPARENTHESIS;
        else if (RIGHTPARENTHESIS.getSymbol().equals(operator))
            return RIGHTPARENTHESIS;
        else if (AND.getSymbol().equals(operator))
            return AND;
        else if (OR.getSymbol().equals(operator))
            return OR;
        else
            throw new IllegalArgumentException("unsupport operator " + operator);
    }

    /** 返回运算符优先级。 */
    public int getPriority() {
        return priority;
    }

    /** 返回是否参与优先级比较。 */
    public boolean isCompareable() {
        return compareable;
    }

    /**
     * 与另一运算符比较优先级。
     *
     * @param operator 待比较运算符
     * @return 正数表示本运算符优先级更高
     */
    public int compare(Operator operator) {
        if (this.priority > operator.priority)
            return 1;
        else if (this.priority == operator.priority)
            return 0;
        else
            return -1;
    }

    /** 判断本运算符符号是否与给定字符串相同。 */
    public boolean isSpecifiedOp(String operator) {
        return this.getSymbol().equals(operator);
    }
}
