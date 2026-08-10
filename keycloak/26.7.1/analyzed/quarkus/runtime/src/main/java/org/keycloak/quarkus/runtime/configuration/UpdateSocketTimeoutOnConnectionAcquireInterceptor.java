/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.config.TransactionOptions;
import org.keycloak.models.utils.KeycloakModelUtils;

import io.agroal.api.AgroalPoolInterceptor;
import io.agroal.pool.wrapper.ConnectionWrapper;
import io.quarkus.runtime.configuration.DurationConverter;
import org.jspecify.annotations.NonNull;
import org.postgresql.PGConnection;

/**
 * When acquiring a database connection, set the socket timeout to the same value as the transaction timeout.
 * This will shorten the time between the transaction manager aborting the transaction but not cancelling the statement,
 * and finally freeing the connection and the Java thread as no SQL statement will run longer than the socket timeout.
 */
 * 连接池获取连接时将网络/查询超时对齐事务超时，避免事务中止后 SQL 仍长时间占用连接与线程。

@ApplicationScoped
public class UpdateSocketTimeoutOnConnectionAcquireInterceptor implements AgroalPoolInterceptor {

    // 关闭阶段不关闭 Executor，以免干扰数据库连接收尾
    private final Executor executor;
    private final Integer transactionDefaultTimeout;
    private Class<?> pgClass;

    public UpdateSocketTimeoutOnConnectionAcquireInterceptor() {
        this.executor = Executors
                .newSingleThreadExecutor(new InternalThreadFactory());
        this.transactionDefaultTimeout =
                Long.valueOf(DurationConverter.parseDuration(
                        Configuration.getConfigValue(TransactionOptions.TRANSACTION_DEFAULT_TIMEOUT).getValue()
                ).toMillis()).intValue();
        try {
            pgClass = Class.forName("org.postgresql.PGConnection");
        } catch (ClassNotFoundException ex) {
            pgClass = null;
        }
    }

    @Override
    public void onConnectionAcquire(Connection connection) {
        try {
            Optional<Integer> timeout = KeycloakModelUtils.getTransactionLimit();
            int timeoutMillis = timeout.map(integer -> integer * 1000).orElse(transactionDefaultTimeout);
            if (pgClass != null && connection instanceof ConnectionWrapper wrapper && wrapper.isWrapperFor(pgClass)) {
                // PostgreSQL 优先设置 queryTimeout，比纯读超时更优雅地终止语句
                wrapper.unwrap(PGConnection.class).setQueryTimeout((int) TimeUnit.MILLISECONDS.toSeconds(timeoutMillis));
                timeoutMillis += 1000;
            }
            connection.setNetworkTimeout(executor, timeoutMillis);
        } catch (SQLException e) {
            throw new IllegalStateException("Can't set timeouts for connection", e);
        }
    }

    private static class InternalThreadFactory implements ThreadFactory {

        /** JDBC 网络超时回调使用的守护线程名。 */
        private static final String THREAD_NAME = "jdbc-network-timeout";

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName(THREAD_NAME);
            thread.setDaemon(true);
            return thread;
        }
    }
}
