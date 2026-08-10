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

package com.alibaba.nacos.persistence.datasource;

import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.configuration.DatasourceConfiguration;
import com.alibaba.nacos.persistence.monitor.DatasourceMetrics;
import com.alibaba.nacos.persistence.utils.ConnectionCheckUtil;
import com.alibaba.nacos.persistence.utils.DatasourcePlatformUtil;
import com.alibaba.nacos.persistence.utils.PersistenceExecutor;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 外部数据库数据源服务实现。
 *
 * <p>管理多 HikariCP 数据源的主从选举、健康巡检、PostgreSQL tenant_id 模式校验， 并提供 {@link JdbcTemplate} 与事务模板供持久层使用。</p>
 *
 * @author Nacos
 */
public class ExternalDataSourceServiceImpl implements DataSourceService {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ExternalDataSourceServiceImpl.class);
    
    /** JDBC 查询超时时间，单位：秒。 */
    private int queryTimeout = 3;
    
    private static final int TRANSACTION_QUERY_TIMEOUT = 5;
    
    private static final int DB_MASTER_SELECT_THRESHOLD = 1;
    
    private static final String DB_LOAD_ERROR_MSG = "[db-load-error]load jdbc.properties error";
    
    private static final String POSTGRESQL = "postgresql";
    
    private static final String POSTGRESQL_NULL_TENANT_MIGRATION_SCRIPT =
        "META-INF/pg-upgrade-null-tenant-id.sql";
    
    private static final String[] POSTGRESQL_CONFIG_TENANT_TABLES = {"config_info",
        "config_info_gray", "config_tags_relation", "his_config_info"};
    
    /** 已加载的外部 HikariCP 数据源列表。 */
    private List<HikariDataSource> dataSourceList = new ArrayList<>();
    
    private JdbcTemplate jt;
    
    private DataSourceTransactionManager tm;
    
    private TransactionTemplate tjt;
    
    private JdbcTemplate testMasterJt;
    
    private JdbcTemplate testMasterWritableJt;
    
    private volatile List<JdbcTemplate> testJtList;
    
    private volatile List<Boolean> isHealthList;
    
    /** 当前主库在 dataSourceList 中的索引。 */
    private volatile int masterIndex;
    
    private String dataSourceType = "";
    
    private final String defaultDataSourceType = "";
    
    @Override
    /** 初始化 JDBC 模板、事务管理器并启动主库选举与健康检查定时任务。 */
    public void init() {
        queryTimeout = ConvertUtils.toInt(System.getProperty("QUERYTIMEOUT"), 3);
        jt = new JdbcTemplate();
        // 限制最大返回行数，防止内存膨胀
        jt.setMaxRows(50000);
        jt.setQueryTimeout(queryTimeout);
        
        testMasterJt = new JdbcTemplate();
        testMasterJt.setQueryTimeout(queryTimeout);
        
        testMasterWritableJt = new JdbcTemplate();
        // 主库不可用时缩短超时，避免登录接口长时间阻塞
        testMasterWritableJt.setQueryTimeout(1);
        
        // 初始化各数据源健康检测用的 JdbcTemplate
        
        testJtList = new ArrayList<>();
        isHealthList = new ArrayList<>();
        
        tm = new DataSourceTransactionManager();
        tjt = new TransactionTemplate(tm);
        
        // 事务超时需与普通查询超时区分设置
        tjt.setTimeout(TRANSACTION_QUERY_TIMEOUT);
        
        dataSourceType = DatasourcePlatformUtil.getDatasourcePlatform(defaultDataSourceType);
        
        if (DatasourceConfiguration.isUseExternalDb()) {
            try {
                reload();
            } catch (IOException e) {
                LOGGER.error("[ExternalDataSourceService] datasource reload error", e);
                throw new RuntimeException(DB_LOAD_ERROR_MSG, e);
            }
            
            if (this.dataSourceList.size() > DB_MASTER_SELECT_THRESHOLD) {
                PersistenceExecutor.scheduleTask(new SelectMasterTask(), 10, 10, TimeUnit.SECONDS);
            }
            PersistenceExecutor.scheduleTask(new CheckDbHealthTask(), 10, 10, TimeUnit.SECONDS);
        }
    }
    
    @Override
    /** 重建数据源列表、重选主库并关闭旧连接池。 */
    public synchronized void reload() throws IOException {
        try {
            final List<JdbcTemplate> testJtListNew = new ArrayList<JdbcTemplate>();
            final List<Boolean> isHealthListNew = new ArrayList<Boolean>();
            
            List<HikariDataSource> dataSourceListNew = new ExternalDataSourceProperties()
                .build(EnvUtil.getEnvironment(), (dataSource) -> {
                    // 校验数据源连接可用性
                    ConnectionCheckUtil.checkDataSourceConnection(dataSource);
                    
                    JdbcTemplate jdbcTemplate = new JdbcTemplate();
                    jdbcTemplate.setQueryTimeout(queryTimeout);
                    jdbcTemplate.setDataSource(dataSource);
                    testJtListNew.add(jdbcTemplate);
                    isHealthListNew.add(Boolean.TRUE);
                });
            
            final List<HikariDataSource> dataSourceListOld = dataSourceList;
            final List<JdbcTemplate> testJtListOld = testJtList;
            dataSourceList = dataSourceListNew;
            testJtList = testJtListNew;
            isHealthList = isHealthListNew;
            new SelectMasterTask().run();
            validatePostgresqlTenantSchema();
            new CheckDbHealthTask().run();
            
            // 关闭旧数据源释放连接
            if (dataSourceListOld != null && !dataSourceListOld.isEmpty()) {
                for (HikariDataSource dataSource : dataSourceListOld) {
                    dataSource.close();
                }
            }
            if (testJtListOld != null && !testJtListOld.isEmpty()) {
                for (JdbcTemplate oldJdbc : testJtListOld) {
                    oldJdbc.setDataSource(null);
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (RuntimeException e) {
            LOGGER.error(DB_LOAD_ERROR_MSG, e);
            throw new IOException(e);
        }
    }
    
    @Override
    /** 通过 {@code SELECT @@read_only} 判断主库是否可写。 */
    public boolean checkMasterWritable() {
        
        testMasterWritableJt.setDataSource(jt.getDataSource());
        // 主库不可用时缩短超时，避免登录接口长时间阻塞
        testMasterWritableJt.setQueryTimeout(1);
        String sql = " SELECT @@read_only ";
        
        try {
            Integer result = testMasterWritableJt.queryForObject(sql, Integer.class);
            if (result == null) {
                return false;
            } else {
                return result == 0;
            }
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e, e);
            return false;
        }
        
    }
    
    @Override
    public JdbcTemplate getJdbcTemplate() {
        return this.jt;
    }
    
    @Override
    public TransactionTemplate getTransactionTemplate() {
        return this.tjt;
    }
    
    @Override
    public String getCurrentDbUrl() {
        DataSource ds = this.jt.getDataSource();
        if (ds == null) {
            return StringUtils.EMPTY;
        }
        HikariDataSource bds = (HikariDataSource) ds;
        return bds.getJdbcUrl();
    }
    
    @Override
    /** 汇总各库健康状态，主库异常返回 DOWN，从库异常返回 WARN。 */
    public String getHealth() {
        for (int i = 0; i < isHealthList.size(); i++) {
            if (!isHealthList.get(i)) {
                if (i == masterIndex) {
                    // 主库不健康
                    return "DOWN:"
                        + InternetAddressUtil.getIpFromString(dataSourceList.get(i).getJdbcUrl());
                } else {
                    // 从库不健康
                    return "WARN:"
                        + InternetAddressUtil.getIpFromString(dataSourceList.get(i).getJdbcUrl());
                }
            }
        }
        
        return "UP";
    }
    
    @Override
    public String getDataSourceType() {
        return dataSourceType;
    }
    
    /** PostgreSQL 平台下校验 config 表 tenant_id 列约束。 */
    void validatePostgresqlTenantSchema() {
        if (!POSTGRESQL.equalsIgnoreCase(dataSourceType) || null == jt.getDataSource()) {
            return;
        }
        validatePostgresqlTenantSchema(jt);
    }
    
    void validatePostgresqlTenantSchema(JdbcTemplate jdbcTemplate) {
        for (String tableName : POSTGRESQL_CONFIG_TENANT_TABLES) {
            validatePostgresqlTenantColumn(jdbcTemplate, tableName);
        }
    }
    
    private void validatePostgresqlTenantColumn(JdbcTemplate jdbcTemplate, String tableName) {
        String sql = "SELECT is_nullable, column_default FROM information_schema.columns "
            + "WHERE table_schema = current_schema() AND table_name = ? AND column_name = 'tenant_id'";
        try {
            Map<String, Object> columnInfo = jdbcTemplate.queryForMap(sql, tableName);
            String isNullable = null == columnInfo.get("is_nullable") ? StringUtils.EMPTY
                : String.valueOf(columnInfo.get("is_nullable"));
            String columnDefault = null == columnInfo.get("column_default") ? StringUtils.EMPTY
                : String.valueOf(columnInfo.get("column_default"));
            if (!"NO".equalsIgnoreCase(isNullable) || !StringUtils.contains(columnDefault, "''")) {
                throwIncompatiblePostgresqlTenantSchema(tableName);
            }
        } catch (DataAccessException e) {
            throw new IllegalStateException(
                buildIncompatiblePostgresqlTenantSchemaMessage(tableName), e);
        }
    }
    
    private void throwIncompatiblePostgresqlTenantSchema(String tableName) {
        throw new IllegalStateException(buildIncompatiblePostgresqlTenantSchemaMessage(tableName));
    }
    
    private String buildIncompatiblePostgresqlTenantSchemaMessage(String tableName) {
        return "PostgreSQL schema is incompatible for table '" + tableName
            + "': column 'tenant_id' must be "
            + "NOT NULL DEFAULT ''. Apply the migration script "
            + POSTGRESQL_NULL_TENANT_MIGRATION_SCRIPT
            + " before starting Nacos.";
    }
    
    /** 定时任务：通过试写探测可写主库并切换 JdbcTemplate。 */
    class SelectMasterTask implements Runnable {
        
        @Override
        public void run() {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("check master db.");
            }
            boolean isFound = false;
            
            int index = -1;
            for (HikariDataSource ds : dataSourceList) {
                index++;
                testMasterJt.setDataSource(ds);
                testMasterJt.setQueryTimeout(queryTimeout);
                try {
                    testMasterJt.update(
                        "DELETE FROM config_info WHERE data_id='com.alibaba.nacos.testMasterDB'");
                    if (jt.getDataSource() != ds) {
                        LOGGER.warn("[master-db] {}", ds.getJdbcUrl());
                    }
                    jt.setDataSource(ds);
                    tm.setDataSource(ds);
                    isFound = true;
                    masterIndex = index;
                    break;
                } catch (DataAccessException e) { // 只读库写入失败，继续尝试下一个
                    LOGGER.warn("[master-db] master db access error", e);
                }
            }
            
            if (!isFound) {
                LOGGER.error("[master-db] master db not found.");
                DatasourceMetrics.getDbException().increment();
            }
        }
    }
    
    /** 定时任务：对各数据源执行探活查询并更新健康标记。 */
    class CheckDbHealthTask implements Runnable {
        
        @Override
        public void run() {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("check db health.");
            }
            String sql = "SELECT * FROM config_info_gray WHERE id = 1";
            
            for (int i = 0; i < testJtList.size(); i++) {
                JdbcTemplate jdbcTemplate = testJtList.get(i);
                try {
                    try {
                        jdbcTemplate.queryForMap(sql);
                    } catch (EmptyResultDataAccessException e) {
                        // 空结果视为健康，忽略
                    }
                    isHealthList.set(i, Boolean.TRUE);
                } catch (DataAccessException e) {
                    if (i == masterIndex) {
                        LOGGER.error("[db-error] master db {} down.",
                            InternetAddressUtil
                                .getIpFromString(dataSourceList.get(i).getJdbcUrl()));
                    } else {
                        LOGGER.error("[db-error] slave db {} down.",
                            InternetAddressUtil
                                .getIpFromString(dataSourceList.get(i).getJdbcUrl()));
                    }
                    isHealthList.set(i, Boolean.FALSE);
                    
                    DatasourceMetrics.getDbException().increment();
                }
            }
        }
    }
}
