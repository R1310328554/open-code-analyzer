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
import com.alibaba.nacos.plugin.datasource.impl.enums.mysql.TrustedMysqlFunctionEnum;

/**
 * MySQL 默认数据库方言实现（兼容旧命名）。
 *
 * <p>声明数据源类型为 MySQL，并通过 {@link TrustedMysqlFunctionEnum} 解析可信 SQL 函数。</p>
 *
 * @author Long Yu
 */
public class DefaultDatabaseDialect extends AbstractDatabaseDialect {
    
    /** 返回 MySQL 数据源类型标识。 */
    @Override
    public String getType() {
        return DatabaseTypeConstant.MYSQL;
    }
    
    /**
     * 按函数名解析 MySQL 可信 SQL 函数。
     *
     * @param functionName 逻辑函数名
     * @return MySQL 侧实际 SQL 函数表达式
     */
    @Override
    public String getFunction(String functionName) {
        return TrustedMysqlFunctionEnum.getFunctionByName(functionName);
    }
    
}
