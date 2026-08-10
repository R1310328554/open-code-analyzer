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

package com.alibaba.nacos.plugin.datasource.impl.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * PostgreSQL 可信内置 SQL 函数枚举（已废弃）。
 *
 * <p>白名单校验函数名，防止 Mapper 动态 SQL 拼接时引入注入风险。 请改用 {@link com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum}。</p>
 *
 * @author blake.qiu
 * @deprecated Use {@link com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum} replaced.
 */
@Deprecated
public enum TrustedPostgresqFunctionEnum {
    
    /** 当前时间函数 {@code NOW()}。 */

    NOW("NOW()", "NOW()");
    
    /** 函数名 → 枚举项的快速查找表。 */
    private static final Map<String, TrustedPostgresqFunctionEnum> LOOKUP_MAP = new HashMap<>();
    
    // 启动时填充函数名索引
    static {
        for (TrustedPostgresqFunctionEnum entry : TrustedPostgresqFunctionEnum.values()) {
            LOOKUP_MAP.put(entry.functionName, entry);
        }
    }
    
    private final String functionName;
    
    private final String function;
    
    TrustedPostgresqFunctionEnum(String functionName, String function) {
        this.functionName = functionName;
        this.function = function;
    }
    
    /**
     * 按函数名返回对应 SQL 函数片段。
     *
     * @param functionName function name
     * @return function
     */
    public static String getFunctionByName(String functionName) {
        TrustedPostgresqFunctionEnum entry = LOOKUP_MAP.get(functionName);
        if (entry != null) {
            return entry.function;
        }
        // 非白名单函数名直接拒绝
        throw new IllegalArgumentException(
            String.format("Invalid function name: %s", functionName));
    }
}
