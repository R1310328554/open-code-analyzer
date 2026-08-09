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
import org.apache.commons.lang3.StringUtils;

/**
 * ACL 资源类型：集群、命名空间、Topic、消费组等。
 */
public enum ResourceType {

    /** 未知类型。 */
    UNKNOWN((byte) 0, "Unknown"),

    /** 任意资源类型。 */
    ANY((byte) 1, "Any"),

    /** 集群级资源。 */
    CLUSTER((byte) 2, "Cluster"),

    /** 命名空间级资源。 */
    NAMESPACE((byte) 3, "Namespace"),

    /** Topic 资源。 */
    TOPIC((byte) 4, "Topic"),

    /** 消费组资源。 */
    GROUP((byte) 5, "Group");

    /** JSON 序列化主键字段。 */
    @JSONField(value = true)
    private final byte code;
    /** 资源类型显示名。 */
    private final String name;

    ResourceType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    /** 按名称（忽略大小写）查找资源类型，未匹配返回 null。 */
    public static ResourceType getByName(String name) {
        for (ResourceType resourceType : ResourceType.values()) {
            if (StringUtils.equalsIgnoreCase(resourceType.getName(), name)) {
                return resourceType;
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
