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

import java.util.Set;

/**
 * 枚举型 Topic 属性：取值须落在预定义的 {@link #universe} 集合内。
 */
public class EnumAttribute extends Attribute {
    /** 合法取值集合。 */
    private final Set<String> universe;
    /** 属性默认值。 */
    private final String defaultValue;

    /**
     * @param name 属性名
     * @param changeable 创建后是否可修改
     * @param universe 合法取值集合
     * @param defaultValue 默认值
     */
    public EnumAttribute(String name, boolean changeable, Set<String> universe, String defaultValue) {
        super(name, changeable);
        this.universe = universe;
        this.defaultValue = defaultValue;
    }

    /** 校验取值是否在 universe 集合内。 */
    @Override
    public void verify(String value) {
        if (!this.universe.contains(value)) {
            throw new RuntimeException("value is not in set: " + this.universe);
        }
    }

    /** 返回默认值。 */
    public String getDefaultValue() {
        return defaultValue;
    }
}
