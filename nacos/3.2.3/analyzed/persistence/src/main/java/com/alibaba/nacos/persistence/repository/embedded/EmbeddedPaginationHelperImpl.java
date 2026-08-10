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

package com.alibaba.nacos.persistence.repository.embedded;

import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.persistence.repository.PaginationHelper;
import com.alibaba.nacos.persistence.repository.embedded.operate.DatabaseOperate;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

/**
 * 嵌入式 Derby 存储的分页查询辅助实现。
 *
 * <p>通过 {@link DatabaseOperate} 执行 COUNT 与数据查询，封装 {@link PaginationHelper} 接口供配置/命名等模块复用。</p>
 *
 * @param <E> Generic class
 * @author boyan
 * @date 2010-5-6
 */
public class EmbeddedPaginationHelperImpl<E> implements PaginationHelper<E> {
    
    private final DatabaseOperate databaseOperate;
    
    public EmbeddedPaginationHelperImpl(DatabaseOperate databaseOperate) {
        this.databaseOperate = databaseOperate;
    }
    
    /**
     * 标准分页查询：先统计总数再拉取当前页数据。
     *
     * @param sqlCountRows Query total SQL
     * @param sqlFetchRows Query data sql
     * @param args         query args
     * @param pageNo       page number
     * @param pageSize     page size
     * @param rowMapper    Entity mapping
     * @return Paging data
     */
    @Override
    public Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows,
        final Object[] args,
        final int pageNo, final int pageSize, final RowMapper rowMapper) {
        return fetchPage(sqlCountRows, sqlFetchRows, args, pageNo, pageSize, null, rowMapper);
    }
    
    @Override
    public Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, Object[] args,
        final int pageNo,
        final int pageSize, final Long lastMaxId, final RowMapper rowMapper) {
        return doFetchPage(sqlCountRows, args, sqlFetchRows, args, pageNo, pageSize, rowMapper);
    }
    
    @Override
    public Page<E> fetchPageLimit(final String sqlCountRows, final String sqlFetchRows,
        final Object[] args,
        final int pageNo, final int pageSize, final RowMapper rowMapper) {
        return doFetchPage(sqlCountRows, null, sqlFetchRows, args, pageNo, pageSize, rowMapper);
    }
    
    @Override
    public Page<E> fetchPageLimit(final String sqlCountRows, final Object[] args1,
        final String sqlFetchRows,
        final Object[] args2, final int pageNo, final int pageSize, final RowMapper rowMapper) {
        return doFetchPage(sqlCountRows, args1, sqlFetchRows, args2, pageNo, pageSize, rowMapper);
    }
    
    @Override
    public Page<E> fetchPageLimit(final String sqlFetchRows, final Object[] args, final int pageNo,
        final int pageSize,
        final RowMapper rowMapper) {
        checkPageInfo(pageNo, pageSize);
        // 构造分页结果对象
        final Page<E> page = new Page<>();
        
        List<E> result = databaseOperate.queryMany(sqlFetchRows, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }
    
    @Override
    public Page fetchPageLimit(MapperResult countMapperResult, MapperResult mapperResult,
        int pageNo, int pageSize,
        RowMapper rowMapper) {
        return fetchPageLimit(countMapperResult.getSql(),
            countMapperResult.getParamList().toArray(),
            mapperResult.getSql(), mapperResult.getParamList().toArray(), pageNo, pageSize,
            rowMapper);
    }
    
    @Override
    public void updateLimit(final String sql, final Object[] args) {
        EmbeddedStorageContextHolder.addSqlContext(sql, args);
        try {
            databaseOperate.update(EmbeddedStorageContextHolder.getCurrentSqlContext());
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
    private void checkPageInfo(final int pageNo, final int pageSize) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
    }
    
    private Page<E> doFetchPage(final String sqlCountRows, final Object[] countAgrs,
        final String sqlFetchRows,
        final Object[] fetchArgs, final int pageNo, final int pageSize, final RowMapper rowMapper) {
        checkPageInfo(pageNo, pageSize);
        // 查询符合条件的记录总数
        Integer rowCountInt = null;
        if (null != countAgrs) {
            rowCountInt = databaseOperate.queryOne(sqlCountRows, countAgrs, Integer.class);
        } else {
            rowCountInt = databaseOperate.queryOne(sqlCountRows, Integer.class);
        }
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }
        
        // 根据总数与 pageSize 计算总页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }
        
        // Create Page object
        final Page<E> page = new Page<>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);
        
        if (pageNo > pageCount) {
            return page;
        }
        
        List<E> result = databaseOperate.queryMany(sqlFetchRows, fetchArgs, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }
}
