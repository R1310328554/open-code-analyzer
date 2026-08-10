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

package com.alibaba.nacos.ai.enums;

/**
 * MCP 批量导入结果状态枚举。
 *
 * <p>标识单条 MCP Server 导入记录的最终状态：跳过、失败或成功。</p>
 * @author xinluo
 */
public enum McpImportResultStatusEnum {
    
    /**
     * 已跳过（如重复或不符合导入条件）。
     */
    SKIPPED("skipped"),
    
    /**
     * 导入失败。
     */
    FAILED("failed"),
    
    /**
     * 导入成功。
     */
    SUCCESS("success");
    
    /** 状态字符串值 */
    private final String name;
    
    McpImportResultStatusEnum(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
