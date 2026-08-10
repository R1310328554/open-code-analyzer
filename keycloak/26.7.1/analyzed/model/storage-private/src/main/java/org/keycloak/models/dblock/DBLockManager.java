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

package org.keycloak.models.dblock;

import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;

/**
 * 数据库锁管理器：从 {@link KeycloakSession} 获取 {@link DBLockProvider}，
 * 并在启动时处理强制解锁等全局锁协调逻辑。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DBLockManager {

    protected static final Logger logger = Logger.getLogger(DBLockManager.class);

    private final KeycloakSession session;

    /** 绑定当前 Keycloak 会话，后续通过该会话解析锁 Provider。 */
    public DBLockManager(KeycloakSession session) {
        this.session = session;
    }

    /** 检查系统属性是否要求启动时强制释放 DB 锁（生产环境慎用）。 */
    public void checkForcedUnlock() {
        if (Boolean.getBoolean("keycloak.dblock.forceUnlock")) {
            DBLockProvider lock = getDBLock();
            if (lock.supportsForcedUnlock()) {
                logger.warn("Forced release of DB lock at startup requested by System property. Make sure to not use this in production environment! And especially when more cluster nodes are started concurrently.");
                lock.releaseLock();
            } else {
                throw new IllegalStateException("Forced unlock requested, but provider " + lock + " doesn't support it");
            }
        }
    }

    /** 获取当前会话关联的 {@link DBLockProvider} 实例。 */
    public DBLockProvider getDBLock() {
        return session.getProvider(DBLockProvider.class);
    }

    /** 获取 {@link DBLockProviderFactory}，用于访问锁 Provider 的配置与能力。 */
    public DBLockProviderFactory getDBLockFactory() {
        return (DBLockProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(DBLockProvider.class);
    }
}
