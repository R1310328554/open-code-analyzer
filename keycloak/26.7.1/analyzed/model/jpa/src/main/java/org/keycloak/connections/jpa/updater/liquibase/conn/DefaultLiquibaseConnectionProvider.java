/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.connections.jpa.updater.liquibase.conn;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.keycloak.Config;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.connections.jpa.updater.liquibase.LiquibaseJpaUpdaterProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import liquibase.Scope;
import liquibase.ThreadLocalScopeManager;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.ui.LoggerUIService;
import org.jboss.logging.Logger;

/**
 * Liquibase 连接的默认 SPI 实现。
 * <p>负责 JVM 级 Liquibase Scope 初始化、按 Keycloak 配置选择 {@link Database} 实现，并构建 {@link KeycloakLiquibase}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultLiquibaseConnectionProvider implements LiquibaseConnectionProviderFactory, LiquibaseConnectionProvider {

    private static final Logger logger = Logger.getLogger(DefaultLiquibaseConnectionProvider.class);

    /** 写入 {@link AbstractJdbcDatabase} 的索引创建行数阈值参数名。 */
    public static final String INDEX_CREATION_THRESHOLD_PARAM = "keycloak.indexCreationThreshold";

    /** 表行数超过此阈值时，{@link CustomCreateIndexChange} 可能跳过在线建索引。 */
    private long indexCreationThreshold;
    /** 由 Keycloak 数据库别名映射得到的 Liquibase Database 类。 */
    private Class<? extends Database> liquibaseDatabaseClazz;

    /** 保证同一 JVM 内 Liquibase 只初始化一次（多 Undertow/并行测试场景）。 */
    private static final AtomicBoolean INITIALIZATION = new AtomicBoolean(false);
    
    @Override
    public LiquibaseConnectionProvider create(KeycloakSession session) {
        if (! INITIALIZATION.get()) {
            // 需同步临界区：多 Undertow 或并行模型测试可能同时初始化 Liquibase，导致并发失败
            synchronized (INITIALIZATION) {
                if (! INITIALIZATION.get()) {
                    baseLiquibaseInitialization();
                    INITIALIZATION.set(true);
                }
            }
        }
        return this;
    }

    /** 初始化 Liquibase Scope：正确类加载器、资源访问器与 UI 服务。 */
    protected void baseLiquibaseInitialization() {
        // 必须用正确 ClassLoader 初始化 Scope，否则 Liquibase 无法加载扩展
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            Scope.setScopeManager(new ThreadLocalScopeManager());
            Scope.getCurrentScope();
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }

        // 子 Scope 绑定 ClassLoader 与 ResourceAccessor，确保自定义 Change 能正确加载
        final Map<String, Object> scopeValues = new HashMap<>();
        scopeValues.put(Scope.Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor(this.getClass().getClassLoader()));
        scopeValues.put(Scope.Attr.classLoader.name(), this.getClass().getClassLoader());
        scopeValues.put(Scope.Attr.ui.name(), new LoggerUIService());
        try {
            Scope.enter(scopeValues);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Liquibase: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(Config.Scope config) {
        indexCreationThreshold = config.getLong("indexCreationThreshold", 300000L);
        logger.debugf("indexCreationThreshold is %d", indexCreationThreshold);

        // 显式处理默认值：Config 可能非 MicroProfile，尚无实际 server 配置
        String dbAlias = config.root().get(DatabaseOptions.DB.getKey(), "dev-file");
        logger.debugf("dbAlias is %s", dbAlias);

        // 不依赖 Liquibase 自动探测 DB（可能对 EDB 等误判），按 Keycloak 厂商映射选定 Database 类
        String liquibaseType = org.keycloak.config.database.Database.getVendor(dbAlias).orElseThrow().getLiquibaseType();
        logger.debugf("liquibaseType is %s", liquibaseType);

        try {
            liquibaseDatabaseClazz = (Class<? extends Database>) Class.forName(liquibaseType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load Liquibase Database class: " + liquibaseType, e);
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "default";
    }

    /** 构建用于标准 JPA changelog 的 {@link KeycloakLiquibase}，并注入索引创建阈值。 */
    @Override
    public KeycloakLiquibase getLiquibase(Connection connection, String defaultSchema) throws LiquibaseException {
        Database database = getLiquibaseDatabase(connection);
        if (defaultSchema != null) {
            database.setDefaultSchemaName(defaultSchema);
        }

        String changelog = LiquibaseJpaUpdaterProvider.CHANGELOG;
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(getClass().getClassLoader());

        logger.debugf("Using changelog file %s and changelogTableName %s", changelog, database.getDatabaseChangeLogTableName());

        ((AbstractJdbcDatabase) database).set(INDEX_CREATION_THRESHOLD_PARAM, indexCreationThreshold);
        return new KeycloakLiquibase(changelog, resourceAccessor, database);
    }

    /** 构建用于自定义 changelog 与独立变更表的 {@link KeycloakLiquibase}。 */
    @Override
    public KeycloakLiquibase getLiquibaseForCustomUpdate(Connection connection, String defaultSchema, String changelogLocation, ClassLoader classloader, String changelogTableName) throws LiquibaseException {
        Database database = getLiquibaseDatabase(connection);
        if (defaultSchema != null) {
            database.setDefaultSchemaName(defaultSchema);
        }

        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(classloader);
        database.setDatabaseChangeLogTableName(changelogTableName);

        logger.debugf("Using changelog file %s and changelogTableName %s", changelogLocation, database.getDatabaseChangeLogTableName());

        return new KeycloakLiquibase(changelogLocation, resourceAccessor, database);
    }

    /** 与 Hibernate 类似，强制 Liquibase 使用 Keycloak 配置的 Database 实现而非自动探测。 */
    private Database getLiquibaseDatabase(Connection connection) {
        Database liquibaseDatabase;

        // 模拟 DatabaseFactory#findCorrectDatabaseImplementation：反射实例化已选定的 Database 类
        try {
            liquibaseDatabase = liquibaseDatabaseClazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + liquibaseDatabaseClazz.getName());
        }
        DatabaseConnection liquibaseConnection = new JdbcConnection(connection);
        try {
            logger.debugf("DB Product Name: %s", liquibaseConnection.getDatabaseProductName());
        } catch (LiquibaseException e) {
            logger.debug("Failed to detect DB Product Name", e);
        }
        liquibaseDatabase.setConnection(liquibaseConnection);

        return liquibaseDatabase;
    }

}
