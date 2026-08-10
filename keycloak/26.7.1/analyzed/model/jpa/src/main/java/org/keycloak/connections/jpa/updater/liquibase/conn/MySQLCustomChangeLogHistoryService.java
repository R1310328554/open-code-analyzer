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
package org.keycloak.connections.jpa.updater.liquibase.conn;

import org.keycloak.connections.jpa.updater.liquibase.LiquibaseConstants;

import liquibase.Scope;
import liquibase.change.ColumnConfig;
import liquibase.changelog.StandardChangeLogHistoryService;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.executor.jvm.ChangelogJdbcMdcListener;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

/**
 * MySQL 专用的变更历史服务：为 DATABASECHANGELOG 表补建复合主键。
 * <p>解决 MySQL Group Replication 要求每张表必须有主键的限制。</p>
 *
 * @author hmlnarik
 */
public class MySQLCustomChangeLogHistoryService extends StandardChangeLogHistoryService {

    /** 防止 init 重复执行 PK 补建逻辑。 */
    private boolean serviceInitialized;

    @Override
    public boolean supports(Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public void init() throws DatabaseException {
        super.init();

        if (serviceInitialized) return;

        serviceInitialized = true;


        // 手动迁移模式跳过执行——PK 语句由 LiquibaseJpaUpdaterProvider 写入导出脚本
        ExecutorService executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);
        if (executorService.getExecutor(LiquibaseConstants.JDBC_EXECUTOR, getDatabase()) instanceof LoggingExecutor) {
            return;
        }

        if (!existsDatabaseChangelogPK()) {
            createDatabaseChangelogPK();
        }
    }

    /** 优先级高于标准实现，确保 MySQL 走本服务。 */
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // 确保优先于 StandardChangeLogHistoryService
    }

    /** 检查 DATABASECHANGELOG 表是否已有主键。 */
    private boolean existsDatabaseChangelogPK() throws DatabaseException {
        try {
            PrimaryKey example = new PrimaryKey();
            Table table = new Table();
            table.setSchema(new Schema(getLiquibaseCatalogName(), getLiquibaseSchemaName()));
            table.setName(getDatabaseChangeLogTableName());
            example.setTable(table);
            return SnapshotGeneratorFactory.getInstance().has(example, getDatabase());
        } catch (InvalidExampleException e) {
            throw new DatabaseException(e);
        }
    }

    /** 为 DATABASECHANGELOG 添加 (ID, AUTHOR, FILENAME) 复合主键；若已存在则忽略异常。 */
    private void createDatabaseChangelogPK() throws DatabaseException {
        AddPrimaryKeyStatement pkStatement = getAddDatabaseChangeLogPKStatement();
        try {
            ChangelogJdbcMdcListener.execute(getDatabase(), ex -> ex.execute(pkStatement));
            getDatabase().commit();
        } catch (DatabaseException e) {
            // 主键已存在时忽略异常
            if (!existsDatabaseChangelogPK()) {
                throw e;
            }
        }
    }

    /** 构造 DATABASECHANGELOG 复合主键的 DDL 语句。 */
    public AddPrimaryKeyStatement getAddDatabaseChangeLogPKStatement() {
        return new AddPrimaryKeyStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(),
                ColumnConfig.arrayFromNames("ID, AUTHOR, FILENAME"), "PK_DATABASECHANGELOG");
    }
}
