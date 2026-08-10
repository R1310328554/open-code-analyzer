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


import java.util.function.BiFunction;

import org.keycloak.models.sessions.infinispan.remote.transaction.RemoteChangeLogTransaction;

/**
 * {@link RemoteChangeLogTransaction} 使用的实体变更跟踪接口。
 * <p>
 * 记录 Keycloak 事务中对缓存实体的修改，并在提交时应用到 Infinispan 远程缓存。
 *
 * @param <K> Infinispan 键类型
 * @param <V> Infinispan 值类型
 */
public interface Updater<K, V> extends BiFunction<K, V, V> {

    /** 表示条目尚无版本号（如新创建）。 */
    int NO_VERSION = -1;

    /**
     * @return Infinispan 缓存键
     */
    K getKey();

    /**
     * @return 事务中使用的最新实体快照
     */
    V getValue();

    /**
     * @return 首次从 Infinispan 读取时的条目版本
     */
    long getVersionRead();

    /**
     * @return {@code true} 表示 Keycloak 事务中已删除，提交时应从 Infinispan 移除
     */
    boolean isDeleted();

    /**
     * @return {@code true} 表示 Keycloak 事务中新创建，可优化为 put-if-absent
     */
    boolean isCreated();

    /**
     * @return {@code true} 表示实体未被修改
     */
    boolean isReadOnly();

    /**
     * @return {@code true} 表示实体已过期
     */
    boolean isExpired();

    /**
     * @return {@code true} 表示实体无效，事务中不可访问
     */
    default boolean isInvalid() {
        return isExpired() || isDeleted();
    }

    /**
     * 标记实体为已删除。
     */
    void markDeleted();

    /**
     * 从 Infinispan 加载时标记实体已过期。
     */
    void markExpired();

    /**
     * @return {@code true} 表示瞬态实体，不应写入 Infinispan 缓存
     */
    default boolean isTransient() {
        return false;
    }

    /**
     * 计算 Infinispan 缓存条目的过期参数。
     *
     * @return {@link Expiration} 过期配置
     */
    Expiration computeExpiration();

    /** @return {@code true} 表示已从缓存读取并持有版本号 */
    default boolean hasVersion() {
        return getVersionRead() != NO_VERSION;
    }
}
