/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.impl.dialect;

import com.alibaba.nacos.plugin.datasource.constants.DatabaseTypeConstant;
import com.alibaba.nacos.plugin.datasource.impl.enums.oracle.TrustedOracleFunctionEnum;

/**
 * Oracle 数据库方言实现。
 *
 * <p>注册 Oracle 类型标识，并通过 {@link TrustedOracleFunctionEnum} 解析可信 SQL 函数映射。</p>
 *
 * @author xiweng.yy
 */
public class OracleDatabaseDialect extends AbstractDatabaseDialect {
    
    /** 返回 Oracle 数据库类型常量。 */
    @Override
    public String getType() {
        return DatabaseTypeConstant.ORACLE;
    }
    
    /** 将通用函数名映射为 Oracle 方言 SQL 片段（如 NOW → SYSDATE）。 */
    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }
}
