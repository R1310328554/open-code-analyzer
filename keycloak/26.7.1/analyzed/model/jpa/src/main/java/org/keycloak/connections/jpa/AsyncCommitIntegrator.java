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

package org.keycloak.connections.jpa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.jboss.logging.Logger;

/**
 * Hibernate 事件监听器：在 PostgreSQL 上为仅修改 {@link AsynchronousCommitAllowed} 实体的
 * 事务启用异步提交（{@code SET LOCAL synchronous_commit TO OFF}），跳过 WAL fsync 等待以提升吞吐。
 * <p>
 * 数据库仍保持崩溃一致性；此类事务在崩溃时可能丢失最后几毫秒的数据。非 PostgreSQL 数据库为 no-op。
 * <p>
 * On PostgreSQL, issues {@code SET LOCAL synchronous_commit TO OFF} before commit when
 * all modified entities in the transaction allow it. This skips the WAL fsync wait,
 * improving throughput for ephemeral data. The database remains crash-consistent;
 * only the last few milliseconds of such transactions may be lost on a crash.
 * <p>
 * On non-PostgreSQL databases, {@link #registerListeners(EntityManagerFactory)} is a no-op.
 *
 * @author Alexander Schwartz
 */
public class AsyncCommitIntegrator implements PreInsertEventListener, PreUpdateEventListener, PreDeleteEventListener {

    private static final Logger logger = Logger.getLogger(AsyncCommitIntegrator.class);

    /** 会话属性：本事务需要同步提交（不可异步）。 */
    private static final String SYNC_REQUIRED = "kc.sync_commit_required";
    /** 会话属性：已注册事务完成回调。 */
    private static final String CALLBACK_REGISTERED = "kc.async_commit.registered";

    /**
     * 若底层数据库为 PostgreSQL，则在给定 {@link EntityManagerFactory} 上注册异步提交监听器；其他数据库无操作。
     * Registers asynchronous commit listeners on the given {@link EntityManagerFactory}
     * if the underlying database is PostgreSQL. No-op for other databases.
     */
    public static void registerListeners(EntityManagerFactory emf) {
        SessionFactoryImplementor sf = emf.unwrap(SessionFactoryImplementor.class);
        if (!(sf.getJdbcServices().getDialect() instanceof PostgreSQLDialect)) {
            return;
        }

        if (isAuroraWithLogicalReplication(sf)) {
            logger.warn("Asynchronous commit optimization disabled: Aurora PostgreSQL with logical replication " +
                    "detected. Aurora may not deliver async-committed transactions to logical decoding consumers.");
            return;
        }

        AsyncCommitIntegrator listener = new AsyncCommitIntegrator();
        var registry = sf.getEventEngine().getListenerRegistry();
        registry.appendListeners(EventType.PRE_INSERT, listener);
        registry.appendListeners(EventType.PRE_UPDATE, listener);
        registry.appendListeners(EventType.PRE_DELETE, listener);

        logger.debug("Registered asynchronous commit listeners for PostgreSQL");
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        handleEntity(event.getEntity(), event.getSession(), AsynchronousCommitAllowed.EntityOperationType.INSERT);
        return false;
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        handleEntity(event.getEntity(), event.getSession(), AsynchronousCommitAllowed.EntityOperationType.UPDATE);
        return false;
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        handleEntity(event.getEntity(), event.getSession(), AsynchronousCommitAllowed.EntityOperationType.DELETE);
        return false;
    }

    /** 根据实体类型与操作决定是否允许异步提交，必要时标记整笔事务需同步提交。 */
    private void handleEntity(Object entity, SharedSessionContractImplementor session, AsynchronousCommitAllowed.EntityOperationType opType) {
        if (!(session instanceof Session s)) {
            return;
        }

        Map<String, Object> props = s.getProperties();

        if (Boolean.TRUE.equals(props.get(SYNC_REQUIRED))) {
            return;
        }

        if (props.get(CALLBACK_REGISTERED) == null) {
            s.setProperty(CALLBACK_REGISTERED, Boolean.TRUE);
            session.getTransactionCompletionCallbacks().registerCallback(
                    (SharedSessionContractImplementor sess) -> {
                        if (!Boolean.TRUE.equals(((Session) sess).getProperties().get(SYNC_REQUIRED))) {
                            sess.doWork(AsyncCommitIntegrator::setAsyncCommit);
                        }
                    }
            );
        }

        if (entity instanceof AsynchronousCommitAllowed asyncEntity) {
            if (!asyncEntity.isAsyncCommitAllowed(opType)) {
                s.setProperty(SYNC_REQUIRED, Boolean.TRUE);
            }
        } else {
            s.setProperty(SYNC_REQUIRED, Boolean.TRUE);
        }
    }

    /**
     * 检测是否为启用逻辑复制的 Aurora PostgreSQL——此组合下 {@code synchronous_commit = off}
     * 可能导致已提交事务无法（或严重延迟）出现在 Debezium 等逻辑解码消费者中。
     * <p>
     * 检测方式：{@code SELECT aurora_version()} 仅 Aurora 存在；{@code SHOW wal_level = 'logical'} 表示可能有 CDC 消费者。
     * 异常时保守返回 {@code true} 以避免静默丢失 CDC 数据。
     * <p>
     * Detects Aurora PostgreSQL with logical replication enabled — a combination where
     * {@code SET LOCAL synchronous_commit TO OFF} can cause committed transactions to
     * never appear (or appear with extreme delay) in logical decoding consumers like Debezium.
     * <p>
     * Detection: {@code SELECT aurora_version()} only exists on Aurora (throws on standard PG);
     * {@code SHOW wal_level = 'logical'} indicates a CDC consumer may be reading the WAL.
     * Fails safe (returns {@code true}) on unexpected errors to avoid silent CDC data loss.
     *
     * @see <a href="https://repost.aws/questions/QU_4m9WIVUQ1aC-w4v2MzC7g">Aurora PostgreSQL does not perform logical decoding when synchronous_commit = off</a>
     */
    private static boolean isAuroraWithLogicalReplication(SessionFactoryImplementor sf) {
        try {
            JdbcConnectionAccess bootstrapJdbcConnectionAccess = sf.getJdbcServices().getBootstrapJdbcConnectionAccess();
            Connection connection = bootstrapJdbcConnectionAccess.obtainConnection();
            try {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT aurora_version()")) {
                    if (!rs.next()) {
                        return false;
                    }
                } catch (SQLException e) {
                    return false;
                }

                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW wal_level")) {
                    return rs.next() && "logical".equals(rs.getString(1));
                }
            } finally {
                bootstrapJdbcConnectionAccess.releaseConnection(connection);
            }
        } catch (SQLException e) {
            logger.warn("Failed to detect Aurora/logical replication status; disabling asynchronous commit optimization", e);
            return true;
        }
    }

    /** 在当前连接上执行 {@code SET LOCAL synchronous_commit TO OFF}。 */
    private static void setAsyncCommit(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET LOCAL synchronous_commit TO OFF");
        }
    }
}
