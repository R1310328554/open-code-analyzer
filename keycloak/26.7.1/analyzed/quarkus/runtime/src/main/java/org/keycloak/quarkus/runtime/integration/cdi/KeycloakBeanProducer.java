/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.inject.Inject;

import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.keycloak.quarkus.runtime.transaction.TransactionalSessionHandler;

import io.quarkus.arc.Unremovable;
import org.jboss.logging.Logger;

/**
 * CDI 生产者：为每个 HTTP 请求提供 {@link KeycloakSession}，
 * 并在销毁时通过 {@link org.keycloak.quarkus.runtime.transaction.TransactionalSessionHandler} 关闭会话。
 */
@ApplicationScoped
@Unremovable
public class KeycloakBeanProducer implements TransactionalSessionHandler {
    
    private static final Logger logger = Logger.getLogger(KeycloakBeanProducer.class);

    @Inject
    QuarkusKeycloakSessionFactory factory;

    @RequestScoped
    public KeycloakSession getKeycloakSession() {
        // 首次调用 session 方法时才懒创建；此处不启动 JTA 事务——
        // prematching 过滤器可能仍在事件循环中，事务须推迟到阻塞线程。
        return factory.create();
    }

    /** 请求结束时销毁会话；若未主动关闭则记录警告并强制关闭。 */
    void dispose(@Disposes KeycloakSession session) {
        if (!session.isClosed()) {
            logger.warn("Proactive closing of the session was missed - refinements are needed to TransactionSessionHandler related logic");
        }
        close(session);
    }
}
