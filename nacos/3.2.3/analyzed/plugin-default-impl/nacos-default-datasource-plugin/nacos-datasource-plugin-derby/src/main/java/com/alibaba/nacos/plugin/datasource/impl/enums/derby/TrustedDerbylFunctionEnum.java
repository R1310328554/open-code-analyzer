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

package com.alibaba.nacos.plugin.datasource.impl.enums.derby;

import java.util.HashMap;
import java.util.Map;

/**
 * Derby 可信内置 SQL 函数枚举（旧版命名，供 {@link com.alibaba.nacos.plugin.datasource.impl.derby.AbstractMapperByDerby} 使用）。
 *
 * <p>白名单校验函数名，防止 Mapper 动态 SQL 拼接时引入注入风险。</p>
 *
 * @author blake.qiu
 */
public enum TrustedDerbylFunctionEnum {
    
    /** 当前时间函数 {@code NOW()}，映射为 {@code CURRENT_TIMESTAMP}。 */
    NOW("NOW()", "CURRENT_TIMESTAMP");
    
    /** 函数名 → 枚举项的快速查找表。 */
    private static final Map<String, TrustedDerbylFunctionEnum> LOOKUP_MAP = new HashMap<>();
    
    // 启动时填充函数名索引
    static {
        for (TrustedDerbylFunctionEnum entry : TrustedDerbylFunctionEnum.values()) {
            LOOKUP_MAP.put(entry.functionName, entry);
        }
    }
    
    /** 逻辑函数名（Mapper 侧传入）。 */
    private final String functionName;
    
    /** Derby 侧实际 SQL 函数表达式。 */
    private final String function;
    
    TrustedDerbylFunctionEnum(String functionName, String function) {
        this.functionName = functionName;
        this.function = function;
    }
    
    /**
     * 按函数名返回对应 Derby SQL 函数片段。
     *
     * @param functionName function name
     * @return function
     */
    public static String getFunctionByName(String functionName) {
        TrustedDerbylFunctionEnum entry = LOOKUP_MAP.get(functionName);
        if (entry != null) {
            return entry.function;
        }
        throw new IllegalArgumentException(
            String.format("Invalid function name: %s", functionName));
    }
}
