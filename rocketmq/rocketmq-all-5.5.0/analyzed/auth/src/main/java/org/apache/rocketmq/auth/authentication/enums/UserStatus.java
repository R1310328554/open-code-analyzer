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

/** 用户账号启用/禁用状态。 */
public enum UserStatus {

    /** 账号已启用。 */
    ENABLE((byte) 1, "enable"),

    /** 账号已禁用，认证将被拒绝。 */
    DISABLE((byte) 2, "disable");

    @JSONField(value = true)
    private final byte code;

    private final String name;

    UserStatus(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    /** 按名称（忽略大小写）查找用户状态。 */
    public static UserStatus getByName(String name) {
        for (UserStatus subjectType : UserStatus.values()) {
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
