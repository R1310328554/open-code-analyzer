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

package org.keycloak.connections.jpa.updater.liquibase.lock;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import org.jboss.logging.Logger;

/**
 * 自定义 Liquibase 数据库变更日志锁 SQL 生成器，采用 {@code SELECT FOR UPDATE} 悲观锁
 * （与 Hibernate {@code LockMode.PESSIMISTIC_WRITE} 同类语义）。
 * <p>按数据库方言生成对应的行级排他锁语句，替代 Liquibase 默认的 UPDATE 式加锁。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CustomLockDatabaseChangeLogGenerator extends LockDatabaseChangeLogGenerator {

    private static final Logger logger = Logger.getLogger(CustomLockDatabaseChangeLogGenerator.class);

    /** 优先级略高于默认 {@link LockDatabaseChangeLogGenerator}，确保本生成器优先生效。 */
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // 确保优先级高于 LockDatabaseChangeLogGenerator
    }

    /** 根据锁语句与数据库类型生成悲观锁 SQL（仅 SELECT FOR UPDATE 一类，不做 UPDATE）。 */
    @Override
    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        Sql selectForUpdateSql = generateSelectForUpdate(database,
                (statement instanceof CustomLockDatabaseChangeLogStatement)?
                        ((CustomLockDatabaseChangeLogStatement) statement).getId() : 1);

        return new Sql[] { selectForUpdateSql };
    }


    /** 按 {@code id} 锁定 {@code DATABASECHANGELOGLOCK} 表对应行，各数据库方言语法不同。 */
    private Sql generateSelectForUpdate(Database database, int id) {
        String catalog = database.getLiquibaseCatalogName();
        String schema = database.getLiquibaseSchemaName();
        String rawLockTableName = database.getDatabaseChangeLogLockTableName();

        String lockTableName = database.escapeTableName(catalog, schema, rawLockTableName);
        String idColumnName  = database.escapeColumnName(catalog, schema, rawLockTableName, "ID");

        String sqlBase = "SELECT " + idColumnName + " FROM " + lockTableName;
        String sqlWhere = " WHERE " + idColumnName + "=" + id;

        String sql;
        if (database instanceof MySQLDatabase || database instanceof PostgresDatabase || database instanceof H2Database ||
                database instanceof OracleDatabase) {
            sql = sqlBase + sqlWhere + " FOR UPDATE";
        } else if (database instanceof MSSQLDatabase) {
            sql = sqlBase + " WITH (UPDLOCK, ROWLOCK)" + sqlWhere;
        } else if (database instanceof DB2Database) {
            sql = sqlBase + sqlWhere +  " FOR READ ONLY WITH RS USE AND KEEP UPDATE LOCKS";
        } else {
            sql = sqlBase + sqlWhere;
            logger.warnf("No direct support for database %s . Database lock may not work correctly", database.getClass().getName());
        }

        logger.debugf("SQL command for pessimistic lock: %s", sql);
        
        return new UnparsedSql(sql);
    }

}
