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

package org.keycloak.connections.jpa.updater.liquibase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.updater.JpaUpdaterProvider;
import org.keycloak.connections.jpa.updater.liquibase.conn.KeycloakLiquibase;
import org.keycloak.connections.jpa.updater.liquibase.conn.LiquibaseConnectionProvider;
import org.keycloak.connections.jpa.updater.liquibase.conn.MySQLCustomChangeLogHistoryService;
import org.keycloak.connections.jpa.util.JpaUtils;
import org.keycloak.models.KeycloakSession;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.StreamUtil;
import org.jboss.logging.Logger;

/**
 * 基于 Liquibase 的 JPA 数据库升级核心实现：驱动主 changelog 与扩展 {@link JpaEntityProvider} changelog 的校验、升级与 SQL 导出。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LiquibaseJpaUpdaterProvider implements JpaUpdaterProvider {

    private static final Logger logger = Logger.getLogger(LiquibaseJpaUpdaterProvider.class);

    /** Keycloak 内置 JPA 主 changelog 的 classpath 路径。 */
    public static final String CHANGELOG = "META-INF/jpa-changelog-master.xml";

    /** DATABASECHANGELOG 表中部署标识列名，旧库升级时需补建。 */
    public static final String DEPLOYMENT_ID_COLUMN = "DEPLOYMENT_ID";

    /** 当前 Keycloak 会话，用于获取 Liquibase 连接 Provider 与扩展实体 Provider。 */
    private final KeycloakSession session;

    public LiquibaseJpaUpdaterProvider(KeycloakSession session) {
        this.session = session;
    }

    /** 类级同步执行 schema 升级，避免并发启动时重复应用 changeset。 */
    @Override
    public void update(Connection connection, String defaultSchema) {
        synchronized (LiquibaseJpaUpdaterProvider.class) {
            updateSynch(connection, null, defaultSchema);
        }
    }

    /** 类级同步导出待执行 SQL 脚本到文件。 */
    @Override
    public void export(Connection connection, String defaultSchema, File file) {
        synchronized (LiquibaseJpaUpdaterProvider.class) {
            updateSynch(connection, file, defaultSchema);
        }
    }

    /**
     * 升级/导出的同步核心逻辑：先处理主 changelog，再依次处理各 {@link JpaEntityProvider} 的自定义 changelog。
     */
    private void updateSynch(Connection connection, File file, String defaultSchema) {
        logger.debug("Starting database update");

        // Liquibase 任务无法注入自定义对象，通过 ThreadLocal 传递当前 KeycloakSession
        ThreadLocalSessionContext.setCurrentSession(session);

        Writer exportWriter = null;
        try {
            // 首先执行 Keycloak 主 changelog
            KeycloakLiquibase liquibase = getLiquibaseForKeycloakUpdate(connection, defaultSchema);
            if (file != null) {
                exportWriter = new FileWriter(file);
            }
            updateChangeSet(liquibase, exportWriter);

            // 再为每个注册了自定义 changelog 的 JpaEntityProvider 执行升级
            Set<JpaEntityProvider> jpaProviders = session.getAllProviders(JpaEntityProvider.class);
            for (JpaEntityProvider jpaProvider : jpaProviders) {
                String customChangelog = jpaProvider.getChangelogLocation();
                if (customChangelog != null) {
                    String factoryId = jpaProvider.getFactoryId();
                    String changelogTableName = JpaUtils.getCustomChangelogTableName(factoryId);
                    liquibase = getLiquibaseForCustomProviderUpdate(connection, defaultSchema, customChangelog, jpaProvider.getClass().getClassLoader(), changelogTableName);
                    updateChangeSet(liquibase, exportWriter);
                }
            }
        } catch (LiquibaseException | IOException | SQLException e) {
            logger.error("Error has occurred while updating the database", e);
            throw new RuntimeException("Failed to update database", e);
        } finally {
            ThreadLocalSessionContext.removeCurrentSession();
            if (exportWriter != null) {
                try {
                    exportWriter.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }

    /**
     * 对单个 Liquibase 实例执行 changeset 升级或导出：补建 DEPLOYMENT_ID 列、应用未运行 changeset、重置 Liquibase 服务。
     */
    protected void updateChangeSet(KeycloakLiquibase liquibase, Writer exportWriter) throws LiquibaseException, SQLException {
        String changelog = liquibase.getChangeLogFile();
        Database database = liquibase.getDatabase();
        Table changelogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl(database, false, Table.class, Column.class), database);

        if (changelogTable != null) {
            boolean hasDeploymentIdColumn = changelogTable.getColumn(DEPLOYMENT_ID_COLUMN) != null;

            // 旧版 DATABASECHANGELOG 表缺少 DEPLOYMENT_ID 列时在线补建
            if (!hasDeploymentIdColumn) {
                ChangeLogHistoryService changelogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
                changelogHistoryService.generateDeploymentId();
                String deploymentId = changelogHistoryService.getDeploymentId();

                logger.debugv("Adding missing column {0}={1} to {2} table", DEPLOYMENT_ID_COLUMN, deploymentId,changelogTable.getName());

                List<SqlStatement> statementsToExecute = new ArrayList<>();
                statementsToExecute.add(new AddColumnStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
                        changelogTable.getName(), DEPLOYMENT_ID_COLUMN, "VARCHAR(10)", null));
                statementsToExecute.add(new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), changelogTable.getName())
                        .addNewColumnValue(DEPLOYMENT_ID_COLUMN, deploymentId));
                statementsToExecute.add(new SetNullableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
                        changelogTable.getName(), DEPLOYMENT_ID_COLUMN, "VARCHAR(10)", false));

                ExecutorService executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);
                Executor executor = executorService.getExecutor(LiquibaseConstants.JDBC_EXECUTOR, liquibase.getDatabase());

                for (SqlStatement sql : statementsToExecute) {
                    executor.execute(sql);
                    database.commit();
                }
            }
        }

        List<ChangeSet> changeSets = getLiquibaseUnrunChangeSets(liquibase);
        if (!changeSets.isEmpty()) {
            List<RanChangeSet> ranChangeSets = liquibase.getDatabase().getRanChangeSetList();
            if (ranChangeSets.isEmpty()) {
                logger.infov("Initializing database schema. Using changelog {0}", changelog);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debugv("Updating database from {0} to {1}. Using changelog {2}", ranChangeSets.get(ranChangeSets.size() - 1).getId(), changeSets.get(changeSets.size() - 1).getId(), changelog);
                } else {
                    logger.infov("Updating database. Using changelog {0}", changelog);
                }
            }

            if (exportWriter != null) {
                // 导出模式：空库时先输出 changelog 表建表语句
                if (ranChangeSets.isEmpty()) {
                    outputChangeLogTableCreationScript(liquibase, exportWriter);
                }
                liquibase.update(null, new LabelExpression(), exportWriter, false);
            } else {
                liquibase.update((Contexts) null);
            }

            logger.debugv("Completed database update for changelog {0}", changelog);
        } else {
            logger.debugv("Database is up to date for changelog {0}", changelog);
        }

        // 重置 Liquibase 单例服务，避免 ChangeLogHistoryServiceFactory 状态污染后续 changelog
        // 参见 KEYCLOAK-3769 讨论
        resetLiquibaseServices(liquibase);
    }

    /** 向导出 Writer 写入 DATABASECHANGELOG 建表及 MySQL 主键补丁 SQL。 */
    private void outputChangeLogTableCreationScript(Liquibase liquibase, final Writer exportWriter) throws DatabaseException {
        Database database = liquibase.getDatabase();

        ExecutorService executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);
        Executor oldTemplate = executorService.getExecutor(LiquibaseConstants.JDBC_EXECUTOR, database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(oldTemplate, exportWriter, database);
        executorService.setExecutor(LiquibaseConstants.JDBC_EXECUTOR, database, loggingExecutor);

        loggingExecutor.comment("*********************************************************************");
        loggingExecutor.comment("* Keycloak database creation script - apply this script to empty DB *");
        loggingExecutor.comment("*********************************************************************" + StreamUtil.getLineSeparator());

        // DatabaseChangeLogTable 由 Liquibase 自动写入脚本
        // DatabaseChangeLogLockTable 在 KeycloakApplication 构造链路的 CustomLockService.init() 中
        // 已提前创建/重建，故不出现在本初始化脚本中

        // MySQL 需为 DATABASECHANGELOG 表补充主键（运行时由 MySQLCustomChangeLogHistoryService 处理）
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        if (changeLogHistoryService instanceof MySQLCustomChangeLogHistoryService customChangeLogHistoryService) {
            loggingExecutor.comment("Add primary key to DATABASECHANGELOG table for MySQL");
            loggingExecutor.execute(customChangeLogHistoryService.getAddDatabaseChangeLogPKStatement());
        }

        executorService.setExecutor(LiquibaseConstants.JDBC_EXECUTOR, database, oldTemplate);
    }

    /** 类级同步校验数据库是否与全部 changelog 同步。 */
    @Override
    public Status validate(Connection connection, String defaultSchema) {
        synchronized (LiquibaseJpaUpdaterProvider.class) {
            return this.validateSynch(connection, defaultSchema);
        }
    }

    /** 依次校验主 changelog 与各扩展 Provider changelog，任一过期即返回 OUTDATED。 */
    protected Status validateSynch(final Connection connection, final String defaultSchema) {

        logger.debug("Validating if database is updated");
        ThreadLocalSessionContext.setCurrentSession(session);

        try {
            // 先校验 Keycloak 主 changelog
            KeycloakLiquibase liquibase = getLiquibaseForKeycloakUpdate(connection, defaultSchema);

            Status status = validateChangeSet(liquibase, liquibase.getChangeLogFile());
            if (status != Status.VALID) {
                return status;
            }

            // 再校验每个扩展 JpaEntityProvider 的 changelog
            Set<JpaEntityProvider> jpaProviders = session.getAllProviders(JpaEntityProvider.class);
            for (JpaEntityProvider jpaProvider : jpaProviders) {
                String customChangelog = jpaProvider.getChangelogLocation();
                if (customChangelog != null) {
                    String factoryId = jpaProvider.getFactoryId();
                    String changelogTableName = JpaUtils.getCustomChangelogTableName(factoryId);
                    liquibase = getLiquibaseForCustomProviderUpdate(connection, defaultSchema, customChangelog, jpaProvider.getClass().getClassLoader(), changelogTableName);
                    if (validateChangeSet(liquibase, liquibase.getChangeLogFile()) != Status.VALID) {
                        return Status.OUTDATED;
                    }
                }
            }
        } catch (LiquibaseException e) {
            throw new RuntimeException("Failed to validate database", e);
        } finally {
            ThreadLocalSessionContext.removeCurrentSession();
        }

        return Status.VALID;
    }

    /**
     * 校验单个 changelog：无待执行 changeset 为 VALID；全部未执行为 EMPTY；部分未执行为 OUTDATED。
     */
    protected Status validateChangeSet(KeycloakLiquibase liquibase, String changelog) throws LiquibaseException {
        final Status result;
        List<ChangeSet> changeSets = getLiquibaseUnrunChangeSets(liquibase);

        if (!changeSets.isEmpty()) {
            if (changeSets.size() == liquibase.getDatabaseChangeLog().getChangeSets().size()) {
                result = Status.EMPTY;
            } else {
                logger.debugf("Validation failed. Database is not up-to-date for changelog %s", changelog);
                result = Status.OUTDATED;
            }
        } else {
            logger.debugf("Validation passed. Database is up-to-date for changelog %s", changelog);
            result = Status.VALID;
        }

        // 重置 Liquibase 服务，避免校验多个 changelog 时状态串扰（KEYCLOAK-3769）
        resetLiquibaseServices(liquibase);

        return result;
    }

    /** 重置 Liquibase 内部服务并重新注册 MySQL 自定义 changelog 历史服务。 */
    private void resetLiquibaseServices(KeycloakLiquibase liquibase) {
        liquibase.resetServices();
        ChangeLogHistoryServiceFactory.getInstance().register(new MySQLCustomChangeLogHistoryService());
    }

    /** 列出尚未执行的 changeset（不含已过滤标签）。 */
    private List<ChangeSet> getLiquibaseUnrunChangeSets(Liquibase liquibase) throws LiquibaseException {
        return liquibase.listUnrunChangeSets(null, new LabelExpression(), false);
    }

    /** 构建用于 Keycloak 主 changelog 升级的 Liquibase 实例。 */
    private KeycloakLiquibase getLiquibaseForKeycloakUpdate(Connection connection, String defaultSchema) throws LiquibaseException {
        LiquibaseConnectionProvider liquibaseProvider = session.getProvider(LiquibaseConnectionProvider.class);
        return liquibaseProvider.getLiquibase(connection, defaultSchema);
    }

    /** 构建用于扩展 Provider 自定义 changelog 的 Liquibase 实例（独立 changelog 跟踪表）。 */
    private KeycloakLiquibase getLiquibaseForCustomProviderUpdate(Connection connection, String defaultSchema, String changelogLocation, ClassLoader classloader, String changelogTableName) throws LiquibaseException {
        LiquibaseConnectionProvider liquibaseProvider = session.getProvider(LiquibaseConnectionProvider.class);
        return liquibaseProvider.getLiquibaseForCustomUpdate(connection, defaultSchema, changelogLocation, classloader, changelogTableName);
    }

    @Override
    public void close() {
    }

    /** 将表名与默认 schema 拼接为限定名（schema 为 null 时仅返回表名）。 */
    public static String getTable(String table, String defaultSchema) {
        return defaultSchema != null ? defaultSchema + "." + table : table;
    }

}
