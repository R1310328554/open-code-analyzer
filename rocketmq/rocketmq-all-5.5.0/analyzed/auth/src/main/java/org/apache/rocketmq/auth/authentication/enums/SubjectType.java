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
package org.apache.rocketmq.auth.authentication.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

/** 授权主体类型枚举，用于解析 subjectKey 前缀。 */
public enum SubjectType {

    /** 普通用户主体。 */
    USER((byte) 1, "User");

    @JSONField(value = true)
    private final byte code;
    private final String name;

    SubjectType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    /** 按名称（忽略大小写）查找主体类型。 */
    public static SubjectType getByName(String name) {
        for (SubjectType subjectType : SubjectType.values()) {
            if (StringUtils.equalsIgnoreCase(subjectType.getName(), name)) {
                return subjectType;
            }
        }
        return null;
    }

    /** 返回持久化用的数值编码。 */
    public byte getCode() {
        return code;
    }

    /** 返回可读名称。 */
    public String getName() {
        return name;
    }
}
