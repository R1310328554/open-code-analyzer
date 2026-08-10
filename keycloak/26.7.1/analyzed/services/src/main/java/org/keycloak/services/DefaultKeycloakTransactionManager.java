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
package org.keycloak.services;

import java.util.LinkedList;
import java.util.List;

import jakarta.transaction.TransactionManager;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.tracing.TracingProvider;
import org.keycloak.transaction.JtaTransactionManagerLookup;
import org.keycloak.transaction.JtaTransactionWrapper;

/**
 * {@link KeycloakTransactionManager} 默认实现：协调 prepare、主事务与 afterCompletion 三阶段提交/回滚。
 * <p>支持 {@link JTAPolicy#REQUIRES_NEW} 时挂起现有 JTA 并创建新事务；集成 {@link TracingProvider} 追踪。</p>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultKeycloakTransactionManager implements KeycloakTransactionManager {

    private final List<KeycloakTransaction> prepare = new LinkedList<>();
    private final List<KeycloakTransaction> transactions = new LinkedList<>();
    private final List<KeycloakTransaction> afterCompletion = new LinkedList<>();
    private boolean active;
    private boolean rollback;
    private final KeycloakSession session;
    private JTAPolicy jtaPolicy = JTAPolicy.REQUIRES_NEW;
    // 防止未捕获异常导致重复 commit/rollback
    protected boolean completed;

    /** @param session 所属 Keycloak 会话 */
    public DefaultKeycloakTransactionManager(KeycloakSession session) {
        this.session = session;
    }

    /** {@inheritDoc} 注册主阶段参与者；若已 begin 则自动启动子事务 */
    @Override
    public void enlist(KeycloakTransaction transaction) {
        if (completed) {
            throw new IllegalStateException("Transaction already completed");
        }
        if (active && !transaction.isActive()) {
            transaction.begin();
        }

        transactions.add(transaction);
    }

    /** {@inheritDoc} 注册主事务成功提交后才 commit 的参与者 */
    @Override
    public void enlistAfterCompletion(KeycloakTransaction transaction) {
        if (completed) {
            throw new IllegalStateException("Transaction already completed");
        }
        if (active && !transaction.isActive()) {
            transaction.begin();
        }

        afterCompletion.add(transaction);
    }

    /** {@inheritDoc} 注册两阶段 prepare 阶段参与者（先于主事务 commit） */
    @Override
    public void enlistPrepare(KeycloakTransaction transaction) {
        if (completed) {
            throw new IllegalStateException("Transaction already completed");
        }
        if (active && !transaction.isActive()) {
            transaction.begin();
        }

        prepare.add(transaction);
    }

    @Override
    public JTAPolicy getJTAPolicy() {
        return jtaPolicy;
    }

    @Override
    public void setJTAPolicy(JTAPolicy policy) {
        jtaPolicy = policy;

    }

    /** {@inheritDoc} 按 JTA 策略启动事务并 begin 所有已登记子事务 */
    @Override
    public void begin() {
        if (completed) {
            throw new IllegalStateException("Transaction already completed");
        }
        if (active) {
             throw new IllegalStateException("Transaction already active");
        }

        if (jtaPolicy == JTAPolicy.REQUIRES_NEW) {
            JtaTransactionManagerLookup jtaLookup = session.getProvider(JtaTransactionManagerLookup.class);
            if (jtaLookup != null) {
                TransactionManager tm = jtaLookup.getTransactionManager();
                if (tm != null) {
                   enlist(new JtaTransactionWrapper(session, tm));
                }
            }
        }

        for (KeycloakTransaction tx : transactions) {
            if (!tx.isActive()) {
                tx.begin();
            }
        }

        for (KeycloakTransaction tx : prepare) {
            if (!tx.isActive()) {
                tx.begin();
            }
        }

        for (KeycloakTransaction tx : afterCompletion) {
            if (!tx.isActive()) {
                tx.begin();
            }
        }

        active = true;
    }

    /** {@inheritDoc} 依次 commit prepare → 主 → afterCompletion；失败则回滚 */
    @Override
    public void commit() {
        if (completed) {
            return;
        } else {
            completed = true;
        }

        TracingProvider tracing = session.getProvider(TracingProvider.class);
        tracing.trace(DefaultKeycloakTransactionManager.class, "commit", span -> {
            RuntimeException exception = null;
            for (KeycloakTransaction tx : prepare) {
                try {
                    commitWithTracing(tx, tracing);
                } catch (RuntimeException e) {
                    exception = exception == null ? e : exception;
                }
            }
            if (exception != null) {
                rollback(exception);
                return;
            }
            for (KeycloakTransaction tx : transactions) {
                try {
                    commitWithTracing(tx, tracing);
                } catch (RuntimeException e) {
                    exception = exception == null ? e : exception;
                }
            }

            // 主事务 commit 失败时不 commit afterCompletion 参与者
            if (exception == null) {
                for (KeycloakTransaction tx : afterCompletion) {
                    try {
                        commitWithTracing(tx, tracing);
                    } catch (RuntimeException e) {
                        exception = exception == null ? e : exception;
                    }
                }
            } else {
                for (KeycloakTransaction tx : afterCompletion) {
                    try {
                        tx.rollback();
                    } catch (RuntimeException e) {
                        ServicesLogger.LOGGER.exceptionDuringRollback(e);
                    }
                }
            }

            active = false;
            if (exception != null) {
                throw exception;
            }
        });
    }

    private static void commitWithTracing(KeycloakTransaction tx, TracingProvider tracing) {
        tracing.trace(tx.getClass(), "commit", span -> {
            tx.commit();
        });
    }

    /** {@inheritDoc} 回滚所有已登记子事务 */
    @Override
    public void rollback() {
        if (completed) {
            return;
        } else {
            completed = true;
        }

        RuntimeException exception = null;
        rollback(exception);
    }

    protected void rollback(RuntimeException exception) {
        TracingProvider tracing = session.getProvider(TracingProvider.class);

        for (KeycloakTransaction tx : transactions) {
            try {
                rollbackWithTracing(tx, tracing);
            } catch (RuntimeException e) {
                exception = exception != null ? e : exception;
            }
        }
        for (KeycloakTransaction tx : afterCompletion) {
            try {
                rollbackWithTracing(tx, tracing);
            } catch (RuntimeException e) {
                exception = exception != null ? e : exception;
            }
        }
        active = false;
        if (exception != null) {
            throw exception;
        }
    }

    private static void rollbackWithTracing(KeycloakTransaction tx, TracingProvider tracing) {
        tracing.trace(tx.getClass(), "rollback", span -> {
            tx.rollback();
        });
    }

    /** {@inheritDoc} 标记当前事务仅可回滚 */
    @Override
    public void setRollbackOnly() {
        rollback = true;
    }

    /** {@inheritDoc} @return 是否已标记或子事务要求回滚 */
    @Override
    public boolean getRollbackOnly() {
        if (rollback) {
            return true;
        }

        for (KeycloakTransaction tx : transactions) {
            if (tx.getRollbackOnly()) {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} @return 事务是否已 begin 且未完成 */
    @Override
    public boolean isActive() {
        return active;
    }

}
