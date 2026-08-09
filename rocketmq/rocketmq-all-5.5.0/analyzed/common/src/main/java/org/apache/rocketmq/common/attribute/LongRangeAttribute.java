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
package org.apache.rocketmq.common.attribute;

import static java.lang.String.format;

/**
 * 长整型范围 Topic 属性：取值须在 {@code [min, max]} 闭区间内。
 */
public class LongRangeAttribute extends Attribute {
    /** 允许的最小值（含）。 */
    private final long min;
    /** 允许的最大值（含）。 */
    private final long max;
    /** 属性默认值。 */
    private final long defaultValue;

    /**
     * @param name 属性名
     * @param changeable 创建后是否可修改
     * @param min 最小值
     * @param max 最大值
     * @param defaultValue 默认值
     */
    public LongRangeAttribute(String name, boolean changeable, long min, long max, long defaultValue) {
        super(name, changeable);
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
    }

    /** 解析为 long 并校验是否在 [min, max] 范围内。 */
    @Override
    public void verify(String value) {
        long l = Long.parseLong(value);
        if (l < min || l > max) {
            throw new RuntimeException(format("value is not in range(%d, %d)", min, max));
        }
    }

    /** 返回默认值。 */
    public long getDefaultValue() {
        return defaultValue;
    }
}
