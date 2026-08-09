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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 布尔型 Topic 属性：取值须为 {@code true} 或 {@code false}（忽略大小写）。
 */
public class BooleanAttribute extends Attribute {
    /** 属性默认值。 */
    private final boolean defaultValue;

    /**
     * @param name 属性名
     * @param changeable 创建后是否可修改
     * @param defaultValue 默认值
     */
    public BooleanAttribute(String name, boolean changeable, boolean defaultValue) {
        super(name, changeable);
        this.defaultValue = defaultValue;
    }

    /** 校验取值是否为 true/false。 */
    @Override
    public void verify(String value) {
        checkNotNull(value);

        if (!"false".equalsIgnoreCase(value) && !"true".equalsIgnoreCase(value)) {
            throw new RuntimeException("boolean attribute format is wrong.");
        }
    }

    /** 返回默认值。 */
    public boolean getDefaultValue() {
        return defaultValue;
    }
}
