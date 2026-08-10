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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.models.dblock.DBLockProvider;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.InsertStatement;

/**
 * 自定义 Liquibase 锁表初始化 SQL 生成器：仅 INSERT 缺失锁行，不附带默认 DELETE。
 * <p>Liquibase 默认实现会在插入前 DELETE 全表，导致 Keycloak 多命名空间锁行为异常；本生成器按 {@link DBLockProvider.Namespace} 补缺。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CustomInsertLockRecordGenerator extends AbstractSqlGenerator<InitializeDatabaseChangeLogLockTableStatement> {

    /** 优先级高于默认 InitializeDatabaseChangeLogLockTableGenerator，确保本实现生效。 */
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // Ensure bigger priority than InitializeDatabaseChangeLogLockTableGenerator
    }

    @Override
    public ValidationErrors validate(InitializeDatabaseChangeLogLockTableStatement initializeDatabaseChangeLogLockTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    /** 为尚未存在的 DBLockProvider 命名空间 ID 生成 INSERT（LOCKED=false）。 */
    @Override
    public Sql[] generateSql(InitializeDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // get the IDs that are already in the database if migration
        Set<Integer> currentIds = new HashSet<>();
        if (statement instanceof CustomInitializeDatabaseChangeLogLockTableStatement) {
            currentIds = ((CustomInitializeDatabaseChangeLogLockTableStatement) statement).getCurrentIds();
        }

        // generate all the IDs that are currently missing in the lock table
        List<Sql> result = new ArrayList<>();
        for (DBLockProvider.Namespace lock : DBLockProvider.Namespace.values()) {
            if (!currentIds.contains(lock.getId())) {
                InsertStatement insertStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                        .addColumnValue("ID", lock.getId())
                        .addColumnValue("LOCKED", Boolean.FALSE);
                result.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(insertStatement, database)));
            }
        }

        return result.toArray(new Sql[result.size()]);
    }
}
