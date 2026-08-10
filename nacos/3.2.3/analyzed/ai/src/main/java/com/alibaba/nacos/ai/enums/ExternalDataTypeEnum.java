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

import com.alibaba.nacos.api.utils.StringUtils;

/**
 * 外部 MCP 数据类型枚举。
 *
 * <p>用户可将外部 MCP Server 数据导入 Nacos，本枚举定义支持的导入来源类型。</p>
 * @author xinluo
 */
public enum ExternalDataTypeEnum {
    
    /**
     * MCP Server JSON 文本，须符合 MCP Server JSON Schema 格式。
     */
    JSON("json"),
    
    /**
     * MCP Registry URL，须为合法 Registry 地址且 API 符合 OpenAPI 规范。
     */
    URL("url"),
    
    /**
     * MCP Registry 种子文件（seed.json）。
     */
    FILE("file");
    
    /**
     * 外部数据类型的字符串标识。
     */
    private final String name;
    
    ExternalDataTypeEnum(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 从字符串解析外部数据类型。
     * @param value the value to parse.
     * @return the external data type.
     */
    public static ExternalDataTypeEnum parseType(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        
        for (ExternalDataTypeEnum type : ExternalDataTypeEnum.values()) {
            if (type.getName().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
