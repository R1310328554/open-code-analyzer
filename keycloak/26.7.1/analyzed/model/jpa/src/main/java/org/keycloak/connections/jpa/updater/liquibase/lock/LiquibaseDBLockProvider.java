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

import java.sql.Connection;
import java.sql.SQLException;

import org.keycloak.common.util.Retry;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.connections.jpa.JpaConnectionProviderFactory;
import org.keycloak.connections.jpa.updater.liquibase.conn.LiquibaseConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.dblock.DBLockProvider;
import org.keycloak.models.utils.KeycloakModelUtils;

import liquibase.Liquibase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import org.jboss.logging.Logger;

/**
 * 基于 Liquibase {@link CustomLockService} 的 JPA 数据库锁 {@link DBLockProvider} 实现。
 * <p>在独立 JDBC 连接上通过 {@code SELECT FOR UPDATE} 锁定 {@code DATABASECHANGELOGLOCK} 指定命名空间行，
 * 并在 JTA 事务外执行以避免与容器事务管理冲突。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LiquibaseDBLockProvider implements DBLockProvider {

    private static final Logger logger = Logger.getLogger(LiquibaseDBLockProvider.class);

    // 10 次重试通常足够应对并发建表/插行
    private final int DEFAULT_MAX_ATTEMPTS = 10;


    private final LiquibaseDBLockProviderFactory factory;
    private final KeycloakSession session;

    private CustomLockService lockService;
    private Connection dbConnection;
    private boolean initialized = false;
    private Namespace namespaceLocked = null;

    public LiquibaseDBLockProvider(LiquibaseDBLockProviderFactory factory, KeycloakSession session) {
        this.factory = factory;
        this.session = session;
    }


    /** 延迟初始化：获取 JDBC 连接并配置 {@link CustomLockService}。 */
    private void lazyInit() {
        if (!initialized) {
            LiquibaseConnectionProvider liquibaseProvider = session.getProvider(LiquibaseConnectionProvider.class);
            JpaConnectionProviderFactory jpaProviderFactory = (JpaConnectionProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(JpaConnectionProvider.class);

            this.dbConnection = jpaProviderFactory.getConnection();
            String defaultSchema = jpaProviderFactory.getSchema();

            try {
                Liquibase liquibase = liquibaseProvider.getLiquibase(dbConnection, defaultSchema);

                this.lockService = new CustomLockService();
                lockService.setChangeLogLockWaitTime(factory.getLockWaitTimeoutMillis());
                lockService.setDatabase(liquibase.getDatabase());
                initialized = true;
            } catch (LiquibaseException exception) {
                safeRollbackConnection();
                safeCloseConnection();
                throw new IllegalStateException(exception);
            }
        }
    }

    /** 事务已回滚后关闭旧连接并重新 lazyInit，供 {@link LockRetryException} 重试路径使用。 */
    private void restart() {
        safeCloseConnection();
        lazyInit();
    }

    /** 在 JTA 事务外等待指定命名空间的数据库锁，遇 {@link LockRetryException} 时回滚并重试。 */
    @Override
    public void waitForLock(Namespace lock) {
        KeycloakModelUtils.suspendJtaTransaction(session.getKeycloakSessionFactory(), () -> {

            lazyInit();

            if (this.lockService.hasChangeLogLock()) {
                if (lock.equals(this.namespaceLocked)) {
                    logger.warnf("Locking namespace %s which was already locked in this provider", lock);
                    return;
                } else {
                    throw new RuntimeException("Trying to get a lock when one was already taken by the provider");
                }
            }

            logger.debugf("Going to lock namespace=%s", lock);
            Retry.executeWithBackoff((int iteration) -> {

                lockService.waitForLock(lock);
                namespaceLocked = lock;

            }, (int iteration, Throwable e) -> {

                if (e instanceof LockRetryException && iteration < (DEFAULT_MAX_ATTEMPTS - 1)) {
                    // 可在新事务中再次尝试获取锁
                    safeRollbackConnection();
                    restart();
                } else {
                    safeRollbackConnection();
                    safeCloseConnection();
                }

            }, DEFAULT_MAX_ATTEMPTS, 10);
        });

    }

    /** 释放当前命名空间锁并重置 {@link CustomLockService} 状态。 */
    @Override
    public void releaseLock() {
        KeycloakModelUtils.suspendJtaTransaction(session.getKeycloakSessionFactory(), () -> {
            lazyInit();

            logger.debugf("Going to release database lock namespace=%s", namespaceLocked);
            namespaceLocked = null;
            lockService.releaseLock();
            lockService.reset();
        });
    }

    @Override
    public Namespace getCurrentLock() {
        return this.namespaceLocked;
    }

    /** {@code SELECT FOR UPDATE} 锁由其他事务持有，无法强制解锁。 */
    @Override
    public boolean supportsForcedUnlock() {
        // 基于 SELECT FOR UPDATE 的实现无法强制解除他事务持有的行锁
        return false;
    }

    /** 销毁 Liquibase 锁表（测试/维护场景）。 */
    @Override
    public void destroyLockInfo() {
        KeycloakModelUtils.suspendJtaTransaction(session.getKeycloakSessionFactory(), () -> {
            lazyInit();

            try {
                this.lockService.destroy();
                dbConnection.commit();
                logger.debug("Destroyed lock table");
            } catch (DatabaseException | SQLException de) {
                logger.error("Failed to destroy lock table");
                safeRollbackConnection();
            }
        });
    }

    @Override
    public void close() {
        KeycloakModelUtils.suspendJtaTransaction(session.getKeycloakSessionFactory(), this::safeCloseConnection);
    }

    private void safeRollbackConnection() {
        if (dbConnection != null) {
            try {
                this.dbConnection.rollback();
            } catch (SQLException se) {
                logger.warn("Failed to rollback connection after error", se);
            }
        }
    }

    /** 关闭 JDBC 连接；内存库需显式关闭以防进程退出时自动关库。 */
    private void safeCloseConnection() {
        // 关闭连接，防止内存数据库在连接泄漏时过早 shutdown
        if (dbConnection != null) {
            try {
                dbConnection.close();
                dbConnection = null;
                lockService = null;
                initialized = false;
            } catch (SQLException e) {
                logger.warn("Failed to close connection", e);
            }
        }
    }
}
