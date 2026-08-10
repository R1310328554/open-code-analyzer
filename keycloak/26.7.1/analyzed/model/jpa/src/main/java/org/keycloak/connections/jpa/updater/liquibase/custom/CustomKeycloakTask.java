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

package org.keycloak.connections.jpa.updater.liquibase.custom;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.connections.jpa.updater.liquibase.LiquibaseJpaUpdaterProvider;
import org.keycloak.connections.jpa.updater.liquibase.ThreadLocalSessionContext;
import org.keycloak.models.KeycloakSession;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

/**
 * Keycloak 自定义 Liquibase 数据迁移任务基类。
 * <p>在 Liquibase 事务中生成 {@link SqlStatement} 列表，并通过线程绑定的 {@link KeycloakSession} 访问运行时上下文。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class CustomKeycloakTask implements CustomSqlChange {

    /** 当前线程绑定的 Keycloak 会话。 */
    protected KeycloakSession kcSession;

    protected Database database;
    protected JdbcConnection jdbcConnection;
    protected Connection connection;
    /** 迁移完成后写入 changelog 的确认消息。 */
    protected StringBuilder confirmationMessage = new StringBuilder();
    /** 待 Liquibase 批量执行的 SQL 语句。 */
    protected List<SqlStatement> statements = new ArrayList<SqlStatement>();

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public String getConfirmationMessage() {
        return confirmationMessage.toString();
    }

    /** 从 ThreadLocal 获取 KeycloakSession，缺失则 setup 失败。 */
    @Override
    public void setUp() throws SetupException {
        this.kcSession = ThreadLocalSessionContext.getCurrentSession();
        if (this.kcSession == null) {
            throw new SetupException("Thread bound session is null");
        }
    }

    /** 若 REALM 表存在且有数据则执行子类迁移逻辑，否则跳过。 */
    @Override
    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
        this.database = database;
        jdbcConnection = (JdbcConnection) database.getConnection();
        connection = jdbcConnection.getWrappedConnection();

        if (isApplicable()) {
            confirmationMessage.append(getTaskId() + ": ");
            generateStatementsImpl();
        } else {
            confirmationMessage.append(getTaskId() + ": no update applicable for this task");
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    /**
     * 判断迁移是否适用：REALM 表存在且至少有一行。
     * <p>部分 RDBMS 不允许在同一事务内先 SELECT 再 UPDATE 同表，故用 Savepoint 回滚探测查询。</p>
     */
    protected boolean isApplicable() throws CustomChangeException {
        try {
            String correctedTableName = database.correctObjectName("REALM", Table.class);
            if (SnapshotGeneratorFactory.getInstance().has(new Table().setName(correctedTableName), database)) {
                // Liquibase 管理的事务内：Savepoint 回滚使探测 SELECT 不影响后续 UPDATE
                Savepoint savepoint = connection.setSavepoint();
                try (Statement st = connection.createStatement(); ResultSet resultSet = st.executeQuery("SELECT ID FROM " + getTableName(correctedTableName))) {
                    return (resultSet.next());
                } finally {
                    connection.rollback(savepoint);
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new CustomChangeException("Failed to check database availability", e);
        }
    }

    /**
     * 子类实现：向 {@link #statements} 填充 SQL，并追加 {@link #confirmationMessage}。
     */
    protected abstract void generateStatementsImpl() throws CustomChangeException;

    /** 迁移任务标识，用于日志与确认消息。 */
    protected abstract String getTaskId();

    /** 返回带 schema 限定的表名，供原生 SQL 使用。 */
    protected String getTableName(String tableName) {
        String correctedSchemaName = database.escapeObjectName(database.getDefaultSchemaName(), Schema.class);
        return LiquibaseJpaUpdaterProvider.getTable(tableName, correctedSchemaName);
    }
}
