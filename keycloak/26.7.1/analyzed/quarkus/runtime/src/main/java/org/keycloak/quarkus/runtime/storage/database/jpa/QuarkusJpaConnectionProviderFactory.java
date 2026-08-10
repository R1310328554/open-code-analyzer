/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.storage.database.jpa;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import jakarta.enterprise.inject.Instance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.keycloak.ServerStartupError;
import org.keycloak.common.Version;
import org.keycloak.common.util.Environment;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.config.database.Database;
import org.keycloak.connections.jpa.AsyncCommitIntegrator;
import org.keycloak.connections.jpa.updater.JpaUpdaterProvider;
import org.keycloak.connections.jpa.util.JpaUtils;
import org.keycloak.migration.MigrationModelManager;
import org.keycloak.migration.ModelVersion;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.dblock.DBLockManager;
import org.keycloak.models.dblock.DBLockProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.configuration.DurationConverter;
import org.jboss.logging.Logger;

import static org.keycloak.config.TransactionOptions.MIGRATION_TRANSACTION_TIMEOUT;
import static org.keycloak.connections.jpa.util.JpaUtils.configureNamedQuery;
import static org.keycloak.models.utils.KeycloakModelUtils.runJobInTransaction;
import static org.keycloak.quarkus.runtime.storage.database.liquibase.QuarkusJpaUpdaterProvider.VERIFY_AND_RUN_MASTER_CHANGELOG;

/**
 * Quarkus 默认 JPA 连接工厂：启动时校验/迁移 schema、注册命名查询、检查数据库编码与索引，并暴露运维信息。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class QuarkusJpaConnectionProviderFactory extends AbstractJpaConnectionProviderFactory implements ServerInfoAwareProviderFactory {

    /** 持久化单元属性前缀，用于注册 Hibernate 命名查询（{@code kc.query.<name>}）。 */
    public static final String QUERY_PROPERTY_PREFIX = "kc.query.";
    /** 默认 CDI 持久化单元名。 */
    public static final String DEFAULT_PERSISTENCE_UNIT = "keycloak-default";
    private static final Logger logger = Logger.getLogger(QuarkusJpaConnectionProviderFactory.class);
    /** 查询 MIGRATION_MODEL 最新版本记录的 SQL（{@code %s} 为 schema 前缀）。 */
    private static final String SQL_GET_LATEST_VERSION = "SELECT ID, VERSION FROM %sMIGRATION_MODEL ORDER BY UPDATE_TIME DESC";
    private static final String MIGRATION_TRANSACTION_TIMEOUT_KEY = "migrationTransactionTimeout";

    /** 数据库 schema 迁移策略。 */
    enum MigrationStrategy {
        /** 自动执行 Liquibase 变更集。 */
        UPDATE,
        /** 仅校验 schema 是否与 changelog 一致。 */
        VALIDATE,
        /** 导出 SQL 脚本供人工执行，不自动迁移。 */
        MANUAL
    }

    /** 启动后填充的数据库连接与驱动运维信息。 */
    private Map<String, String> operationalInfo;

    @Override
    public String getId() {
        return "quarkus";
    }

    /** 从持久化单元属性中读取 {@link #QUERY_PROPERTY_PREFIX} 前缀项并注册命名查询。 */
    private void addSpecificNamedQueries(KeycloakSession session) {
        EntityManager em = createEntityManager(entityManagerFactory, session, false);

        try {
            Map<String, Object> unitProperties = entityManagerFactory.getProperties();

            for (Map.Entry<String, Object> entry : unitProperties.entrySet()) {
                if (entry.getKey().startsWith(QUERY_PROPERTY_PREFIX)) {
                    configureNamedQuery(entry.getKey().substring(QUERY_PROPERTY_PREFIX.length()), entry.getValue().toString(), em);
                }
            }
        } finally {
            JpaUtils.closeEntityManager(em);
        }
    }

    /**
     * 启动后初始化：异步提交监听、数据库健康检查、schema 迁移/校验、模型迁移与索引检查。
     */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        super.postInit(factory);
        if (config.getBoolean("asyncCommit", true)) {
            AsyncCommitIntegrator.registerListeners(entityManagerFactory);
        }

        checkMySQLWaitTimeout();
        checkMSSQLIsolationLevel();
        checkUtf8Encoding();

        String id = null;
        String version = null;
        String schema = getSchema();
        boolean schemaChanged;

        try {
            KeycloakModelUtils.setTransactionLimit(factory, getMigrationTransactionTimeout());
        } catch (Exception e) {
            logErrorSettingMigrationTransactionTimeout(e);
        }
        try (Connection connection = getConnection(); KeycloakSession session = factory.create()) {
            try {
                try (Statement statement = connection.createStatement()) {
                    try (ResultSet rs = statement.executeQuery(String.format(SQL_GET_LATEST_VERSION, getSchema(schema)))) {
                        if (rs.next()) {
                            id = rs.getString(1);
                            version = rs.getString(2);
                        }
                    }
                }
            } catch (SQLException ignore) {
                // 迁移表可能尚不存在，视为空库
            }
            createOperationalInfo(connection);
            addSpecificNamedQueries(session);
            schemaChanged = createOrUpdateSchema(schema, version, connection, session);
        } catch (SQLException cause) {
            throw new RuntimeException("Failed to update database.", cause);
        }

        if (schemaChanged || Environment.isNonServerMode()) {
            runJobInTransaction(factory, this::initSchema);
        } else {
            Version.RESOURCES_VERSION = id;
        }
        // 此处异常会终止启动，无需 finally 恢复超时
        try {
            // 0 表示恢复默认事务超时
            KeycloakModelUtils.setTransactionLimit(factory, 0);
        } catch (Exception e) {
            logErrorSettingMigrationTransactionTimeout(e);
        }

        checkMissingIndexes(factory);
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("initializeEmpty")
                .type("boolean")
                .helpText("Initialize database if empty. If set to false the database has to be manually initialized. If you want to manually initialize the database set migrationStrategy to manual which will create a file with SQL commands to initialize the database.")
                .defaultValue(true)
                .add()
                .property()
                .name("migrationStrategy")
                .type("string")
                .helpText("Strategy to use to migrate database. Valid values are update, manual and validate. Update will automatically migrate the database schema. Manual will export the required changes to a file with SQL commands that you can manually execute on the database. Validate will simply check if the database is up-to-date.")
                .options("update", "manual", "validate")
                .defaultValue("update")
                .add()
                .property()
                .name("migrationExport")
                .type("string")
                .helpText("Path for where to write manual database initialization/migration file.")
                .add()
                .property()
                .name("asyncCommit")
                .type("boolean")
                .helpText("If enabled, transactions that only modify ephemeral entities (such as authentication sessions or events) use asynchronous commit on PostgreSQL, skipping the WAL fsync wait. This improves throughput but means the last few milliseconds of such transactions may be lost on a crash. Automatically disabled on Aurora PostgreSQL when logical replication is active.")
                .defaultValue(true)
                .add()
                .build();
    }

    /** 优先从 CDI 解析默认 {@link EntityManagerFactory}，否则按 {@link #DEFAULT_PERSISTENCE_UNIT} 查找。 */
    @Override
    protected EntityManagerFactory getEntityManagerFactory() {
        Instance<EntityManagerFactory> instance = Arc.container().select(EntityManagerFactory.class);

        if (instance.isResolvable()) {
            return instance.get();
        }

        return getEntityManagerFactory(DEFAULT_PERSISTENCE_UNIT).orElseThrow(() -> new IllegalStateException("Failed to resolve the default entity manager factory"));
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        return operationalInfo;
    }

    @Override
    public int order() {
        return 100;
    }

    /** 解析迁移策略配置，兼容旧键 {@code databaseSchema}。 */
    private MigrationStrategy getMigrationStrategy() {
        String migrationStrategy = config.get("migrationStrategy");
        if (migrationStrategy == null) {
            // 向后兼容 databaseSchema 配置项
            migrationStrategy = config.get("databaseSchema");
        }

        if (migrationStrategy != null) {
            return MigrationStrategy.valueOf(migrationStrategy.toUpperCase());
        } else {
            return MigrationStrategy.UPDATE;
        }
    }

    private void initSchema(KeycloakSession session) {
        logger.debug("Calling migrateModel");
        migrateModel(session);
    }

    /** 在数据库锁保护下执行 Keycloak 模型迁移，防止多节点并发迁移。 */
    private void migrateModel(KeycloakSession session) {
        // 多节点同时启动时通过 DB 锁串行化迁移
        DBLockManager dbLockManager = new DBLockManager(session);
        DBLockProvider dbLock = dbLockManager.getDBLock();
        dbLock.waitForLock(DBLockProvider.Namespace.DATABASE);
        try {
            MigrationModelManager.migrate(session);
        } finally {
            dbLock.releaseLock();
        }
    }

    private String getSchema(String schema) {
        return schema == null ? "" : schema + ".";
    }

    private File getDatabaseUpdateFile() {
        String databaseUpdateFile = config.get("migrationExport", "keycloak-database-update.sql");
        return new File(databaseUpdateFile);
    }

    private void createOperationalInfo(Connection connection) {
        try {
            operationalInfo = new LinkedHashMap<>();
            DatabaseMetaData md = connection.getMetaData();
            operationalInfo.put("databaseUrl", md.getURL());
            operationalInfo.put("databaseUser", md.getUserName());
            operationalInfo.put("databaseProduct", md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
            operationalInfo.put("databaseDriver", md.getDriverName() + " " + md.getDriverVersion());
            operationalInfo.put("migrationTimeout", getMigrationTransactionTimeout() + " seconds");
            logger.debugf("Database info: %s", operationalInfo.toString());
        } catch (SQLException e) {
            logger.warn("Unable to prepare operational info due database exception: " + e.getMessage());
        }
    }

    /** 按迁移策略校验或更新 schema，并在 session 上标记是否需执行主 changelog。 */
    private boolean createOrUpdateSchema(String schema, String version, Connection connection, KeycloakSession session) {
        MigrationStrategy strategy = getMigrationStrategy();
        boolean initializeEmpty = config.getBoolean("initializeEmpty", true);
        File databaseUpdateFile = getDatabaseUpdateFile();

        JpaUpdaterProvider updater = session.getProvider(JpaUpdaterProvider.class);

        boolean requiresMigration = version == null || !version.equals(new ModelVersion(Version.VERSION).toString());
        session.setAttribute(VERIFY_AND_RUN_MASTER_CHANGELOG, requiresMigration);

        JpaUpdaterProvider.Status status = updater.validate(connection, schema);

        if (status == JpaUpdaterProvider.Status.VALID) {
            logger.debug("Database is up-to-date");
        } else if (status == JpaUpdaterProvider.Status.EMPTY) {
            if (initializeEmpty) {
                update(connection, schema, session, updater);
            } else {
                switch (strategy) {
                    case UPDATE:
                        update(connection, schema, session, updater);
                        break;
                    case MANUAL:
                        export(connection, schema, databaseUpdateFile, session, updater);
                        throw new ServerStartupError("Database not initialized, please initialize database with " + databaseUpdateFile.getAbsolutePath(), false);
                    case VALIDATE:
                        throw new ServerStartupError("Database not initialized, please enable database initialization", false);
                }
            }
        } else {
            switch (strategy) {
                case UPDATE:
                    update(connection, schema, session, updater);
                    break;
                case MANUAL:
                    export(connection, schema, databaseUpdateFile, session, updater);
                    throw new ServerStartupError("Database not up-to-date, please migrate database with " + databaseUpdateFile.getAbsolutePath(), false);
                case VALIDATE:
                    throw new ServerStartupError("Database not up-to-date, please enable database migration", false);
            }
        }

        return requiresMigration;
    }

    private void update(Connection connection, String schema, KeycloakSession session, JpaUpdaterProvider updater) {
        DBLockManager dbLockManager = new DBLockManager(session);
        DBLockProvider dbLock2 = dbLockManager.getDBLock();
        dbLock2.waitForLock(DBLockProvider.Namespace.DATABASE);
        try {
            updater.update(connection, schema);
        } finally {
            dbLock2.releaseLock();
        }
    }

    private void export(Connection connection, String schema, File databaseUpdateFile, KeycloakSession session,
            JpaUpdaterProvider updater) {
        DBLockManager dbLockManager = new DBLockManager(session);
        DBLockProvider dbLock2 = dbLockManager.getDBLock();
        dbLock2.waitForLock(DBLockProvider.Namespace.DATABASE);
        try {
            updater.export(connection, schema, databaseUpdateFile);
        } finally {
            dbLock2.releaseLock();
        }
    }

    /** 检查 MySQL/MariaDB {@code wait_timeout} 是否大于连接池最大生命周期，避免连接被服务端提前关闭。 */
    private void checkMySQLWaitTimeout() {
        String db = Configuration.getConfigValue(DatabaseOptions.DB).getValue();
        Database.Vendor vendor = Database.getVendor(db).orElseThrow();
        if (!(Database.Vendor.MYSQL == vendor || Database.Vendor.MARIADB == vendor)) {
            return;
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SHOW VARIABLES LIKE 'wait_timeout'")) {
            if (rs.next()) {
                var waitTimeout = rs.getInt(2);
                var poolMaxLifetime = DurationConverter.parseDuration(Configuration.getConfigValue(DatabaseOptions.DB_POOL_MAX_LIFETIME).getValue());
                if (poolMaxLifetime.getSeconds() >= waitTimeout) {
                    logger.warnf("%1$s 'wait_timeout=%2$d' is less than or equal to the configured '%3$s' duration. " +
                                "This can cause 'No operations allowed after connection closed' exceptions, which can impact Keycloak operations. " +
                                "To avoid such issues, set '%3$s' to a duration smaller than '%2$d' seconds.",
                          vendor, waitTimeout, DatabaseOptions.DB_POOL_MAX_LIFETIME.getKey(), poolMaxLifetime);
                }
            }
        } catch (SQLException e) {
            logger.warnf(e, "Unable to validate %s 'wait_timeout' due to database exception", vendor);
        }
    }

    /** 检查 SQL Server 隔离级别是否为 READ COMMITTED SNAPSHOT，高负载下可降低死锁风险。 */
    private void checkMSSQLIsolationLevel() {
        String db = Configuration.getConfigValue(DatabaseOptions.DB).getValue();
        Database.Vendor vendor = Database.getVendor(db).orElseThrow();
        if (Database.Vendor.MSSQL != vendor) {
            return;
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             Statement statement2 = connection.createStatement();
             ResultSet rs = statement.executeQuery("DBCC USEROPTIONS");
             ResultSet dbnameRs = statement2.executeQuery("SELECT DB_NAME() as db")) {
            dbnameRs.next();
            String dbName = dbnameRs.getString(1);
            while (rs.next()) {
                String option = rs.getString(1);
                String value = rs.getString(2);
                if ("isolation level".equalsIgnoreCase(option) && (!"read committed snapshot".equalsIgnoreCase(value))) {
                    logger.warnf("%s 'isolation level' for database '%s' is set to '%s'. Keycloak recommends 'read committed snapshot' isolation level to avoid deadlocks under high load. Please adjust the isolation level by executing 'ALTER DATABASE %s SET READ_COMMITTED_SNAPSHOT ON'.", vendor, dbName, rs.getString(2), dbName);
                }
            }
        } catch (SQLException e) {
            logger.warnf(e, "Unable to validate %s 'isolation level' due to database exception", vendor);
        }
    }

    public int getMigrationTransactionTimeout() {
        var value = config.get(MIGRATION_TRANSACTION_TIMEOUT_KEY, MIGRATION_TRANSACTION_TIMEOUT);
        var duration =  DurationConverter.parseDuration(value);
        // already validated by TransactionPropertyMappers
        assert duration != null;
        assert !duration.isZero();
        assert !duration.isNegative();
        return Math.toIntExact(duration.toSeconds());
    }

    private static void logErrorSettingMigrationTransactionTimeout(Exception e) {
        logger.debug("Unable to set the transaction timeout for migration task. Using the default timeout.", e);
    }

    private void checkUtf8Encoding() {
        String db = Configuration.getConfigValue(DatabaseOptions.DB).getValue();
        Database.Vendor vendor = Database.getVendor(db).orElseThrow();
        switch (vendor) {
            case TIDB, MARIADB, MYSQL -> checkMySQLUtf8(vendor);
            case POSTGRES -> checkPostgresEncoding();
            case MSSQL -> checkMSSQLEncoding();
            case ORACLE -> checkOracleEncoding();
            //H2 do we care?
        }
    }

    private void checkOracleEncoding() {
        checkEncoding(Database.Vendor.ORACLE, "AL32UTF8"::equals, "'AL32UTF8' encoding", "SELECT value FROM nls_database_parameters WHERE parameter = 'NLS_CHARACTERSET'");
    }

    private void checkMSSQLEncoding() {
        checkEncoding(Database.Vendor.MSSQL, s -> s.endsWith("_UTF8"), "any UTF-8 collation (ending with `_UTF8`)", "SELECT DATABASEPROPERTYEX(DB_NAME(), 'Collation') AS DatabaseCollation");
    }

    private void checkPostgresEncoding() {
        checkEncoding(Database.Vendor.POSTGRES, "UTF8"::equals, "'UFT8' encoding", "SELECT pg_encoding_to_char(encoding) FROM pg_database WHERE datname = current_database()");
    }

    private void checkMySQLUtf8(Database.Vendor vendor) {
        checkEncoding(vendor, "utf8mb4"::equals, "'utf8mb4' encoding", "SELECT default_character_set_name FROM information_schema.SCHEMATA WHERE schema_name = DATABASE()");
    }

    /**
     * 校验数据库字符集是否为有效 UTF-8 编码。
     * <p>自 26.6 起非 UTF-8 编码已弃用。</p>
     */
    private void checkEncoding(Database.Vendor vendor, Predicate<String> isValid, String recommendation, String query) {
        try (var connection = getConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery(query)) {
            rs.next();
            var encoding = rs.getString(1);
            if (isValid.test(encoding)) {
                return;
            }
            logInvalidEncoding(vendor, encoding, recommendation);
        } catch (SQLException e) {
            logger.warnf(e, "Unable to validate %s encoding due to database exception", vendor);
        }
    }

    private static void logInvalidEncoding(Database.Vendor vendor, String encoding, String recommendedEncoding) {
        logger.warnf("Invalid %s charset encoding '%s'. It is recommended to use %s", vendor, encoding, recommendedEncoding);
    }

    /** 在后台守护线程中异步检查缺失的数据库索引。 */
    private void checkMissingIndexes(KeycloakSessionFactory factory) {
        var thread = new Thread(new DatabaseIndexChecker(this::getConnection, factory, getSchema()), "db-index-checker");
        thread.setDaemon(true);
        thread.start();
    }
}
