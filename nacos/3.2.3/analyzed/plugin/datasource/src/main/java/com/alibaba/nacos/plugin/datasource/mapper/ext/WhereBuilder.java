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

package com.alibaba.nacos.plugin.datasource.mapper.ext;

import com.alibaba.nacos.common.constant.Symbols;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * WHERE 子句流式构建器。
 *
 * <p>以链式 API 拼装 SELECT 语句的 WHERE、分页、排序及 EXISTS 子查询，
 * 最终输出 {@link MapperResult} 供 Mapper 执行。</p>
 *
 * @author haiqi.wang
 * @date 2024/08/13
 */
public final class WhereBuilder {
    
    /** 基础 SQL（SELECT…FROM 部分，不含 WHERE）。 */
    private final String sql;
    
    /** 预编译占位符对应的参数列表。 */
    private final List<Object> parameters = new ArrayList<>();
    
    /** WHERE 条件片段累积器。 */
    private final StringBuilder where = new StringBuilder(" WHERE ");
    
    /**
     * 构造构建器。
     *
     * @param sql 基础 SQL 脚本
     */
    public WhereBuilder(String sql) {
        this.sql = sql;
    }
    
    /**
     * 追加 AND 连接符。
     *
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder and() {
        where.append(" AND ");
        return this;
    }
    
    /** 追加左括号，用于分组条件。 */
    public WhereBuilder startParentheses() {
        where.append(" ( ");
        return this;
    }
    
    /** 追加右括号，结束分组条件。 */
    public WhereBuilder endParentheses() {
        where.append(" ) ");
        return this;
    }
    
    /**
     * 追加 OR 连接符。
     *
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder or() {
        where.append(" OR ");
        return this;
    }
    
    /**
     * 追加等值条件 {@code field = ?}。
     *
     * @param filed     字段名
     * @param parameter 绑定参数值
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder eq(String filed, Object parameter) {
        where.append(filed).append(" = ? ");
        parameters.add(parameter);
        return this;
    }
    
    /**
     * 追加 LIKE 条件 {@code field LIKE ?}。
     *
     * @param filed     字段名
     * @param parameter 绑定参数值
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder like(String filed, Object parameter) {
        where.append(filed).append(" LIKE ? ");
        parameters.add(parameter);
        return this;
    }
    
    /**
     * 追加带 ESCAPE 的 LIKE 条件。
     *
     * @param filed     字段名
     * @param parameter 绑定参数值
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder likeWithEscape(String filed, Object parameter) {
        where.append(filed).append(" LIKE ? ESCAPE '\\' ");
        parameters.add(parameter);
        return this;
    }
    
    /**
     * 追加 IN 条件 {@code field IN (?, …)}。
     *
     * @param filed         字段名
     * @param parameterArr  参数数组
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder in(String filed, Object[] parameterArr) {
        where.append(filed).append(" IN (");
        for (int i = 0; i < parameterArr.length; i++) {
            if (i != 0) {
                where.append(", ");
            }
            where.append('?');
            parameters.add(parameterArr[i]);
        }
        where.append(") ");
        return this;
    }
    
    /**
     * 追加 OFFSET/FETCH 分页（SQL Server 等方言）。
     *
     * @param startRow 起始行（偏移量）
     * @param pageSize 每页条数
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder offset(int startRow, int pageSize) {
        where.append(" OFFSET ")
            .append(startRow)
            .append(" ROWS FETCH NEXT ")
            .append(pageSize)
            .append(" ROWS ONLY");
        return this;
    }
    
    /**
     * 追加 LIMIT 分页（MySQL 等方言）。
     *
     * @param startRow 起始行
     * @param pageSize 每页条数
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder limit(int startRow, int pageSize) {
        where.append(" LIMIT ")
            .append(startRow)
            .append(Symbols.COMMA)
            .append(pageSize);
        return this;
    }
    
    /**
     * 追加 GROUP BY 子句。
     *
     * @param fields 分组字段
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder groupBy(String fields) {
        where.append(" GROUP BY ").append(fields);
        return this;
    }
    
    /**
     * 追加 ORDER BY 子句。
     *
     * @param fields 排序字段
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder orderBy(String fields) {
        where.append(" ORDER BY ").append(fields);
        return this;
    }
    
    /**
     * 追加 EXISTS 子查询条件。
     * <p>
     * 用于子查询过滤，示例：
     * <pre>
     * builder.exists(SELECT 1 FROM tags b WHERE, sub -> {
     * sub.eqColumn("b.id", "a.id").and().like("b.tag", "dev");
     * });
     * </pre>
     *
     * @param subSqlPrefix 子查询前缀，通常为 "SELECT 1 FROM table WHERE "
     * @param consumer     构建子查询条件的 Lambda
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder exists(String subSqlPrefix, Consumer<WhereBuilder> consumer) {
        WhereBuilder subBuilder = new WhereBuilder("");
        subBuilder.where.setLength(0);
        consumer.accept(subBuilder);
        MapperResult res = subBuilder.build();
        
        where.append(" EXISTS ( ").append(subSqlPrefix).append(res.getSql()).append(" ) ");
        
        if (res.getParamList() != null) {
            parameters.addAll(res.getParamList());
        }
        return this;
    }
    
    /**
     * 追加列间等值比较 {@code field1 = field2}。
     * <p>
     * 与 {@link #eq(String, Object)} 不同，此方法直接比较两列，不使用占位符。
     *
     * @param field1 第一个字段名（如 "b.id"）
     * @param field2 第二个字段名（如 "a.id"）
     * @return 当前 {@link WhereBuilder} 实例
     */
    public WhereBuilder eqColumn(String field1, String field2) {
        where.append(field1).append(" = ").append(field2).append(" ");
        return this;
    }
    
    /**
     * 组装完整 SQL 并返回 {@link MapperResult}。
     *
     * @return 含 SQL 与参数列表的映射结果
     */
    public MapperResult build() {
        return new MapperResult(sql + where, parameters);
    }
}
