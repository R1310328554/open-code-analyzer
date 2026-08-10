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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.configuration.DatasourceConfiguration;
import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 本地 Derby 嵌入式数据源服务实现。
 *
 * <p>单机模式下在 Nacos 工作目录创建 Derby 库，执行 schema 脚本初始化表结构， 支持清理重建与备份恢复等运维操作。</p>
 *
 * @author Nacos
 */
public class LocalDataSourceServiceImpl implements DataSourceService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDataSourceServiceImpl.class);
    
    /** Derby 嵌入式 JDBC 驱动类名。 */
    private final String jdbcDriverName = "org.apache.derby.jdbc.EmbeddedDriver";
    
    private final String userName = "nacos";
    
    private final String password = "nacos";
    
    /** Derby 数据目录相对 Nacos Home 的路径。 */
    private final String derbyBaseDir =
        "data" + File.separator + PersistenceConstant.DERBY_BASE_DIR;
    
    private final String derbyShutdownErrMsg = "Derby system shutdown.";
    
    /** 本地 Derby JDBC 模板。 */
    private volatile JdbcTemplate jt;
    
    private volatile TransactionTemplate tjt;
    
    private boolean initialize = false;
    
    private boolean jdbcTemplateInit = false;
    
    private String healthStatus = "UP";
    
    /** 数据源类型标识：derby。 */
    private String dataSourceType = "derby";
    
    @Override
    /** 非外部库模式下创建 Derby 库并执行 schema 初始化。 */
    public synchronized void init() throws Exception {
        if (DatasourceConfiguration.isUseExternalDb()) {
            return;
        }
        if (!initialize) {
            LOGGER.info("use local db service for init");
            final String jdbcUrl =
                "jdbc:derby:" + Paths.get(EnvUtil.getNacosHome(), derbyBaseDir) + ";create=true";
            initialize(jdbcUrl);
            initialize = true;
        }
    }
    
    @Override
    /** 重新执行 derby-schema.sql 脚本。 */
    public synchronized void reload() {
        DataSource ds = jt.getDataSource();
        if (ds == null) {
            throw new RuntimeException("datasource is null");
        }
        try {
            execute(ds.getConnection(), "META-INF/derby-schema.sql");
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage(), e);
            }
            throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                "load derby-schema.sql error.", e);
        }
    }
    
    public DataSource getDatasource() {
        return jt.getDataSource();
    }
    
    /**
     * 关闭并删除 Derby 数据目录后重新创建库。
     *
     * @throws Exception 清理或重建失败时抛出
     */
    /** 清空本地 Derby 并重新初始化。 */
    public void cleanAndReopenDerby() throws Exception {
        doDerbyClean();
        final String jdbcUrl =
            "jdbc:derby:" + Paths.get(EnvUtil.getNacosHome(), derbyBaseDir).toString()
                + ";create=true";
        initialize(jdbcUrl);
    }
    
    /**
     * 清理 Derby 后执行自定义恢复逻辑并重新连接指定 JDBC URL。
     *
     * @param jdbcUrl 恢复后的 JDBC 连接串
     * @param callable 恢复中间步骤回调
     * @throws Exception 恢复失败时抛出
     */
    public void restoreDerby(String jdbcUrl, Callable<Void> callable) throws Exception {
        doDerbyClean();
        callable.call();
        initialize(jdbcUrl);
    }
    
    private void doDerbyClean() throws Exception {
        LOGGER.warn("use local db service for reopenDerby");
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (Exception e) {
            // Derby shutdown 会抛出预期异常，非 shutdown 消息则继续抛出
            if (!StringUtils.containsIgnoreCase(e.getMessage(), derbyShutdownErrMsg)) {
                throw e;
            }
        }
        DiskUtils.deleteDirectory(Paths.get(EnvUtil.getNacosHome(), derbyBaseDir).toString());
    }
    
    /** 配置 HikariCP 连接 Derby 并初始化 JdbcTemplate 与事务模板。 */
    private synchronized void initialize(String jdbcUrl) {
        DataSourcePoolProperties poolProperties =
            DataSourcePoolProperties.build(EnvUtil.getEnvironment());
        poolProperties.setDriverClassName(jdbcDriverName);
        poolProperties.setJdbcUrl(jdbcUrl);
        poolProperties.setUsername(userName);
        poolProperties.setPassword(password);
        HikariDataSource ds = poolProperties.getDataSource();
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(ds);
        if (jdbcTemplateInit) {
            jt.setDataSource(ds);
            tjt.setTransactionManager(tm);
        } else {
            jt = new JdbcTemplate();
            jt.setMaxRows(50000);
            jt.setQueryTimeout(5000);
            jt.setDataSource(ds);
            tjt = new TransactionTemplate(tm);
            tjt.setTimeout(5000);
            jdbcTemplateInit = true;
        }
        reload();
    }
    
    @Override
    public boolean checkMasterWritable() {
        return true;
    }
    
    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jt;
    }
    
    @Override
    public TransactionTemplate getTransactionTemplate() {
        return tjt;
    }
    
    @Override
    public String getCurrentDbUrl() {
        return "jdbc:derby:" + EnvUtil.getNacosHome() + File.separator + derbyBaseDir
            + ";create=true";
    }
    
    @Override
    public String getHealth() {
        return healthStatus;
    }
    
    @Override
    public String getDataSourceType() {
        return dataSourceType;
    }
    
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
    
    /**
     * 从 conf 目录或 classpath 加载 SQL 脚本并拆分为语句列表。
     *
     * @param sqlFile 脚本 classpath 路径
     * @return SQL 语句列表
     * @throws Exception 读取或解析失败时抛出
     */
    private List<String> loadSql(String sqlFile) throws Exception {
        List<String> sqlList = new ArrayList<>();
        InputStream sqlFileIn = null;
        try {
            File file = new File(
                EnvUtil.getNacosHome() + File.separator + "conf" + File.separator
                    + "derby-schema.sql");
            if (StringUtils.isBlank(EnvUtil.getNacosHome()) || !file.exists()) {
                ClassLoader classLoader = getClass().getClassLoader();
                URL url = classLoader.getResource(sqlFile);
                sqlFileIn = url.openStream();
            } else {
                sqlFileIn = new FileInputStream(file);
            }
            
            StringBuilder sqlSb = new StringBuilder();
            byte[] buff = new byte[1024];
            int byteRead = 0;
            while ((byteRead = sqlFileIn.read(buff)) != -1) {
                sqlSb.append(new String(buff, 0, byteRead, PersistenceConstant.DEFAULT_ENCODE));
            }
            
            String[] sqlArr = sqlSb.toString().split(";");
            for (int i = 0; i < sqlArr.length; i++) {
                String sql = sqlArr[i].replaceAll("--.*", "").trim();
                if (StringUtils.isNotEmpty(sql)) {
                    sqlList.add(sql);
                }
            }
            return sqlList;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            IoUtils.closeQuietly(sqlFileIn);
        }
    }
    
    /**
     * 在指定连接上逐条执行 SQL 脚本，单条失败仅记录警告。
     *
     * @param conn 数据库连接
     * @param sqlFile 脚本路径
     * @throws Exception 加载脚本失败时抛出
     */
    private void execute(Connection conn, String sqlFile) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            List<String> sqlList = loadSql(sqlFile);
            for (String sql : sqlList) {
                try {
                    stmt.execute(sql);
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }
    
}
