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

package org.apache.rocketmq.filter.constant;

/**
 * 一元运算类型枚举，用于 SQL 选择器解析与求值。
 */
public enum UnaryType {
    /** 取负（-x）。 */
    NEGATE,
    /** IN 集合成员判断。 */
    IN,
    /** 逻辑非（NOT）。 */
    NOT,
    /** 布尔类型强制转换。 */
    BOOLEANCAST,
    /** 字符串 LIKE 模式匹配。 */
    LIKE
}
