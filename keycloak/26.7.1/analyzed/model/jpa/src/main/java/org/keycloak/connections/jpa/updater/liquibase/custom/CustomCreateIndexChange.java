/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.connections.jpa.updater.liquibase.custom;

import java.io.StringWriter;

import org.keycloak.connections.jpa.updater.liquibase.LiquibaseConstants;
import org.keycloak.connections.jpa.updater.liquibase.LiquibaseJpaUpdaterProvider;
import org.keycloak.connections.jpa.updater.liquibase.conn.DefaultLiquibaseConnectionProvider;

import liquibase.Scope;
import liquibase.change.AddColumnConfig;
import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeParameterMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.jboss.logging.Logger;

/**
 * 按表行数阈值条件创建索引的 Liquibase Change。
 * <p>大表在线建索引可能长时间锁表；超过 {@link DefaultLiquibaseConnectionProvider#INDEX_CREATION_THRESHOLD_PARAM} 时跳过执行并记录 WARN，供 DBA 手动创建。</p>
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
@DatabaseChange(name = "createIndex", description = "Creates an index on an existing column or set of columns conditionally based on the number of records.", priority = ChangeMetaData.PRIORITY_DEFAULT
    + 1, appliesTo = "index")
public class CustomCreateIndexChange extends CreateIndexChange {
    private static final Logger logger = Logger.getLogger(CustomCreateIndexChange.class);
    /** 来自 Database 配置的索引创建行数阈值。 */
    private long indexCreationThreshold;
    /** 缓存的目标表行数估计值。 */
    private Long entriesInTable = null;
    /** 是否已输出跳过建索引的 WARN 日志。 */
    private boolean logged;

    @Override
    public SqlStatement[] generateStatements(Database database) {
        // 手动迁移模式（LoggingExecutor）直接生成标准建索引语句
        if (getExecutor(database) instanceof LoggingExecutor)
            return super.generateStatements(database);

        Object indexCreationThreshold = ((AbstractJdbcDatabase) database)
            .get(DefaultLiquibaseConnectionProvider.INDEX_CREATION_THRESHOLD_PARAM);

        if (indexCreationThreshold instanceof Long) {
            this.indexCreationThreshold = (Long) indexCreationThreshold;
            if (this.indexCreationThreshold <= 0)
                return super.generateStatements(database);
        } else {
            return super.generateStatements(database);
        }
        try {
            // 确认目标表已存在
            if (getTableName() == null || !SnapshotGeneratorFactory.getInstance()
                .has(new Table().setName(getTableName()).setSchema(new Schema(getCatalogName(), getSchemaName())), database)) {
                return super.generateStatements(database);
            }

            Long entriesInTable = computeEntriesInTable(database);

            // 超过阈值：跳过在线建索引，将 DDL 写入 changelog 注释供手动执行
            if (entriesInTable > this.indexCreationThreshold) {
                String loggingString = createLoggingString(database);
                if (!logged) {
                    logger.warnv("Following index should be created: {0}", loggingString);
                    logged = true;
                }
                getChangeSet().setComments(loggingString);
                return new SqlStatement[] {};
            }

        } catch (DatabaseException | InvalidExampleException e) {
            throw new UnexpectedLiquibaseException("Database error while index threshold validation.", e);
        }

        return super.generateStatements(database);
    }

    /** 供测试或外部调用：始终生成标准建索引语句（忽略阈值）。 */
    public SqlStatement[] generateOriginalStatement(Database database) {
        return super.generateStatements(database);
    }

    /** 估算目标表行数；PostgreSQL 优先用 pg_class 统计，否则 COUNT 或带上限 COUNT。 */
    private Long computeEntriesInTable(Database database) throws DatabaseException {
        if (entriesInTable != null) {
            return entriesInTable;
        }

        if (database instanceof PostgresDatabase) {
            try {
                // 用 pg_class.reltuples 估计行数，避免全表 COUNT 锁行
                entriesInTable = getExecutor(database)
                        .queryForLong(new RawParameterizedSqlStatement("SELECT reltuples::bigint AS estimate FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = current_schema AND UPPER(c.relname) = UPPER(?)", getTableName()));
                // 统计信息有效时直接返回
                if (entriesInTable > 0) {
                    return entriesInTable;
                }
            } catch (UnexpectedLiquibaseException e) {
                logger.warn("No permissions to run SELECT on the pg_class and pg_namespace tables, therefore can't estimate row count. Falling back to slower method to count entries with SELECT COUNT(*).", e);
            }
            // 带上限的 COUNT，仅建立行数下界而非精确值
            entriesInTable = getExecutor(database)
                    .queryForLong(new RawParameterizedSqlStatement(String.format("SELECT COUNT(*) FROM (SELECT 1 FROM %s LIMIT ?) t", getTableNameForSqlSelects(database, getTableName())), this.indexCreationThreshold + 1));
            return entriesInTable;
        }

        entriesInTable = getExecutor(database)
                    .queryForLong(new RawParameterizedSqlStatement(String.format("SELECT COUNT(*) FROM %s", getTableNameForSqlSelects(database, getTableName()))));
        return entriesInTable;
    }

    private static Executor getExecutor(Database database) {
        return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(LiquibaseConstants.JDBC_EXECUTOR, database);
    }

    private String getTableNameForSqlSelects(Database database, String tableName) {
        String correctedSchemaName = database.escapeObjectName(database.getDefaultSchemaName(), Schema.class);
        return LiquibaseJpaUpdaterProvider.getTable(tableName, correctedSchemaName);
    }

    /** 将 CREATE INDEX 语句格式化为可记录的字符串。 */
    private String createLoggingString(Database database) throws DatabaseException {
        StringWriter writer = new StringWriter();
        LoggingExecutor loggingExecutor = new LoggingExecutor(getExecutor(database), writer, database);
        SqlStatement sqlStatement = new CreateIndexStatement(getIndexName(), getCatalogName(), getSchemaName(), getTableName(),
            this.isUnique(), getAssociatedWith(), getColumns().toArray(new AddColumnConfig[0]))
                .setTablespace(getTablespace()).setClustered(getClustered());

        loggingExecutor.execute(sqlStatement);

        return writer.toString();
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        SqlStatement[] statements = super.generateStatements(database);
        if (statements == null) {
            return false;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(statement, database)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Warnings warn(Database database) {
        Warnings warnings = new Warnings();
        if (generateStatementsVolatile(database)) {
            return warnings;
        }

        SqlStatement[] statements = super.generateStatements(database);
        if (statements == null) {
            return warnings;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().supports(statement, database)) {
                warnings.addAll(SqlGeneratorFactory.getInstance().warn(statement, database));
            } else if (statement.skipOnUnsupported()) {
                warnings.addWarning(statement.getClass().getName() + " is not supported on " + database.getShortName() + ", but "
                        + Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName() + " will still execute");
            }
        }

        return warnings;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors changeValidationErrors = new ValidationErrors();

        ChangeFactory changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
        for (ChangeParameterMetaData param : changeFactory.getChangeMetaData(this).getParameters().values()) {
            if (param.isRequiredFor(database) && param.getCurrentValue(this) == null) {
                changeValidationErrors.addError(param.getParameterName() + " is required for "
                    + changeFactory.getChangeMetaData(this).getName() + " on " + database.getShortName());
            }
        }
        if (changeValidationErrors.hasErrors()) {
            return changeValidationErrors;
        }

        if (!generateStatementsVolatile(database)) {
            String unsupportedWarning = changeFactory.getChangeMetaData(this).getName() + " is not supported on "
                + database.getShortName();
            boolean sawUnsupportedError = false;

            SqlStatement[] statements = super.generateStatements(database);
            if (statements != null) {
                for (SqlStatement statement : statements) {
                    boolean supported = SqlGeneratorFactory.getInstance().supports(statement, database);
                    if (!supported && !sawUnsupportedError) {
                        if (!statement.skipOnUnsupported()) {
                            changeValidationErrors.addError(unsupportedWarning);
                            sawUnsupportedError = true;
                        }
                    } else {
                        changeValidationErrors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
                    }
                }
            }
        }

        return changeValidationErrors;
    }

    // 显式声明 supports 以消除 "class does not implement the 'supports(Database)' method" 警告
    @Override
    public boolean supports(Database database) {
        return super.supports(database);
    }
}
