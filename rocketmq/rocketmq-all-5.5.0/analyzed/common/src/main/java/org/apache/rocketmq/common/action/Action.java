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
package org.apache.rocketmq.common.action;

import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

/**
 * ACL/鉴权动作枚举：与 {@link RocketMQAction} 配合描述对资源的操作类型。
 * 支持按名称忽略大小写解析。
 */
public enum Action {

    /** 未知动作。 */
    UNKNOWN((byte) 0, "Unknown"),

    /** 全部动作（通配）。 */
    ALL((byte) 1, "All"),

    /** 任意动作。 */
    ANY((byte) 2, "Any"),

    /** 发布消息。 */
    PUB((byte) 3, "Pub"),

    /** 订阅/消费。 */
    SUB((byte) 4, "Sub"),

    /** 创建资源。 */
    CREATE((byte) 5, "Create"),

    /** 更新资源。 */
    UPDATE((byte) 6, "Update"),

    /** 删除资源。 */
    DELETE((byte) 7, "Delete"),

    /** 获取单个资源。 */
    GET((byte) 8, "Get"),

    /** 列出资源。 */
    LIST((byte) 9, "List");

    /** 动作编码，JSON 序列化主键。 */
    @JSONField(value = true)
    private final byte code;
    /** 动作英文名称。 */
    private final String name;

    Action(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    /** 按名称忽略大小写查找，未找到返回 null。 */
    public static Action getByName(String name) {
        for (Action action : Action.values()) {
            if (StringUtils.equalsIgnoreCase(action.getName(), name)) {
                return action;
            }
        }
        return null;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
