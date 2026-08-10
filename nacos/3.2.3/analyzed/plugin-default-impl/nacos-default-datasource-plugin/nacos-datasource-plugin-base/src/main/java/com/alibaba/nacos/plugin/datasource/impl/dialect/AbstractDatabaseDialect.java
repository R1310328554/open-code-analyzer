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

import com.alibaba.nacos.plugin.datasource.dialect.DatabaseDialect;

import com.alibaba.nacos.plugin.datasource.constants.PrimaryKeyConstant;

/**
 * 数据库方言抽象基类，提供 MySQL/PostgreSQL 等数据库通用的分页与主键返回约定。
 *
 * <p>默认使用 {@code LIMIT offset, count} 语法拼接分页 SQL，子类可按具体数据库覆盖。</p>
 *
 * @author Long Yu
 */
public abstract class AbstractDatabaseDialect implements DatabaseDialect {
    
    /** 计算分页起始偏移量：{@code (page - 1) * pageSize}。 */
    @Override
    public int getPagePrevNum(int page, int pageSize) {
        return (page - 1) * pageSize;
    }
    
    /** 返回单页记录数（默认等于 pageSize）。 */
    @Override
    public int getPageLastNum(int page, int pageSize) {
        return pageSize;
    }
    
    /** 在 SQL 末尾追加 {@code LIMIT ?} 占位符（取前 N 条）。 */
    @Override
    public String getLimitTopSqlWithMark(String sql) {
        return sql + " LIMIT ? ";
    }
    
    /** 在 SQL 末尾追加 {@code LIMIT ?,?} 占位符（偏移量 + 页大小）。 */
    @Override
    public String getLimitPageSqlWithMark(String sql) {
        return sql + " LIMIT ?,? ";
    }
    
    /** 将分页参数直接拼入 SQL 的 LIMIT 子句。 */
    @Override
    public String getLimitPageSql(String sql, int pageNo, int pageSize) {
        return sql + "  LIMIT " + getPagePrevNum(pageNo, pageSize) + " , " + pageSize;
    }
    
    /** 按绝对偏移量拼接 LIMIT 分页 SQL。 */
    @Override
    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {
        return sql + "  LIMIT " + startOffset + " , " + pageSize;
    }
    
    /** 返回插入后需回填的主键列名（小写形式）。 */
    @Override
    public String[] getReturnPrimaryKeys() {
        return PrimaryKeyConstant.LOWER_RETURN_PRIMARY_KEYS;
    }
    
}
