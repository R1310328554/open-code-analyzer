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
package org.apache.rocketmq.common.resource;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * ACL 资源匹配模式：任意、字面量或前缀匹配。
 */
public enum ResourcePattern {

    /** 匹配任意资源。 */
    ANY((byte) 1, "ANY"),

    /** 字面量精确匹配。 */
    LITERAL((byte) 2, "LITERAL"),

    /** 前缀匹配。 */
    PREFIXED((byte) 3, "PREFIXED");

    /** 序列化/反序列化使用的模式编码。 */
    @JSONField(value = true)
    private final byte code;
    /** 模式名称字符串。 */
    private final String name;

    ResourcePattern(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
