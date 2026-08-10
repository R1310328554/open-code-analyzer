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

package com.alibaba.nacos.persistence.repository.embedded.operate;

import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.model.RestResultUtils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.configuration.condition.ConditionStandaloneEmbedStorage;
import com.alibaba.nacos.persistence.datasource.DataSourceService;
import com.alibaba.nacos.persistence.datasource.DynamicDataSource;
import com.alibaba.nacos.persistence.repository.embedded.sql.ModifyRequest;
import com.alibaba.nacos.persistence.repository.embedded.sql.limiter.SqlLimiter;
import com.alibaba.nacos.persistence.repository.embedded.sql.limiter.SqlTypeLimiter;
import com.alibaba.nacos.sys.utils.DiskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 单机模式下 Derby 数据库操作实现。
 *
 * <p>在 {@link ConditionStandaloneEmbedStorage} 条件下装配，直接使用本地 {@link JdbcTemplate} 与 {@link SqlTypeLimiter} 执行 SQL。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Conditional(ConditionStandaloneEmbedStorage.class)
@Component
public class StandaloneDatabaseOperateImpl implements BaseDatabaseOperate {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(StandaloneDatabaseOperateImpl.class);
    
    private final SqlLimiter sqlLimiter;
    
    private JdbcTemplate jdbcTemplate;
    
    private TransactionTemplate transactionTemplate;
    
    public StandaloneDatabaseOperateImpl() {
        this.sqlLimiter = new SqlTypeLimiter();
    }
    
    /** 初始化时从 {@link DynamicDataSource} 获取 JdbcTemplate 与事务模板。 */
    @PostConstruct
    protected void init() {
        DataSourceService dataSourceService = DynamicDataSource.getInstance().getDataSource();
        jdbcTemplate = dataSourceService.getJdbcTemplate();
        transactionTemplate = dataSourceService.getTransactionTemplate();
        LOGGER.info("use StandaloneDatabaseOperateImpl");
    }
    
    @Override
    public <R> R queryOne(String sql, Class<R> cls) {
        return queryOne(jdbcTemplate, sql, cls);
    }
    
    @Override
    public <R> R queryOne(String sql, Object[] args, Class<R> cls) {
        return queryOne(jdbcTemplate, sql, args, cls);
    }
    
    @Override
    public <R> R queryOne(String sql, Object[] args, RowMapper<R> mapper) {
        return queryOne(jdbcTemplate, sql, args, mapper);
    }
    
    @Override
    public <R> List<R> queryMany(String sql, Object[] args, RowMapper<R> mapper) {
        return queryMany(jdbcTemplate, sql, args, mapper);
    }
    
    @Override
    public <R> List<R> queryMany(String sql, Object[] args, Class<R> rClass) {
        return queryMany(jdbcTemplate, sql, args, rClass);
    }
    
    @Override
    public List<Map<String, Object>> queryMany(String sql, Object[] args) {
        return queryMany(jdbcTemplate, sql, args);
    }
    
    @Override
    public CompletableFuture<RestResult<String>> dataImport(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try (DiskUtils.LineIterator iterator = DiskUtils.lineIterator(file)) {
                // 每批最多 1000 条 SQL，异步并行导入
                List<String> batchUpdate = new ArrayList<>(batchSize);
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                List<Boolean> results = new CopyOnWriteArrayList<>();
                while (iterator.hasNext()) {
                    String sql = iterator.next();
                    if (StringUtils.isNotBlank(sql)) {
                        // 导入前校验 SQL 类型是否在白名单内
                        batchUpdate.add(sql);
                    }
                    if (batchUpdate.size() == batchSize || !iterator.hasNext()) {
                        List<ModifyRequest> sqls = batchUpdate.stream().map(s -> {
                            ModifyRequest request = new ModifyRequest();
                            request.setSql(s);
                            return request;
                        }).collect(Collectors.toList());
                        futures.add(CompletableFuture
                            .runAsync(() -> results.add(doDataImport(jdbcTemplate, sqls))));
                        batchUpdate.clear();
                    }
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                int code = 500;
                if (!CollectionUtils.isEmpty(results)) {
                    code = (!results.stream().anyMatch(Boolean.FALSE::equals)) ? 200 : 500;
                }
                return RestResult.<String>builder().withCode(code).withData("").build();
            } catch (Throwable ex) {
                LOGGER.error("An exception occurred when external data was imported into Derby : ",
                    ex);
                return RestResultUtils.failed(ex.getMessage());
            }
        });
    }
    
    @Override
    public Boolean update(List<ModifyRequest> modifyRequests,
        BiConsumer<Boolean, Throwable> consumer) {
        return update(transactionTemplate, jdbcTemplate, modifyRequests, consumer);
    }
    
    @Override
    public Boolean update(List<ModifyRequest> requestList) {
        return update(transactionTemplate, jdbcTemplate, requestList);
    }
}
