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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.util.Time;
import org.keycloak.common.util.reflections.Reflections;
import org.keycloak.connections.jpa.updater.liquibase.LiquibaseConstants;
import org.keycloak.models.dblock.DBLockProvider;

import liquibase.Scope;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.StandardLockService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.jboss.logging.Logger;

/**
 * Keycloak 定制的 Liquibase 锁服务：修复上游缺陷，超时以毫秒计，并支持多命名空间锁行。
 * <p>在 {@link StandardLockService} 基础上增强 H2 主键可见性检测、
 * 按 {@link DBLockProvider.Namespace} 初始化锁表，以及 {@code SELECT FOR UPDATE} 式加锁。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CustomLockService extends StandardLockService {

    private static final Logger log = Logger.getLogger(CustomLockService.class);

    /** H2 上表创建非原子，须确认主键已可见，否则误判锁表已就绪。 */
    @Override
    protected boolean isDatabaseChangeLogLockTableCreated() throws DatabaseException {
        boolean originalReturnValue = super.isDatabaseChangeLogLockTableCreated();
        if (originalReturnValue && database.getConnection().getDatabaseProductName().equals("H2")) {
            /* Liquibase 仅检查表是否存在。H2 中带主键的建表非原子，主键可能尚未可见；
               主键缺失会导致重复插入锁行，进而建主键失败并回滚整表创建。
               已有 DATABASECHANGELOG 可表明数据曾插入完成，但 DBLockTest 仅删 LOCK 表时无效。
               详见 https://github.com/keycloak/keycloak/issues/15487
             */
            Table lockTable = (Table) new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(
                    new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()));
            SnapshotGeneratorFactory instance = SnapshotGeneratorFactory.getInstance();

            try {
                DatabaseSnapshot snapshot = instance.createSnapshot(lockTable.getSchema().toCatalogAndSchema(), database,
                        new SnapshotControl(database, false, Table.class, PrimaryKey.class).setWarnIfObjectNotFound(false));
                Table lockTableFromSnapshot = snapshot.get(lockTable);
                if (lockTableFromSnapshot == null) {
                    throw new RuntimeException("DATABASECHANGELOGLOCK not found, although Liquibase claims it exists.");
                } else if (lockTableFromSnapshot.getPrimaryKey() == null) {
                    log.warn("Primary key not found - table creation not complete yet.");
                    return false;
                }
            } catch (InvalidExampleException e) {
                throw new RuntimeException(e);
            }
        }
        return originalReturnValue;
    }

    /** 创建锁表、为各 {@link DBLockProvider.Namespace} 插入锁行，并处理 Derby 旧版 boolean 列兼容。 */
    @Override
    public void init() throws DatabaseException {
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(LiquibaseConstants.JDBC_EXECUTOR, database);

        if (!isDatabaseChangeLogLockTableCreated()) {

            try {
                if (log.isTraceEnabled()) {
                    log.trace("Create Database Lock Table");
                }
                executor.execute(new CreateDatabaseChangeLogLockTableStatement());
                database.commit();
            } catch (DatabaseException de) {
                log.warn("Failed to create lock table. Maybe other transaction created in the meantime. Retrying...", de);
                if (log.isTraceEnabled()) {
                    log.trace(de.getMessage(), de); // 详细堆栈仅 TRACE 级别
                }
                database.rollback();
                throw new LockRetryException(de);
            }

            log.debugf("Created database lock table with name: %s", database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));

            try {
                Field field = Reflections.findDeclaredField(StandardLockService.class, "hasDatabaseChangeLogLockTable");
                Reflections.setAccessible(field);
                field.set(CustomLockService.this, true);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }

        try {
            Set<Integer> currentIds = currentIdsInDatabaseChangeLogLockTable();
            Set<Integer> customNamespaceIds = Arrays.stream(DBLockProvider.Namespace.values())
                    .map(DBLockProvider.Namespace::getId)
                    .collect(Collectors.toSet());
            if (!currentIds.containsAll(customNamespaceIds)) {
                if (log.isTraceEnabled()) {
                    log.tracef("Initialize Database Lock Table, current locks %s", currentIds);
                }
                executor.execute(new CustomInitializeDatabaseChangeLogLockTableStatement(currentIds));
                database.commit();

                log.debug("Initialized record in the database lock table");
            }

        } catch (DatabaseException de) {
            log.warn("Failed to insert first record to the lock table. Maybe other transaction inserted in the meantime. Retrying...", de);
            if (log.isTraceEnabled()) {
                log.trace(de.getMessage(), de); // 详细堆栈仅 TRACE 级别
            }
            database.rollback();
            throw new LockRetryException(de);
        }


        // Keycloak 不支持 Derby，但保留兼容逻辑以防万一
        if (executor.updatesDatabase() && database instanceof DerbyDatabase && ((DerbyDatabase) database).supportsBooleanDataType()) { // 检查锁表 LOCKED 列是否为旧版 smallint 而非 boolean
            String lockTable = database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
            Object obj = executor.queryForObject(new RawSqlStatement("select min(locked) as test from " + lockTable + " fetch first row only"), Object.class);
            if (!(obj instanceof Boolean)) { // 类型不符，需重建锁表
                executor.execute(new DropTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName(), false));
                executor.execute(new CreateDatabaseChangeLogLockTableStatement());
                executor.execute(new InitializeDatabaseChangeLogLockTableStatement());
            }
        }

    }

    /** 查询锁表中已存在的 ID 集合，用于判断哪些命名空间行尚待插入。 */
    private Set<Integer> currentIdsInDatabaseChangeLogLockTable() throws DatabaseException {
        try {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(LiquibaseConstants.JDBC_EXECUTOR, database);
            String idColumnName = database.escapeColumnName(database.getLiquibaseCatalogName(),
                    database.getLiquibaseSchemaName(),
                    database.getDatabaseChangeLogLockTableName(),
                    "ID");
            String lockTableName = database.escapeTableName(database.getLiquibaseCatalogName(),
                    database.getLiquibaseSchemaName(),
                    database.getDatabaseChangeLogLockTableName());
            SqlStatement sqlStatement = new RawSqlStatement("SELECT " + idColumnName + " FROM " + lockTableName);
            List<Map<String, ?>> rows = executor.queryForList(sqlStatement);
            Set<Integer> ids = rows.stream().map(columnMap -> ((Number) columnMap.get("ID")).intValue()).collect(Collectors.toSet());
            database.commit();
            return ids;
        } catch (UnexpectedLiquibaseException ulie) {
            // MariaDB Galera 10.1 可能因 DB 锁将 UnexpectedLiquibaseException 包装 DatabaseException，回滚后重试即可
            if (ulie.getCause() != null && ulie.getCause() instanceof DatabaseException) {
                throw (DatabaseException) ulie.getCause();
            } else {
                throw ulie;
            }
        }
    }

    /** 等待默认 Liquibase 锁（ID=1）。 */
    @Override
    public void waitForLock() {
        waitForLock(new LockDatabaseChangeLogStatement());
    }

    /** 等待指定命名空间对应的锁行。 */
    public void waitForLock(DBLockProvider.Namespace lock) {
        waitForLock(new CustomLockDatabaseChangeLogStatement(lock.getId()));
    }

    /** 在 {@link #getChangeLogLockWaitTime()} 超时内循环尝试 {@link #acquireLock}。 */
    private void waitForLock(LockDatabaseChangeLogStatement lockStmt) {
        boolean locked = false;
        long startTime = Time.toMillis(Time.currentTime());
        long timeToGiveUp = startTime + (getChangeLogLockWaitTime());
        boolean nextAttempt = true;

        while (nextAttempt) {
            locked = acquireLock(lockStmt);
            if (!locked) {
                int remainingTime = ((int)(timeToGiveUp / 1000)) - Time.currentTime();
                if (remainingTime > 0) {
                    log.debugf("Will try to acquire log another time. Remaining time: %d seconds", remainingTime);
                } else {
                    nextAttempt = false;
                }
            } else {
                nextAttempt = false;
            }
        }

        if (!locked) {
            int timeout = ((int)(getChangeLogLockWaitTime() / 1000));
            throw new IllegalStateException("Could not acquire change log lock within specified timeout " + timeout + " seconds.  Currently locked by other transaction");
        }
    }

    /** 尝试获取默认 Liquibase 锁。 */
    @Override
    public boolean acquireLock() {
        return acquireLock(new LockDatabaseChangeLogStatement());
    }

    /** 执行悲观锁 SQL；失败返回 false 供 {@link #waitForLock} 重试。 */
    private boolean acquireLock(LockDatabaseChangeLogStatement lockStmt) {
        if (hasChangeLogLock) {
            // 当前事务已持有锁
            return true;
        }

        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(LiquibaseConstants.JDBC_EXECUTOR, database);

        try {
            database.rollback();

            // 确保锁表已创建且锁行已插入
            this.init();
        } catch (DatabaseException de) {
            throw new IllegalStateException("Failed to retrieve lock", de);
        }

        try {
            log.debug("Trying to lock database");
            executor.execute(lockStmt);
            log.debug("Successfully acquired database lock");

            hasChangeLogLock = true;
            database.setCanCacheLiquibaseTableInfo(true);
            return true;

        } catch (DatabaseException de) {
            log.warn("Lock didn't yet acquired. Will possibly retry to acquire lock. Details: " + de.getMessage(), de);
            if (log.isTraceEnabled()) {
                log.debug(de.getMessage(), de);
            }
            return false;
        }
    }


    /** 提交事务以释放 {@code SELECT FOR UPDATE} 行锁，并重置内存状态。 */
    @Override
    public void releaseLock() {
        try {
            if (hasChangeLogLock) {
                log.debug("Going to release database lock");
                database.commit();
            } else {
                log.warn("Attempt to release lock, which is not owned by current transaction");
            }
        } catch (Exception e) {
            log.error("Database error during release lock", e);
        } finally {
            try {
                hasChangeLogLock = false;
                database.setCanCacheLiquibaseTableInfo(false);
                database.rollback();
            } catch (DatabaseException ignored) {
            }
        }
    }


}
