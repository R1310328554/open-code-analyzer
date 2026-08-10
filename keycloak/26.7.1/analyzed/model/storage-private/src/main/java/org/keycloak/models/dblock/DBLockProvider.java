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

import org.keycloak.provider.Provider;

/**
 * <p>全局数据库锁，确保集群中某些 DB 操作同一时刻仅由一个节点执行。</p>
 *
 * <p>锁按命名空间（{@link Namespace}）区分类型或上下文。同一 {@link DBLockProvider}
 * 实例（同一会话）只能锁定一个命名空间，重复加锁将抛出 {@code RuntimeException}；
 * {@link #getCurrentLock()} 返回当前 Provider 在本机持有的命名空间。</p>
 *
 * <p>不同 {@link DBLockProvider} 实例可在不同线程中加锁。Provider 与会话绑定，
 * 因此需要不同锁实例时必须使用不同的 {@link org.keycloak.models.KeycloakSession}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface DBLockProvider extends Provider {

    /** 锁命名空间，用于区分不同锁类型或业务上下文。 */
    enum Namespace {

        /** 通用数据库迁移与 schema 变更锁。 */
        DATABASE(1),
        /** Keycloak 启动引导流程专用锁。 */
        KEYCLOAK_BOOT(1000)
        // OFFLINE_SESSIONS(1001) -- Not used anymore. Keeping to avoid reusing the number.
        ;

        private final int id;

        Namespace(int id) {
            this.id = id;
        }

        /** 命名空间在持久化锁表中的数值标识。 */
        public int getId() {
            return id;
        }
    }

    /**
     * 尝试获取 DB 锁；若未成功则阻塞等待。超时（默认 900 秒）或已锁定其他命名空间时抛出异常。
     *
     * @param lock 要锁定的命名空间
     */
    void waitForLock(Namespace lock);

    /** 释放本 Provider 先前持有的锁。 */
    void releaseLock();

    /**
     * 返回本 Provider 当前持有的命名空间；未持锁时返回 {@code null}。
     *
     * @return 已锁定的命名空间，或 {@code null}
     */
    Namespace getCurrentLock();

    /**
     * @return 若 Provider 支持启动时强制解锁则返回 {@code true}
     */
    boolean supportsForcedUnlock();

    /** 销毁锁状态（例如删除跟踪锁的表或集合）。 */
    void destroyLockInfo();
}
