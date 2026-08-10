/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.transaction;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import org.hibernate.Session;
import org.jboss.logging.Logger;

/**
 * 默认 Infinispan 事务 Provider 工厂。
 * <p>
 * 创建 {@link DefaultInfinispanTransactionProvider} 并注册 prepare/after-completion 钩子；
 * 可选在 READ COMMITTED 隔离级别下将会话 DB 写入提前至主 JTA 事务的 prepare 阶段。
 */
public class DefaultInfinispanTransactionProviderFactory implements InfinispanTransactionProviderFactory, ServerInfoAwareProviderFactory {

    public static final String ID = "default";
    protected static Logger log = Logger.getLogger(DefaultInfinispanTransactionProviderFactory.class);
    /** 是否启用 prepare 阶段提前写入数据库。 */
    private boolean prepareEnabled;

    /** 是否在主事务中写入会话更新的配置项名称。 */
    public static final String CONFIG_WRITE_SESSION_UPDATES_IN_MAIN_TRANSACTION = "writeSessionUpdatesInMainTransaction";
    private static final boolean DEFAULT_WRITE_SESSION_UPDATES_IN_MAIN_TRANSACTION = true;
    /** 配置项原始值（postInit 前读取）。 */
    private boolean writeSessionUpdatesInMainTransaction;

    @Override
    public InfinispanTransactionProvider create(KeycloakSession session) {
        var provider = new DefaultInfinispanTransactionProvider(session);

        if (prepareEnabled) {
            // prepare 阶段尝试锁定 DB 实体；成功则 after-completion 无需再开新事务
            session.getTransactionManager().enlistPrepare(new AbstractKeycloakTransaction() {
                @Override
                protected void commitImpl() {
                    provider.prepareStep();
                }

                @Override
                protected void rollbackImpl() {
                }
            });
        }

        session.getTransactionManager().enlistAfterCompletion(provider);
        return provider;
    }

    @Override
    public void init(Config.Scope config) {
        writeSessionUpdatesInMainTransaction = config.getBoolean(CONFIG_WRITE_SESSION_UPDATES_IN_MAIN_TRANSACTION, DEFAULT_WRITE_SESSION_UPDATES_IN_MAIN_TRANSACTION);
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();

        builder.property()
                .name(CONFIG_WRITE_SESSION_UPDATES_IN_MAIN_TRANSACTION)
                .type("boolean")
                .helpText("Write session updates in the main transaction instead of writing them behind all the time")
                .defaultValue(DEFAULT_WRITE_SESSION_UPDATES_IN_MAIN_TRANSACTION)
                .add();

        return builder.build();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        if (writeSessionUpdatesInMainTransaction) {
            // 仅 READ COMMITTED 下可安全地将读锁升级为写锁（PostgreSQL、Oracle、MSSQL 默认）
            // 其他隔离级别可能导致死锁，例如 MariaDB（SnapshotIsolationException / "Record has changed since last read"）
            prepareEnabled = KeycloakModelUtils.runJobInTransactionWithResult(factory, session -> {
                EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
                return em.unwrap(Session.class).doReturningWork(connection -> {
                    int transactionIsolation = connection.getTransactionIsolation();
                    boolean allowed = transactionIsolation == Connection.TRANSACTION_READ_COMMITTED;
                    if (!allowed) {
                        log.debugf("Transaction isolation level %d does not allow for writing session updates in main transaction", transactionIsolation);
                    } else {
                        log.debugf("Transaction isolation level %d does allow for writing session updates in main transaction", transactionIsolation);
                    }
                    return allowed;
                });
            });
        }
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        Map<String, String> info = new HashMap<>();
        info.put(CONFIG_WRITE_SESSION_UPDATES_IN_MAIN_TRANSACTION, Boolean.toString(prepareEnabled));
        return info;
    }

    @Override
    public void close() {
    }

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        return Set.of(JpaConnectionProvider.class);
    }

    @Override
    public String getId() {
        return ID;
    }
}
