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

/**
 * 标记接口：实现此接口的 JPA 实体可容忍异步提交。
 * <p>
 * 当一笔事务仅修改实现了本接口且 {@link #isAsyncCommitAllowed(EntityOperationType)} 对相应操作
 * 返回 {@code true} 的实体时，{@link AsyncCommitIntegrator} 可在 PostgreSQL 上启用异步提交。
 * 目前仅支持 PostgreSQL。
 * <p>
 * 未实现本接口的实体视为“重要数据”——对其的任何修改都会强制整笔事务同步提交。
 * <p>
 * Marker interface for JPA entities that can tolerate asynchronous commit.
 * <p>
 * When a transaction only modifies entities that implement this interface (and whose
 * {@link #isAsyncCommitAllowed(EntityOperationType)} returns {@code true} for the
 * respective operation). See {@link AsyncCommitIntegrator} for details.
 * This is currently only supported for PostgreSQL databases.
 * <p>
 * Entities that do NOT implement this interface are considered "important" — any modification
 * to them forces synchronous commit for the entire transaction.
 *
 * @author Alexander Schwartz
 */
public interface AsynchronousCommitAllowed {

    /** 实体上的数据库操作类型。 */
    enum EntityOperationType {
        INSERT, UPDATE, DELETE
    }

    /**
     * 给定操作类型是否允许异步提交。
     * <p>
     * 若事务中任一操作返回 {@code false}，整笔事务将强制同步提交。
     * <p>
     * Whether this entity allows asynchronous commit for the given operation type.
     * <p>
     * Returning {@code false} for any operation that occurs during a transaction
     * will force synchronous commit for the entire transaction.
     *
     * @param operationType the type of database operation being performed
     * @return {@code true} if the operation can tolerate asynchronous commit
     */
    default boolean isAsyncCommitAllowed(EntityOperationType operationType) {
        return true;
    }

}
