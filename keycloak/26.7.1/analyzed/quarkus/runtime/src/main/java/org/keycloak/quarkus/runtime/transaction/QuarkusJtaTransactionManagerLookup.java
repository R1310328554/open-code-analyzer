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

package org.keycloak.quarkus.runtime.transaction;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.TransactionManager;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.transaction.JtaTransactionManagerLookup;

import org.jboss.logging.Logger;

/**
 * Quarkus 运行时 {@link JtaTransactionManagerLookup} 实现，通过 CDI 获取 Jakarta {@link TransactionManager}。
 */
public class QuarkusJtaTransactionManagerLookup implements JtaTransactionManagerLookup {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(QuarkusJtaTransactionManagerLookup.class);

    /** 懒加载并缓存的 JTA 事务管理器实例。 */
    private volatile TransactionManager tm;

    /** {@inheritDoc} 首次调用时从 CDI 容器解析 {@link TransactionManager}。 */
    @Override
    public TransactionManager getTransactionManager() {
        if (tm == null) {
            synchronized (this) {
                if (tm == null) {
                    // 通过 Quarkus CDI 获取容器托管的 TransactionManager
                    tm = CDI.current().select(TransactionManager.class).get();
                    logger.tracev("TransactionManager = {0}", tm);
                    if (tm == null) {
                        throw new RuntimeException("You must provide JTA TransactionManager as the default transaction type is JTA");
                    }
                }
            }
        }
        return tm;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 返回标识符 {@code quarkus}。 */
    @Override
    public String getId() {
        return "quarkus";
    }

    /** {@inheritDoc} Quarkus 实现的优先级。 */
    @Override
    public int order() {
        return 100;
    }
}
