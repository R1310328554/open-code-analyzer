/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.impl.enums.mysql;

import java.util.HashMap;
import java.util.Map;

/**
 * MySQL 可信内置 SQL 函数枚举。
 *
 * <p>维护 Mapper 动态 SQL 允许使用的函数白名单，将逻辑函数名映射为 MySQL 实际表达式，降低 SQL 注入风险。</p>
 *
 * @author blake.qiu
 */
public enum TrustedMysqlFunctionEnum {
    
    /** 当前时间函数 {@code NOW()}，映射为毫秒精度 {@code NOW(3)}。 */
    NOW("NOW()", "NOW(3)");
    
    /** 函数名 → 枚举项的快速查找表。 */
    private static final Map<String, TrustedMysqlFunctionEnum> LOOKUP_MAP = new HashMap<>();
    
    // 启动时填充函数名索引
    static {
        for (TrustedMysqlFunctionEnum entry : TrustedMysqlFunctionEnum.values()) {
            LOOKUP_MAP.put(entry.functionName, entry);
        }
    }
    
    /** 逻辑函数名（Mapper 侧传入）。 */
    private final String functionName;
    
    /** MySQL 侧实际 SQL 函数表达式。 */
    private final String function;
    
    TrustedMysqlFunctionEnum(String functionName, String function) {
        this.functionName = functionName;
        this.function = function;
    }
    
    /**
     * 按函数名返回对应 MySQL SQL 函数片段。
     *
     * @param functionName function name
     * @return function
     */
    public static String getFunctionByName(String functionName) {
        TrustedMysqlFunctionEnum entry = LOOKUP_MAP.get(functionName);
        if (entry != null) {
            return entry.function;
        }
        throw new IllegalArgumentException(
            String.format("Invalid function name: %s", functionName));
    }
}
