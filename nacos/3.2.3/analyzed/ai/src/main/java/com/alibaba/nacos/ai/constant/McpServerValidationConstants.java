/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.constant;

/**
 * MCP 服务端校验相关常量。
 *
 * <p>定义校验结果状态：有效、无效、重复。</p>
 *
 * @author nacos
 */
public final class McpServerValidationConstants {
    
    /**
     * 校验状态：有效。
     */
    public static final String STATUS_VALID = "valid";
    
    /**
     * 校验状态：无效。
     */
    public static final String STATUS_INVALID = "invalid";
    
    /**
     * 校验状态：重复。
     */
    public static final String STATUS_DUPLICATE = "duplicate";
    
    private McpServerValidationConstants() {
        // 私有构造器，禁止实例化
    }
}
