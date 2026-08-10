/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.ai.model.mcp.registry;

/**
 * MCP Server 在 Registry 中的生命周期状态枚举。
 *
 * @author xinluo
 */
public enum McpServerStatusEnum {
    
    /** 活跃可用状态。 */
    ACTIVE("active"),
    
    /** 已删除状态。 */
    DELETED("deleted"),
    
    /** 已废弃状态。 */
    DEPRECATED("deprecated");
    
    /** 状态字符串值。 */
    private final String name;
    
    McpServerStatusEnum(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 将字符串状态解析为枚举值。
     *
     * @param status 状态字符串
     * @return 对应枚举，无效时返回 null
     */
    public static McpServerStatusEnum parseStatus(String status) {
        for (McpServerStatusEnum value : values()) {
            if (value.getName().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
