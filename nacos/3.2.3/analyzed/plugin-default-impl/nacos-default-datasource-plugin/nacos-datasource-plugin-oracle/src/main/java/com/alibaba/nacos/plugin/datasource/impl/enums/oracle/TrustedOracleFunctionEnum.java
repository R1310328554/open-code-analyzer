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

package com.alibaba.nacos.plugin.datasource.impl.enums.oracle;

import java.util.HashMap;
import java.util.Map;

/**
 * Oracle 可信内置 SQL 函数枚举。
 *
 * <p>白名单校验函数名，防止 Mapper 动态 SQL 拼接时引入注入风险； 将跨数据库通用函数名映射为 Oracle 等价写法。</p>
 *
 * @author liam.fu
 */
public enum TrustedOracleFunctionEnum {
    
    /** 当前时间：通用 {@code NOW()} 映射为 Oracle {@code SYSDATE}。 */

    NOW("NOW()", "SYSDATE");
    
    /** 函数名 → 枚举项的快速查找表。 */
    private static final Map<String, TrustedOracleFunctionEnum> LOOKUP_MAP = new HashMap<>();
    
    // 启动时填充函数名索引
    static {
        for (TrustedOracleFunctionEnum entry : TrustedOracleFunctionEnum.values()) {
            LOOKUP_MAP.put(entry.functionName, entry);
        }
    }
    
    private final String functionName;
    
    private final String function;
    
    TrustedOracleFunctionEnum(String functionName, String function) {
        this.functionName = functionName;
        this.function = function;
    }
    
    /**
     * 按函数名返回对应 Oracle SQL 函数片段。
     *
     * @param functionName function name
     * @return function
     */
    public static String getFunctionByName(String functionName) {
        TrustedOracleFunctionEnum entry = LOOKUP_MAP.get(functionName);
        if (entry != null) {
            return entry.function;
        }
        // 非白名单函数名直接拒绝
        throw new IllegalArgumentException(
            String.format("Invalid function name: %s", functionName));
    }
}
