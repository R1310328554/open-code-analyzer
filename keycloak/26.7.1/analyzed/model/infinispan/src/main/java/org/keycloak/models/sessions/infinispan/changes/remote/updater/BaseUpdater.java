/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.sessions.infinispan.changes.remote.updater;

import java.util.Objects;

/**
 * {@link Updater} 实现的公共基类。
 * <p>
 * 持有 Infinispan 缓存键、值、版本及生命周期状态；具体子类自行跟踪字段变更，
 * 并通过 {@link #isUnchanged()} 报告实体是否被修改。
 *
 * @param <K> Infinispan 缓存键类型
 * @param <V> Infinispan 缓存值类型
 */
public abstract class BaseUpdater<K, V> implements Updater<K, V> {

    private final K cacheKey;
    private final V cacheValue;
    private final long versionRead;
    // 构造时的初始状态，用于 resetState
    private final UpdaterState initialState;
    private UpdaterState state;

    protected BaseUpdater(K cacheKey, V cacheValue, long versionRead, UpdaterState state) {
        this.cacheKey = Objects.requireNonNull(cacheKey);
        this.cacheValue = cacheValue;
        this.versionRead = versionRead;
        this.state = Objects.requireNonNull(state);
        this.initialState = state;
    }

    @Override
    public final K getKey() {
        return cacheKey;
    }

    @Override
    public final V getValue() {
        return cacheValue;
    }

    @Override
    public final long getVersionRead() {
        return versionRead;
    }

    @Override
    public final boolean isDeleted() {
        return state == UpdaterState.DELETED || state == UpdaterState.DELETED_TRANSIENT;
    }

    @Override
    public final boolean isCreated() {
        return state == UpdaterState.CREATED;
    }

    @Override
    public final boolean isReadOnly() {
        return state == UpdaterState.READ && isUnchanged();
    }

    @Override
    public final boolean isExpired() {
        return state == UpdaterState.EXPIRED;
    }

    @Override
    public final void markDeleted() {
        // 已创建/瞬态条目标记为 DELETED_TRANSIENT；已读条目标记为 DELETED
        state = switch (state) {
            case READ, DELETED -> UpdaterState.DELETED;
            case CREATED, DELETED_TRANSIENT, EXPIRED -> UpdaterState.DELETED_TRANSIENT;
        };
    }

    @Override
    public void markExpired() {
        state = UpdaterState.EXPIRED;
    }

    @Override
    public boolean isTransient() {
        return state == UpdaterState.DELETED_TRANSIENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseUpdater<?, ?> that = (BaseUpdater<?, ?>) o;
        return cacheKey.equals(that.cacheKey);
    }

    @Override
    public int hashCode() {
        return cacheKey.hashCode();
    }

    @Override
    public String toString() {
        return "BaseUpdater{" +
                "cacheKey=" + cacheKey +
                ", cacheValue=" + cacheValue +
                ", state=" + state +
                ", versionRead=" + versionRead +
                '}';
    }

    /**
     * 将 {@link UpdaterState} 重置为构造时的初始值。
     */
    protected final void resetState() {
        state = initialState;
    }

    /**
     * @return {@code true} 表示实体自创建/读取后未被修改
     */
    protected abstract boolean isUnchanged();

    /** Updater 生命周期状态枚举。 */
    protected enum UpdaterState {
        /**
         * 当前 Keycloak 事务中新创建的缓存条目。
         */
        CREATED,
        /**
         * 已删除，提交时从 Infinispan 移除，不可重建。
         */
        DELETED,
        /**
         * 从 Infinispan 缓存读取的既有条目。
         */
        READ,
        /**
         * 瞬态且已删除：不会写入外部 Infinispan 集群。
         */
        DELETED_TRANSIENT,
        /**
         * 已过期（max-idle 或 lifespan），不应再应用变更。
         */
        EXPIRED,

    }
}
