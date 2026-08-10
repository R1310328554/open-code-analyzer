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

package org.keycloak.models;

/**
 * Keycloak 事务抽象基类：封装启动、提交、回滚及 rollback-only 等通用状态机逻辑。
 * Handles some common transaction logic related to start, rollback-only etc.
 * <p>子类实现 {@link #commitImpl()} 与 {@link #rollbackImpl()} 完成具体资源操作。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractKeycloakTransaction implements KeycloakTransaction {

    /** 当前事务状态。 */
    protected TransactionState state = TransactionState.NOT_STARTED;

    /** 启动事务；重复启动抛出 {@link IllegalStateException}。 */
    @Override
    public void begin() {
        if (state != TransactionState.NOT_STARTED) {
            throw new IllegalStateException("Transaction already started");
        }

        beginImpl();

        state = TransactionState.STARTED;
    }

    /** 提交事务；非 STARTED 状态抛出异常。 */
    @Override
    public void commit() {
        if (state != TransactionState.STARTED) {
            throw new IllegalStateException("Transaction in illegal state for commit: " + state);
        }

        commitImpl();

        state = TransactionState.FINISHED;
    }

    /** 回滚事务；允许 STARTED 或 ROLLBACK_ONLY 状态。 */
    @Override
    public void rollback() {
        if (state != TransactionState.STARTED && state != TransactionState.ROLLBACK_ONLY) {
            throw new IllegalStateException("Transaction in illegal state for rollback: " + state);
        }

        rollbackImpl();

        state = TransactionState.FINISHED;
    }

    /** 标记事务为仅回滚（commit 时将失败）。 */
    @Override
    public void setRollbackOnly() {
        state = TransactionState.ROLLBACK_ONLY;
    }

    /** @return 是否已标记为仅回滚 */
    @Override
    public boolean getRollbackOnly() {
        return state == TransactionState.ROLLBACK_ONLY;
    }

    /** @return 事务是否处于活动状态（STARTED 或 ROLLBACK_ONLY） */
    @Override
    public boolean isActive() {
        return state == TransactionState.STARTED || state == TransactionState.ROLLBACK_ONLY;
    }

    /** @return 当前事务状态枚举值 */
    public TransactionState getState() {
        return state;
    }

    /** 事务生命周期状态枚举。 */
    public enum TransactionState {
        /** 未启动 */ NOT_STARTED,
        /** 已启动 */ STARTED,
        /** 仅回滚 */ ROLLBACK_ONLY,
        /** 已结束 */ FINISHED
    }

    /** 子类可覆盖的启动钩子（默认空实现）。 */
    protected void beginImpl() {}

    /** 子类实现的提交逻辑。 */
    protected abstract void commitImpl();

    /** 子类实现的回滚逻辑。 */
    protected abstract void rollbackImpl();
}
