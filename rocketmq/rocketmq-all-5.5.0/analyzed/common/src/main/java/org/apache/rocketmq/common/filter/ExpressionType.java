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

package org.apache.rocketmq.common.filter;

/**
 * 消息过滤表达式类型常量：SQL92 与 TAG 两种订阅过滤语法。
 */
public class ExpressionType {

    /**
     * SQL92 风格过滤表达式。
     * <ul>
     * 关键字：
     * <li>{@code AND, OR, NOT, BETWEEN, IN, TRUE, FALSE, IS, NULL}</li>
     * </ul>
     * <p/>
     * <ul>
     * 数据类型：
     * <li>布尔：TRUE、FALSE</li>
     * <li>字符串：如 {@code 'abc'}</li>
     * <li>整数：如 123</li>
     * <li>浮点：如 3.1415</li>
     * </ul>
     * <p/>
     * <ul>
     * 语法：
     * <li>{@code AND, OR}</li>
     * <li>{@code >, >=, <, <=, =}</li>
     * <li>{@code BETWEEN A AND B} 等价于 {@code >=A AND <=B}</li>
     * <li>{@code NOT BETWEEN A AND B} 等价于 {@code >B OR <A}</li>
     * <li>{@code IN ('a', 'b')} 等价于 {@code ='a' OR ='b'}，仅支持字符串</li>
     * <li>{@code IS NULL}、{@code IS NOT NULL} 判空</li>
     * <li>{@code =TRUE}、{@code =FALSE} 判布尔</li>
     * </ul>
     * <p/>
     * <p>
     * 示例：{@code (a > 10 AND a < 100) OR (b IS NOT NULL AND b=TRUE)}
     * </p>
     */
    public static final String SQL92 = "SQL92";

    /**
     * TAG 过滤：仅支持 {@code ||} 或运算，如 {@code tag1 || tag2 || tag3}；
     * null 或 {@code *} 表示订阅全部。
     */
    public static final String TAG = "TAG";

    /**
     * 判断表达式类型是否为 TAG（null、空串或 {@link #TAG} 均视为 TAG）。
     *
     * @param type 表达式类型字符串
     * @return 是否为 TAG 类型
     */
    public static boolean isTagType(String type) {
        if (type == null || "".equals(type) || TAG.equals(type)) {
            return true;
        }
        return false;
    }
}
