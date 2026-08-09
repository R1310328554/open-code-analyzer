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
 * 常量表达式：封装字面量值（数字、字符串、布尔、当前时间等）。
 * <p>
 * 相较 ActiveMQ 版本：Long/Double 范围受限，移除八/十六进制，新增 now 表达式。
 * </p>
 */
public class ConstantExpression implements Expression {

    private Object value;

    public ConstantExpression(Object value) {
        this.value = value;
    }

    /** 从十进制文本创建整数常量，自动收窄为 int 或 long。 */
    public static ConstantExpression createFromDecimal(String text) {

        // 去除 Long 后缀 l/L
        if (text.endsWith("l") || text.endsWith("L")) {
            text = text.substring(0, text.length() - 1);
        }

        // 仅支持 Java Long 范围内的整数
        Number value = new Long(text);

        long l = value.longValue();
        if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
            value = value.intValue();
        }
        return new ConstantExpression(value);
    }

    /** 从浮点文本创建 Double 常量，超出范围则抛异常。 */
    public static ConstantExpression createFloat(String text) {
        Double value = new Double(text);
        if (value > Double.MAX_VALUE) {
            throw new RuntimeException(text + " is greater than " + Double.MAX_VALUE);
        }
        if (value < Double.MIN_VALUE) {
            throw new RuntimeException(text + " is less than " + Double.MIN_VALUE);
        }
        return new ConstantExpression(value);
    }

    /** 创建表示当前时间的 now 表达式。 */
    public static ConstantExpression createNow() {
        return new NowExpression();
    }

    /** 直接返回内部常量值，与上下文无关。 */
    public Object evaluate(EvaluationContext context) throws Exception {
        return value;
    }

    public Object getValue() {
        return value;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        Object value = getValue();
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "TRUE" : "FALSE";
        }
        if (value instanceof String) {
            return encodeString((String) value);
        }
        return value.toString();
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {

        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return toString().equals(o.toString());

    }

    /**
     * 将字符串编码为选择器字面量形式（单引号转义）。
     */
    public static String encodeString(String s) {

        StringBuilder builder = new StringBuilder();

        builder.append('\'');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                builder.append(c);
            }
            builder.append(c);
        }
        builder.append('\'');
        return builder.toString();
    }

}
