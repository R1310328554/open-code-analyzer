/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.persistence;

import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;
import org.springframework.jdbc.core.RowMapper;

/**
 * 鉴权插件分页查询助手接口。
 *
 * <p>封装 count + fetch 双 SQL 分页、LIMIT/OFFSET 方言及 {@link MapperResult} 驱动查询。</p>
 *
 * @param <E> Generic class
 * @author huangKeMing
 */
public interface AuthPaginationHelper<E> {
    
    /** 标准双 SQL 分页查询（count + fetch）。 */
    Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args,
        final int pageNo,
        final int pageSize, final RowMapper<E> rowMapper);
    
    /** 带 lastMaxId 游标的分页查询（适用于大表翻页）。 */
    Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args,
        final int pageNo,
        final int pageSize, final Long lastMaxId, final RowMapper<E> rowMapper);
    
    /** 使用 LIMIT/OFFSET 方言的分页查询。 */
    Page<E> fetchPageLimit(final String sqlCountRows, final String sqlFetchRows,
        final Object[] args, final int pageNo,
        final int pageSize, final RowMapper<E> rowMapper);
    
    Page<E> fetchPageLimit(final String sqlCountRows, final Object[] args1,
        final String sqlFetchRows,
        final Object[] args2, final int pageNo, final int pageSize, final RowMapper<E> rowMapper);
    
    Page<E> fetchPageLimit(final String sqlFetchRows, final Object[] args, final int pageNo,
        final int pageSize,
        final RowMapper<E> rowMapper);
    
    /** 基于 {@link MapperResult} 的多数据源分页查询。 */
    Page<E> fetchPageLimit(final MapperResult countMapperResult, final MapperResult mapperResult,
        final int pageNo,
        final int pageSize, final RowMapper<E> rowMapper);
    
    /** 执行带 LIMIT 约束的更新语句。 */
    void updateLimit(final String sql, final Object[] args);
    
}
