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
package org.apache.rocketmq.auth.authorization.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

/**
 * ACL 策略类型：区分用户自定义策略与系统默认策略。
 */
public enum PolicyType {

    /** 用户自定义策略。 */
    CUSTOM((byte) 1, "Custom"),

    /** 系统默认策略。 */
    DEFAULT((byte) 2, "Default");

    @JSONField(value = true)
    private final byte code;
    private final String name;

    PolicyType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    /** 按名称（忽略大小写）查找策略类型；未匹配时返回 null。 */
    public static PolicyType getByName(String name) {
        for (PolicyType policyType : PolicyType.values()) {
            if (StringUtils.equalsIgnoreCase(policyType.getName(), name)) {
                return policyType;
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
