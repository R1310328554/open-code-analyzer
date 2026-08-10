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

package org.keycloak.connections.jpa.updater.liquibase;

import org.keycloak.models.KeycloakSession;

/**
 * 线程本地 {@link KeycloakSession} 持有者。
 * Liquibase 自定义 changeset 任务无法直接注入会话对象，故通过 ThreadLocal 在升级/校验期间传递当前会话。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ThreadLocalSessionContext {

    /** 当前线程绑定的 Keycloak 会话。 */
    private static final ThreadLocal<KeycloakSession> currentSession = new ThreadLocal<KeycloakSession>();

    /** 获取当前线程会话；未设置时返回 null。 */
    public static KeycloakSession getCurrentSession() {
        return currentSession.get();
    }

    /** 将 Keycloak 会话绑定到当前线程（升级流程入口调用）。 */
    public static void setCurrentSession(KeycloakSession session) {
        currentSession.set(session);
    }

    /** 清除线程本地会话，防止线程池复用时泄漏。 */
    public static void removeCurrentSession() {
        currentSession.remove();
    }
}
