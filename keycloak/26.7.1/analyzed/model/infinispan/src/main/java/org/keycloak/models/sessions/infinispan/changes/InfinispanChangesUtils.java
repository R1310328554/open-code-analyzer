/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.changes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.connections.infinispan.InfinispanUtil;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.sessions.infinispan.CacheDecorators;
import org.keycloak.models.sessions.infinispan.SessionAffinityService;
import org.keycloak.models.sessions.infinispan.SessionFunction;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.AggregateCompletionStage;
import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.util.concurrent.ActionSequencer;
import org.jboss.logging.Logger;

/**
 * 嵌入式变更日志事务的工具方法。
 * <p>
 * 负责创建 {@link CacheHolder}、在集群上执行合并后的缓存操作（ADD/REPLACE/REMOVE），
 * 以及带重试与版本检查的 replace 流程。
 */
public class InfinispanChangesUtils {

    private InfinispanChangesUtils() {
    }

    /** 绑定 Infinispan 缓存、ActionSequencer 与亲和键生成器。 */
    public static <K, V extends SessionEntity> CacheHolder<K, V> createWithCache(KeycloakSession session,
                                                                                 String cacheName,
                                                                                 SessionFunction<V> lifespanFunction,
                                                                                 SessionFunction<V> maxIdleFunction) {
        return createWithCache(session, cacheName, lifespanFunction, maxIdleFunction, null);
    }

    public static <K, V extends SessionEntity> CacheHolder<K, V> createWithCache(KeycloakSession session,
                                                                                 String cacheName,
                                                                                 SessionFunction<V> lifespanFunction,
                                                                                 SessionFunction<V> maxIdleFunction,
                                                                                 Supplier<K> keyGenerator) {
        var connections = session.getProvider(InfinispanConnectionProvider.class);
        var cache = connections.<K, SessionEntityWrapper<V>>getCache(cacheName);
        var sequencer = new ActionSequencer(connections.getExecutor(cacheName + "Replace"), false, null);
        return new CacheHolder<>(cache, sequencer, lifespanFunction, maxIdleFunction, SessionAffinityService.create(cache, keyGenerator));
    }

    /** 无底层 Infinispan 缓存的 CacheHolder（仅超时计算）。 */
    public static <K, V extends SessionEntity> CacheHolder<K, V> createWithoutCache(SessionFunction<V> lifespanFunction,
                                                                                    SessionFunction<V> maxIdleFunction) {
        return new CacheHolder<>(null, null, lifespanFunction, maxIdleFunction, null);
    }

    /** 无缓存但带自定义键生成器的 CacheHolder。 */
    public static <K, V extends SessionEntity> CacheHolder<K, V> createWithoutCache(SessionFunction<V> lifespanFunction,
                                                                                    SessionFunction<V> maxIdleFunction,
                                                                                    Supplier<K> keyGenerator) {
        return new CacheHolder<>(null, null, lifespanFunction, maxIdleFunction, keyGenerator);
    }

    /**
     * 在集群缓存上执行合并后的 {@link MergedUpdate} 操作。
     * <p>
     * 本地实体已在事务内更新，此处仅异步同步到 Infinispan。
     */
    public static <K, V extends SessionEntity> void runOperationInCluster(
            CacheHolder<K, V> cacheHolder,
            K key,
            MergedUpdate<V> task,
            SessionEntityWrapper<V> sessionWrapper,
            AggregateCompletionStage<Void> stage,
            Logger logger
    ) {
        SessionUpdateTask.CacheOperation operation = task.getOperation();

        // 底层实体本地已更新，无需在此 runUpdate
        //task.runUpdate(session);

        switch (operation) {
            case REMOVE:
                // 直接从缓存移除
                stage.dependsOn(CacheDecorators.ignoreReturnValues(cacheHolder.cache()).removeAsync(key));
                break;
            case ADD:
                CompletableFuture<?> future = CacheDecorators.ignoreReturnValues(cacheHolder.cache())
                        .putAsync(key, sessionWrapper, task.getLifespanMs(), TimeUnit.MILLISECONDS, task.getMaxIdleTimeMs(), TimeUnit.MILLISECONDS);
                if (logger.isTraceEnabled()) {
                    future = future.thenRun(() -> logger.tracef("Added entity '%s' to the cache '%s' . Lifespan: %d ms, MaxIdle: %d ms", key, cacheHolder.cache().getName(), task.getLifespanMs(), task.getMaxIdleTimeMs()));
                }
                stage.dependsOn(future);
                break;
            case ADD_IF_ABSENT:
                CompletableFuture<Void> putIfAbsentFuture = cacheHolder.cache().putIfAbsentAsync(key, sessionWrapper, task.getLifespanMs(), TimeUnit.MILLISECONDS, task.getMaxIdleTimeMs(), TimeUnit.MILLISECONDS)
                        .thenCompose(existing -> handlePutIfAbsentResponse(cacheHolder, existing, key, task, logger));
                stage.dependsOn(putIfAbsentFuture);
                break;
            case REPLACE:
                stage.dependsOn(replace(cacheHolder, key, task, sessionWrapper, logger));
                break;
            default:
                throw new IllegalStateException("Unsupported state " + operation);
        }

    }

    private static <K, V extends SessionEntity> CompletionStage<Void> handlePutIfAbsentResponse(
            CacheHolder<K, V> cacheHolder,
            SessionEntityWrapper<V> existing,
            K key,
            MergedUpdate<V> task,
            Logger logger
    ) {
        if (existing == null) {
            if (logger.isTraceEnabled()) {
                logger.tracef("Add_if_absent successfully called for entity '%s' to the cache '%s' . Lifespan: %d ms, MaxIdle: %d ms", key, cacheHolder.cache().getName(), task.getLifespanMs(), task.getMaxIdleTimeMs());
            }
            return CompletableFutures.completedNull();
        }
        if (logger.isDebugEnabled()) {
            logger.debugf("Existing entity in cache for key: %s . Will update it", key);
        }

        // 对已有条目应用更新并 replace
        task.runUpdate(existing.getEntity());

        return replace(cacheHolder, key, task, existing, logger);
    }

    private static <K, V extends SessionEntity> CompletionStage<Void> replace(
            CacheHolder<K, V> cacheHolder,
            K key,
            MergedUpdate<V> task,
            SessionEntityWrapper<V> oldVersionEntity,
            Logger logger) {
        return cacheHolder.sequencer().orderOnKey(key, () -> replaceIteration(cacheHolder.cache(), key, task, null, oldVersionEntity, 0, logger));
    }

    private static <K, V extends SessionEntity> CompletionStage<Void> replaceIteration(
            Cache<K, SessionEntityWrapper<V>> cache,
            K key,
            MergedUpdate<V> task,
            SessionEntityWrapper<V> previousSession,
            SessionEntityWrapper<V> expectedSession,
            int iteration,
            Logger logger
    ) {
        if (iteration >= InfinispanUtil.MAXIMUM_REPLACE_RETRIES) {
            logger.warnf("Failed to replace entity '%s' in cache '%s'. Expected: %s, Current: %s", key, cache.getName(), previousSession, expectedSession);
            return CompletableFutures.completedNull();
        }
        V session = expectedSession.getEntity();
        if (session.shouldEvaluateRemoval() && task.shouldRemove(session)) {
            logger.debugf("Entity %s removed after evaluation", key);
            return CacheDecorators.ignoreReturnValues(cache).removeAsync(key).thenRun(CompletionStages.NO_OP_RUNNABLE);
        }
        SessionEntityWrapper<V> newVersionEntity = new SessionEntityWrapper<>(expectedSession.getLocalMetadata(), session);
        CompletionStage<SessionEntityWrapper<V>> stage = cache.computeIfPresentAsync(key, new ReplaceFunction<>(expectedSession.getVersion(), newVersionEntity), task.getLifespanMs(), TimeUnit.MILLISECONDS, task.getMaxIdleTimeMs(), TimeUnit.MILLISECONDS);
        return stage.thenCompose(rv -> handleReplaceResponse(cache, key, task, expectedSession, newVersionEntity, rv, iteration + 1, logger));
    }

    private static <K, V extends SessionEntity> CompletionStage<Void> handleReplaceResponse(
            Cache<K, SessionEntityWrapper<V>> cache,
            K key,
            MergedUpdate<V> task,
            SessionEntityWrapper<V> expectedSession,
            SessionEntityWrapper<V> newSession,
            SessionEntityWrapper<V> returnValue,
            int iteration,
            Logger logger
    ) {
        if (returnValue == null) {
            logger.debugf("Entity %s not found. Maybe removed in the meantime. Replace task will be ignored", key);
            return CompletableFutures.completedNull();
        }

        if (returnValue.getVersion().equals(newSession.getVersion())) {
            if (logger.isTraceEnabled()) {
                logger.tracef("Replace SUCCESS for entity: %s . old version: %s, new version: %s, Lifespan: %d ms, MaxIdle: %d ms", key, expectedSession.getVersion(), newSession.getVersion(), task.getLifespanMs(), task.getMaxIdleTimeMs());
            }
            return CompletableFutures.completedNull();
        }
        task.runUpdate(returnValue.getEntity());
        return replaceIteration(cache, key, task, expectedSession, returnValue, iteration + 1, logger);
    }
}
