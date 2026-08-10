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
package org.keycloak.models.sessions.infinispan.remote.transaction;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.keycloak.common.util.Retry;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.sessions.infinispan.changes.remote.remover.ConditionalRemover;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Expiration;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Updater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.UpdaterFactory;
import org.keycloak.models.sessions.infinispan.transaction.DatabaseUpdate;
import org.keycloak.models.sessions.infinispan.transaction.NonBlockingTransaction;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.util.concurrent.AggregateCompletionStage;
import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.util.concurrent.BlockingManager;

/**
 * 跟踪 Infinispan 远程缓存实体变更的 {@link KeycloakTransaction} 实现。
 * <p>
 * 事务内缓存 {@link Updater} 副本，提交时按创建/替换/删除/过期等状态异步写入远程缓存，
 * 并支持指数退避重试；条件删除由 {@link ConditionalRemover} 在提交阶段执行。
 *
 * @param <K> Infinispan 缓存键类型。
 * @param <V> Infinispan 缓存值类型。
 * @param <T> {@link Updater} 实现类型。
 */
public class RemoteChangeLogTransaction<K, V, T extends Updater<K, V>, R extends ConditionalRemover<K, V>> implements NonBlockingTransaction {

    private static final RetryOperationSuccess<?, ?, ?> TO_NULL = (ignored1, ignored2, ignored3) -> CompletableFutures.completedNull();

    /** 本事务内键到 updater 的变更映射。 */
    private final Map<K, T> entityChanges;
    private final UpdaterFactory<K, V, T> factory;
    /** 批量条件删除器（如按 realm/user 删除）。 */
    private final R conditionalRemover;
    /** 共享的远程缓存与重试配置。 */
    private final SharedState<K, V> sharedState;

    RemoteChangeLogTransaction(UpdaterFactory<K, V, T> factory, SharedState<K, V> sharedState, R conditionalRemover) {
        this.factory = Objects.requireNonNull(factory);
        this.conditionalRemover = Objects.requireNonNull(conditionalRemover);
        this.sharedState = Objects.requireNonNull(sharedState);
        entityChanges = new ConcurrentHashMap<>(8);
    }

    /** 异步提交：先执行条件删除，再逐条提交实体变更。 */
    @Override
    public void asyncCommit(AggregateCompletionStage<Void> stage, Consumer<DatabaseUpdate> databaseUpdates) {
        conditionalRemover.executeRemovals(getCache(), stage);

        for (var updater : entityChanges.values()) {
            if (updater.isReadOnly() || updater.isExpired() || updater.isTransient() || conditionalRemover.willRemove(updater)) {
                continue;
            }
            if (updater.isDeleted()) {
                stage.dependsOn(commitRemove(updater));
                continue;
            }

            var expiration = updater.computeExpiration();

            if (expiration.isExpired()) {
                // 依赖服务端缓存过期事件，此处不写缓存。
                continue;
            }

            if (updater.isCreated()) {
                stage.dependsOn(commitPutIfAbsent(updater, expiration));
                continue;
            }

            if (updater.hasVersion()) {
                stage.dependsOn(commitReplace(updater, expiration));
                continue;
            }

            stage.dependsOn(commitCompute(updater, expiration));
        }
    }

    @Override
    public void asyncRollback(AggregateCompletionStage<Void> stage) {
        entityChanges.clear();
    }


    /**
     * @return 本事务跟踪的 {@link RemoteCache}。
     */
    public RemoteCache<K, V> getCache() {
        return sharedState.cache();
    }

    /**
     * 按 key 获取实体 updater；事务内无副本时从 {@link RemoteCache} 加载。
     *
     * @param key 要读取的 Infinispan 缓存键。
     * @return 用于跟踪后续变更的 {@link Updater}。
     */
    public T get(K key) {
        var updater = entityChanges.get(key);
        if (updater != null) {
            return updater.isInvalid() ? null : updater;
        }
        return onEntityFromCache(key, getCache().getWithMetadata(key));
    }

    /**
     * {@link #get(Object)} 的非阻塞版本。
     *
     * @param key 要读取的 Infinispan 缓存键。
     * @return 用于跟踪后续变更的 {@link Updater}。
     */
    public CompletionStage<T> getAsync(K key) {
        var updater = entityChanges.get(key);
        if (updater != null) {
            return updater.isInvalid() ? CompletableFutures.completedNull() : CompletableFuture.completedFuture(updater);
        }
        return getCache().getWithMetadataAsync(key).thenApply(e -> onEntityFromCache(key, e));
    }

    /**
     * 登记将在远程缓存中创建的新实体。
     *
     * @param key    关联的 Infinispan 缓存键。
     * @param entity 缓存值。
     * @return 用于跟踪后续变更的 {@link Updater}。
     */
    public T create(K key, V entity) {
        var updater = factory.create(key, entity);
        entityChanges.put(key, updater);
        return updater;
    }

    /**
     * 从 {@link RemoteCache} 删除指定键。
     *
     * @param key 要删除的 Infinispan 缓存键。
     */
    public void remove(K key) {
        var updater = entityChanges.get(key);
        if (updater != null) {
            updater.markDeleted();
            return;
        }
        entityChanges.put(key, factory.deleted(key));
    }

    R getConditionalRemover() {
        return conditionalRemover;
    }

    public T wrap(Map.Entry<K, MetadataValue<V>> entry) {
        return entityChanges.computeIfAbsent(entry.getKey(), k -> factory.wrapFromCache(k, entry.getValue()));
    }

    public T wrap(K key, V value, long version) {
        return entityChanges.computeIfAbsent(key, k -> factory.wrapFromCache(k, value, version));
    }

    protected Map<K, T> getCachedEntities() {
        return entityChanges;
    }

    private T onEntityFromCache(K key, MetadataValue<V> entity) {
        if (entity == null) {
            return null;
        }
        var updater = factory.wrapFromCache(key, entity);
        entityChanges.put(key, updater);
        return updater.isInvalid() ? null : updater;
    }

    @SuppressWarnings("unchecked")
    private CompletionStage<Void> commitRemove(Updater<K, V> updater) {
        return executeWithRetries(this::invokeCacheRemove, (RetryOperationSuccess<V, K, V>) TO_NULL, updater, null, 0);
    }

    private CompletionStage<Void> commitPutIfAbsent(Updater<K, V> updater, Expiration expiration) {
        return executeWithRetries(this::invokeCachePutIfAbsent, this::handleBooleanResult, updater, expiration, 0);
    }

    private CompletionStage<Void> commitReplace(Updater<K, V> updater, Expiration expiration) {
        return executeWithRetries(this::invokeCacheReplace, this::handleBooleanResult, updater, expiration, 0);
    }

    @SuppressWarnings("unchecked")
    private CompletionStage<Void> commitCompute(Updater<K, V> updater, Expiration expiration) {
        return executeWithRetries(this::invokeCacheCompute, (RetryOperationSuccess<V, K, V>) TO_NULL, updater, expiration, 0);
    }

    private CompletionStage<Void> handleBooleanResult(boolean success, Updater<K, V> updater, Expiration expiration) {
        return success ?
                CompletableFutures.completedNull() :
                commitCompute(updater, expiration);
    }

    private CompletionStage<V> invokeCacheRemove(Updater<K, V> updater, Expiration ignored) {
        return getCache().removeAsync(updater.getKey());
    }

    private CompletionStage<Boolean> invokeCachePutIfAbsent(Updater<K, V> updater, Expiration expiration) {
        return getCache().withFlags(Flag.FORCE_RETURN_VALUE)
                .putIfAbsentAsync(updater.getKey(), updater.getValue(), expiration.lifespan(), TimeUnit.MILLISECONDS, expiration.maxIdle(), TimeUnit.MILLISECONDS)
                .thenApply(Objects::isNull);
    }

    private CompletionStage<Boolean> invokeCacheReplace(Updater<K, V> updater, Expiration expiration) {
        return getCache().replaceWithVersionAsync(updater.getKey(), updater.getValue(), updater.getVersionRead(), expiration.lifespan(), TimeUnit.MILLISECONDS, expiration.maxIdle(), TimeUnit.MILLISECONDS);
    }

    private CompletionStage<V> invokeCacheCompute(Updater<K, V> updater, Expiration expiration) {
        return getCache().computeIfPresentAsync(updater.getKey(), updater, expiration.lifespan(), TimeUnit.MILLISECONDS, expiration.maxIdle(), TimeUnit.MILLISECONDS);
    }

    private <OR> CompletionStage<Void> executeWithRetries(RetryOperation<OR, K, V> operation, RetryOperationSuccess<OR, K, V> onSuccessAction, Updater<K, V> updater, Expiration expiration, int retry) {
        return operation.execute(updater, expiration)
                .handle((result, throwable) -> handleOperationResult(result, throwable, operation, onSuccessAction, updater, expiration, retry))
                .thenCompose(CompletableFutures.identity());
    }

    private <OR> CompletionStage<Void> handleOperationResult(OR result, Throwable throwable, RetryOperation<OR, K, V> operation, RetryOperationSuccess<OR, K, V> onSuccessAction, Updater<K, V> updater, Expiration expiration, int retry) {
        if (throwable == null) {
            return onSuccessAction.onSuccess(result, updater, expiration);
        }
        if (retry >= sharedState.maxRetries()) {
            return CompletableFuture.failedFuture(CompletableFutures.extractException(throwable));
        }
        return backOffAndExecuteWithRetries(operation, onSuccessAction, updater, expiration, retry + 1);
    }

    private <OR> CompletionStage<Void> backOffAndExecuteWithRetries(RetryOperation<OR, K, V> operation, RetryOperationSuccess<OR, K, V> onSuccessAction, Updater<K, V> updater, Expiration expiration, int retry) {
        var delayMillis = Retry.computeBackoffInterval(sharedState.backOffBaseTimeMillis(), retry);
        return sharedState.blockingManager().scheduleRunBlocking(
                        () -> executeWithRetries(operation, onSuccessAction, updater, expiration, retry),
                        delayMillis, TimeUnit.MILLISECONDS, "retry-" + updater)
                .thenCompose(CompletableFutures.identity());
    }

    private interface RetryOperation<R, K, V> {
        CompletionStage<R> execute(Updater<K, V> updater, Expiration expiration);
    }

    private interface RetryOperationSuccess<R, K, V> {
        CompletionStage<Void> onSuccess(R result, Updater<K, V> updater, Expiration expiration);
    }

    // 尽量缩小类体积：每次请求新建实例，共享状态可在多实例间复用。
    public interface SharedState<K, V> {
        RemoteCache<K, V> cache();

        int maxRetries();

        int backOffBaseTimeMillis();

        BlockingManager blockingManager();
    }
}
