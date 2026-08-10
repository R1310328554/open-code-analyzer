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

package org.keycloak.models.sessions.infinispan.changes.remote.remover;

import org.keycloak.models.sessions.infinispan.changes.remote.updater.Updater;
import org.keycloak.models.sessions.infinispan.remote.transaction.RemoteChangeLogTransaction;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.util.concurrent.AggregateCompletionStage;

/**
 * 远程缓存条件删除器。
 * <p>
 * 适用于按键或值状态批量删除未知数量条目；实现可通过 Ickle 查询或全表扫描定位目标。
 * <p>
 * {@link RemoteChangeLogTransaction} 在应用 {@link Updater} 变更前调用 {@link #willRemove(Updater)}，
 * 跳过即将被本删除器移除的条目，避免无效网络写入。
 *
 * @param <K> {@link RemoteCache} 键类型
 * @param <V> {@link RemoteCache} 值类型
 */
public interface ConditionalRemover<K, V> {

    /**
     * @param key   The entry's key to test.
     * @param value The entry's value to test.
     * @return {@code true} if the entry will be removed from the {@link RemoteCache}.
     */
    boolean willRemove(K key, V value);

    /**
     * @param updater The {@link Updater} to test.
     * @return {@code true} if the entry tracked by the {@link Updater} will be removed from the {@link RemoteCache}.
     */
    default boolean willRemove(Updater<K, V> updater) {
        // 条目已标记删除时 value 可能为 null，此时无法预判条件，交由事务执行删除
        // 无值可校验时返回 false，由事务正常处理移除
        return updater.getValue() != null && willRemove(updater.getKey(), updater.getValue());
    }

    /**
     * Executes the conditional removes in the {@link RemoteCache}.
     *
     * @param cache The {@link RemoteCache} to perform the remove operations.
     * @param stage The {@link AggregateCompletionStage} to add any incomplete tasks.
     */
    void executeRemovals(RemoteCache<K, V> cache, AggregateCompletionStage<Void> stage);

}
