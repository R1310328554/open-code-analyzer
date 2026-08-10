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

package org.keycloak.quarkus.runtime.transaction;

import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.KeycloakSessionUtil;

/**
 * <p>A {@link TransactionalSessionHandler} is responsible for managing transaction sessions and its lifecycle. Its subtypes
 * are usually related to components available from the underlying stack that runs on top of the request processing chain
 * as well as at the end in order to create transaction sessions and close them accordingly, respectively.
 * 负责管理 Keycloak 事务会话及其生命周期；子类型通常嵌入底层请求处理链，在链首开启事务、在链尾关闭会话。
 */
public interface TransactionalSessionHandler {

    /**
     * 在可能的情况下开启事务。
     *
     * @param session a session
     */
    default void beginTransaction(KeycloakSession session) {
        session.getTransactionManager().begin();
    }

    /**
     * 关闭 {@link KeycloakSession} 并清除线程本地绑定。
     *
     * @param session a session
     */
    default void close(KeycloakSession session) {
        try {
            if (session == null || session.isClosed()) {
                return;
            }
    
            session.close();
        } finally {
            // 确保线程上下文不再持有已关闭的会话引用
            KeycloakSessionUtil.setKeycloakSession(null);
        }
    }
}
