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
package org.apache.rocketmq.common;

/** 边界类型：用于表示期望的下界或上界。 */
public enum BoundaryType {
    /** 期望下界。 */
    LOWER("lower"),

    /** 期望上界。 */
    UPPER("upper");

    /** 边界类型名称字符串。 */
    private String name;

    BoundaryType(String name) {
        this.name = name;
    }

    /** 返回边界类型名称。 */
    public String getName() {
        return name;
    }

    /** 按名称解析边界类型，无法识别时默认 LOWER。 */
    public static BoundaryType getType(String name) {
        if (BoundaryType.UPPER.getName().equalsIgnoreCase(name)) {
            return UPPER;
        }
        return LOWER;
    }
}
