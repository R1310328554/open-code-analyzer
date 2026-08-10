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
import com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum;

/**
 * PostgreSQL 数据库方言实现。
 *
 * <p>声明数据源类型为 PostgreSQL，并通过 {@link TrustedPostgresqlFunctionEnum} 将通用函数名映射为 PostgreSQL 内置 SQL 片段； 分页拼接 {@code OFFSET ? LIMIT ?} 语法。</p>
 *
 * @author xiweng.yy
 */
public class PostgresqlDatabaseDialect extends AbstractDatabaseDialect {
    
    /** 返回 PostgreSQL 数据源类型标识。 */
    @Override
    public String getType() {
        return DatabaseTypeConstant.POSTGRESQL;
    }
    
    /**
     * 按函数名解析 PostgreSQL 可信 SQL 函数。
     *
     * @param functionName 逻辑函数名
     * @return PostgreSQL 侧实际 SQL 函数表达式
     */
    @Override
    public String getFunction(String functionName) {
        return TrustedPostgresqlFunctionEnum.getFunctionByName(functionName);
    }
    
    /** 为 SQL 追加占位符形式的分页子句 {@code OFFSET ? LIMIT ?}。 */
    @Override
    public String getLimitPageSqlWithMark(String sql) {
        return sql + " OFFSET ? LIMIT ? ";
    }
    
    /** 为 SQL 追加字面量 offset/limit 分页子句。 */
    @Override
    public String getLimitPageSql(String sql, int pageNo, int pageSize) {
        return sql + "  OFFSET " + getPagePrevNum(pageNo, pageSize) + " LIMIT " + pageSize;
    }
    
    /** 按起始偏移量与页大小拼接分页子句。 */
    @Override
    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {
        return sql + "  OFFSET " + startOffset + " LIMIT " + pageSize;
    }
}
