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

package org.keycloak.quarkus.runtime.storage.database.liquibase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.updater.JpaUpdaterProvider;
import org.keycloak.connections.jpa.updater.liquibase.LiquibaseConstants;
import org.keycloak.connections.jpa.updater.liquibase.ThreadLocalSessionContext;
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
 * Quarkus 环境下的 JPA/Liquibase 数据库更新器：执行主 changelog 与自定义 {@link JpaEntityProvider} 变更集。
 */
public class QuarkusJpaUpdaterProvider implements JpaUpdaterProvider {

    private static final Logger logger = Logger.getLogger(QuarkusJpaUpdaterProvider.class);

    /** Keycloak 主 changelog  classpath 路径。 */
    public static final String CHANGELOG = "META-INF/jpa-changelog-master.xml";
    private static final String DEPLOYMENT_ID_COLUMN = "DEPLOYMENT_ID";
    /** Session 属性：是否校验并执行主 changelog（由连接工厂在启动时设置）。 */
    public static final String VERIFY_AND_RUN_MASTER_CHANGELOG = "VERIFY_AND_RUN_MASTER_CHANGELOG";

    private final KeycloakSession session;
    /** 已解析的未执行变更集缓存（按 changelog 文件路径）。 */
    private Map<String, List<ChangeSet>> changeSets = new HashMap<>();

    public QuarkusJpaUpdaterProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void update(Connection connection, String defaultSchema) {
        update(connection, null, defaultSchema);
    }

    @Override
    public void export(Connection connection, String defaultSchema, File file) {
        update(connection, file, defaultSchema);
    }

    private void update(Connection connection, File file, String defaultSchema) {
        logger.debug("Starting database update");

        // Liquibase 无注入自定义对象的 API，通过 ThreadLocal 传递 KeycloakSession
        ThreadLocalSessionContext.setCurrentSession(session);

        Writer exportWriter = null;
        try {
            if (file != null) {
                exportWriter = new FileWriter(file);
            }

            if (needVerifyMasterChangelog()) {
                // 先执行 Keycloak 主 changelog
                KeycloakLiquibase liquibase = getLiquibaseForKeycloakUpdate(connection, defaultSchema);
                updateChangeSet(liquibase, exportWriter);
            }

            // 再依次处理各自定义 JpaEntityProvider 的 changelog
            Set<JpaEntityProvider> jpaProviders = session.getAllProviders(JpaEntityProvider.class);
            for (JpaEntityProvider jpaProvider : jpaProviders) {
                String customChangelog = jpaProvider.getChangelogLocation();
                if (customChangelog != null) {
                    String factoryId = jpaProvider.getFactoryId();
                    String changelogTableName = JpaUtils.getCustomChangelogTableName(factoryId);
                    KeycloakLiquibase liquibase = getLiquibaseForCustomProviderUpdate(connection, defaultSchema, customChangelog, jpaProvider.getClass().getClassLoader(), changelogTableName);
                    updateChangeSet(liquibase, exportWriter);
                }
            }
        } catch (LiquibaseException | IOException e) {
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

    private Boolean needVerifyMasterChangelog() {
        return session.getAttributeOrDefault(VERIFY_AND_RUN_MASTER_CHANGELOG, Boolean.TRUE);
    }

    protected void updateChangeSet(KeycloakLiquibase liquibase, Writer exportWriter) throws LiquibaseException  {
        String changelog = liquibase.getChangeLogFile();
        Database database = liquibase.getDatabase();
        Table changelogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl(database, false, Table.class, Column.class), database);

        if (changelogTable != null) {
            boolean hasDeploymentIdColumn = changelogTable.getColumn(DEPLOYMENT_ID_COLUMN) != null;

            // 若 DATABASECHANGELOG 表缺少 DEPLOYMENT_ID 列则补建（兼容旧版 Liquibase 表结构）
            if (!hasDeploymentIdColumn) {
                ChangeLogHistoryService changelogHistoryService = getChangeLogHistoryService().getChangeLogService(database);
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

        // 重置 Liquibase 单例服务，避免 ChangeLogHistoryServiceFactory 状态泄漏
        // 参见 KEYCLOAK-3769 讨论
        resetLiquibaseServices(liquibase);
    }

    private void outputChangeLogTableCreationScript(Liquibase liquibase, final Writer exportWriter) throws DatabaseException {
        Database database = liquibase.getDatabase();

        ExecutorService executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);
        Executor oldTemplate = executorService.getExecutor(LiquibaseConstants.JDBC_EXECUTOR, database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(executorService.getExecutor(LiquibaseConstants.JDBC_EXECUTOR, database), exportWriter, database);
        executorService.setExecutor(LiquibaseConstants.JDBC_EXECUTOR, database, loggingExecutor);

        loggingExecutor.comment("*********************************************************************");
        loggingExecutor.comment("* Keycloak database creation script - apply this script to empty DB *");
        loggingExecutor.comment("*********************************************************************" + StreamUtil.getLineSeparator());

        // DATABASECHANGELOG 表由 Liquibase 自动写入脚本
        // DATABASECHANGELOGLOCK 在 CustomLockService.init() 中已处理，不在此脚本中重复

        // MySQL 需为 DATABASECHANGELOG 表添加主键（运行时由 MySQLCustomChangeLogHistoryService 处理）
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        if (changeLogHistoryService instanceof MySQLCustomChangeLogHistoryService customChangeLogHistoryService) {
            loggingExecutor.comment("Add primary key to DATABASECHANGELOG table for MySQL");
            loggingExecutor.execute(customChangeLogHistoryService.getAddDatabaseChangeLogPKStatement());
        }

        executorService.setExecutor(LiquibaseConstants.JDBC_EXECUTOR, database, oldTemplate);
    }

    @Override
    public Status validate(Connection connection, String defaultSchema) {
        logger.debug("Validating if database is updated");
        ThreadLocalSessionContext.setCurrentSession(session);

        try {
            if (needVerifyMasterChangelog()) {
                // 先校验主 changelog
                KeycloakLiquibase liquibase = getLiquibaseForKeycloakUpdate(connection, defaultSchema);

                Status status = validateChangeSet(liquibase, liquibase.getChangeLogFile());
                if (status != Status.VALID) {
                    return status;
                }
            }

            // 再校验各自定义 JpaEntityProvider 的 changelog
            Set<JpaEntityProvider> jpaProviders = session.getAllProviders(JpaEntityProvider.class);
            for (JpaEntityProvider jpaProvider : jpaProviders) {
                String customChangelog = jpaProvider.getChangelogLocation();
                if (customChangelog != null) {
                    String factoryId = jpaProvider.getFactoryId();
                    String changelogTableName = JpaUtils.getCustomChangelogTableName(factoryId);
                    KeycloakLiquibase liquibase = getLiquibaseForCustomProviderUpdate(connection, defaultSchema, customChangelog, jpaProvider.getClass().getClassLoader(), changelogTableName);
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

        // 重置 Liquibase 单例服务，避免 ChangeLogHistoryServiceFactory 状态泄漏
        // 参见 KEYCLOAK-3769 讨论
        resetLiquibaseServices(liquibase);

        return result;
    }

    private void resetLiquibaseServices(KeycloakLiquibase liquibase) {
        liquibase.resetServices();
        getChangeLogHistoryService().register(new MySQLCustomChangeLogHistoryService());
    }

    private ChangeLogHistoryServiceFactory getChangeLogHistoryService() {
        return Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class);
    }

    private List<ChangeSet> getLiquibaseUnrunChangeSets(Liquibase liquibase) {
        // 若已缓存则复用，避免重复 listUnrunChangeSets
        return changeSets.computeIfAbsent(liquibase.getChangeLogFile(), s -> {
            try {
                return liquibase.listUnrunChangeSets(null, new LabelExpression(), false);
            } catch (LiquibaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private KeycloakLiquibase getLiquibaseForKeycloakUpdate(Connection connection, String defaultSchema) throws LiquibaseException {
        LiquibaseConnectionProvider liquibaseProvider = session.getProvider(LiquibaseConnectionProvider.class);
        return liquibaseProvider.getLiquibase(connection, defaultSchema);
    }

    private KeycloakLiquibase getLiquibaseForCustomProviderUpdate(Connection connection, String defaultSchema, String changelogLocation, ClassLoader classloader, String changelogTableName) throws LiquibaseException {
        LiquibaseConnectionProvider liquibaseProvider = session.getProvider(LiquibaseConnectionProvider.class);
        return liquibaseProvider.getLiquibaseForCustomUpdate(connection, defaultSchema, changelogLocation, classloader, changelogTableName);
    }

    @Override
    public void close() {
        changeSets.clear();
        changeSets = null;
    }

}
