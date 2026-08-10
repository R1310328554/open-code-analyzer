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

package org.keycloak.models.sessions.infinispan.changes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.SessionFunction;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;
import org.keycloak.models.sessions.infinispan.transaction.DatabaseUpdate;
import org.keycloak.models.sessions.infinispan.transaction.NonBlockingTransaction;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.AggregateCompletionStage;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.jboss.logging.Logger;

/**
 * 持久化会话的变更日志事务抽象基类。
 * <p>
 * 同时维护在线与离线两套缓存的待提交变更，提交时异步刷新 Infinispan 并通过 {@link JpaChangesPerformer} 写入数据库。
 *
 * @param <K> 缓存键类型
 * @param <V> 会话实体类型
 */
abstract public class PersistentSessionsChangelogBasedTransaction<K, V extends SessionEntity> implements SessionsChangelogBasedTransaction<K, V>, NonBlockingTransaction {

    private static final Logger LOG = Logger.getLogger(PersistentSessionsChangelogBasedTransaction.class);
    protected final KeycloakSession kcSession;
    /** 在线会话在本事务内的变更列表。 */
    protected final Map<K, SessionUpdatesList<V>> updates = new HashMap<>();
    /** 离线会话在本事务内的变更列表。 */
    protected final Map<K, SessionUpdatesList<V>> offlineUpdates = new HashMap<>();
    private final String cacheName;
    private final CacheHolder<K, V> cacheHolder;
    private final CacheHolder<K, V> offlineCacheHolder;

    public PersistentSessionsChangelogBasedTransaction(KeycloakSession session,
                                                       String cacheName,
                                                       CacheHolder<K, V> cacheHolder,
                                                       CacheHolder<K, V> offlineCacheHolder) {
        kcSession = session;
        this.cacheName = cacheName;
        this.cacheHolder = cacheHolder;
        this.offlineCacheHolder = offlineCacheHolder;
    }

    public Cache<K, SessionEntityWrapper<V>> getCache(boolean offline) {
        return offline ? offlineCacheHolder.cache() : cacheHolder.cache();
    }

    protected SessionFunction<V> getLifespanMsLoader(boolean offline) {
        return offline ? offlineCacheHolder.lifespanFunction() : cacheHolder.lifespanFunction();
    }

    protected SessionFunction<V> getMaxIdleMsLoader(boolean offline) {
        return offline ? offlineCacheHolder.maxIdleFunction() : cacheHolder.maxIdleFunction();
    }

    protected Map<K, SessionUpdatesList<V>> getUpdates(boolean offline) {
        return offline ? offlineUpdates : updates;
    }

    public K generateKey() {
        assert cacheHolder.keyGenerator() != null;
        return cacheHolder.keyGenerator().get();
    }

    public SessionEntityWrapper<V> get(K key, boolean offline) {
        SessionUpdatesList<V> myUpdates = getUpdates(offline).get(key);
        if (myUpdates == null) {
            SessionEntityWrapper<V> wrappedEntity = getCache(offline).get(key);
            if (wrappedEntity == null) {
                return null;
            }
            wrappedEntity.getEntity().setOffline(offline);

            RealmModel realm = kcSession.realms().getRealm(wrappedEntity.getEntity().getRealmId());

            myUpdates = new SessionUpdatesList<>(realm, wrappedEntity);
            getUpdates(offline).put(key, myUpdates);

            return wrappedEntity;
        } else {
            // 若已调度删除，则对事务内读者不可见
            boolean scheduledForRemove = myUpdates.getUpdateTasks().stream()
                    .map(SessionUpdateTask::getOperation)
                    .anyMatch(SessionUpdateTask.CacheOperation.REMOVE::equals);

            return scheduledForRemove ? null : myUpdates.getEntityWrapper();
        }
    }

    @Override
    public boolean supportsLockingDatabaseEntities() {
        return true;
    }

    @Override
    public boolean lockDatabaseEntities() {
        for (Map.Entry<K, SessionUpdatesList<V>> entry : Stream.concat(updates.entrySet().stream(), offlineUpdates.entrySet().stream()).toList()) {
            SessionUpdatesList<V> sessionUpdates = entry.getValue();
            if (sessionUpdates.getUpdateTasks().isEmpty()) {
                continue;
            }
            SessionEntityWrapper<V> sessionWrapper = sessionUpdates.getEntityWrapper();
            V entity = sessionWrapper.getEntity();
            boolean isOffline = entity.isOffline();

            // 瞬态会话仅存在于当前事务，不写入 Infinispan 或数据库
            if (sessionUpdates.getPersistenceState() == UserSessionModel.SessionPersistenceState.TRANSIENT) continue;

            RealmModel realm = sessionUpdates.getRealm();

            long lifespanMs = getLifespanMsLoader(isOffline).apply(realm, sessionUpdates.getClient(), entity);
            long maxIdleTimeMs = getMaxIdleMsLoader(isOffline).apply(realm, sessionUpdates.getClient(), entity);

            MergedUpdate<V> merged = MergedUpdate.computeUpdate(sessionUpdates.getUpdateTasks(), sessionWrapper, SessionTimeouts.calculateEffectiveSessionLifespan(maxIdleTimeMs, lifespanMs), SessionTimeouts.IMMORTAL_FLAG);

            if (merged == null) {
                continue;
            }

            if (!lockDatabaseEntity(realm, entry.getKey(), entity.isOffline(), merged.getOperation())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在数据库中锁定实体，防止并发提交冲突。
     */
    protected abstract boolean lockDatabaseEntity(RealmModel realm, K key, boolean offline, SessionUpdateTask.CacheOperation operation);

    @Override
    public void asyncCommit(AggregateCompletionStage<Void> stage, Consumer<DatabaseUpdate> databaseUpdates) {
        JpaChangesPerformer<K, V> persister = null;
        for (Map.Entry<K, SessionUpdatesList<V>> entry : Stream.concat(updates.entrySet().stream(), offlineUpdates.entrySet().stream()).toList()) {
            SessionUpdatesList<V> sessionUpdates = entry.getValue();
            if (sessionUpdates.getUpdateTasks().isEmpty()) {
                continue;
            }
            SessionEntityWrapper<V> sessionWrapper = sessionUpdates.getEntityWrapper();
            V entity = sessionWrapper.getEntity();
            boolean isOffline = entity.isOffline();

            // 瞬态会话仅存在于当前事务，不写入 Infinispan 或数据库
            if (sessionUpdates.getPersistenceState() == UserSessionModel.SessionPersistenceState.TRANSIENT) continue;

            RealmModel realm = sessionUpdates.getRealm();

            long lifespanMs = getLifespanMsLoader(isOffline).apply(realm, sessionUpdates.getClient(), entity);
            long maxIdleTimeMs = getMaxIdleMsLoader(isOffline).apply(realm, sessionUpdates.getClient(), entity);

            MergedUpdate<V> merged = MergedUpdate.computeUpdate(sessionUpdates.getUpdateTasks(), sessionWrapper, SessionTimeouts.calculateEffectiveSessionLifespan(maxIdleTimeMs, lifespanMs), SessionTimeouts.IMMORTAL_FLAG);

            if (merged != null) {
                var c = isOffline ? offlineCacheHolder : cacheHolder;
                if (c.cache() != null) {
                    // 非阻塞方式更新集群缓存
                    InfinispanChangesUtils.runOperationInCluster(c, entry.getKey(), merged, entry.getValue().getEntityWrapper(), stage, LOG);
                }

                if (persister == null) {
                    persister = new JpaChangesPerformer<>(cacheName);
                    databaseUpdates.accept(persister::write);
                }
                persister.registerChange(entry, merged);
            }
        }
    }

    @Override
    public void asyncRollback(AggregateCompletionStage<Void> stage) {
        updates.clear();
        offlineUpdates.clear();
    }

    @Override
    public void addTask(K key, SessionUpdateTask<V> originalTask) {
        if (!(originalTask instanceof PersistentSessionUpdateTask<V> task)) {
            throw new IllegalArgumentException("Task must be instance of PersistentSessionUpdateTask");
        }

        SessionUpdatesList<V> myUpdates = getUpdates(task.isOffline()).get(key);
        if (myUpdates != null) {
            myUpdates.addAndExecute(task);
            return;
        }
        lookupAndAndExecuteTask(key, task);
    }

    @Override
    public void restartEntity(K key, SessionUpdateTask<V> restartTask) {
        if (!(restartTask instanceof PersistentSessionUpdateTask<V> task)) {
            throw new IllegalArgumentException("Task must be instance of PersistentSessionUpdateTask");
        }
        var myUpdates = getUpdates(task.isOffline()).get(key);
        if (myUpdates != null) {
            myUpdates.getUpdateTasks().clear();
            myUpdates.addAndExecute(task);
            return;
        }
        lookupAndAndExecuteTask(key, task);
    }

    private void lookupAndAndExecuteTask(K key, PersistentSessionUpdateTask<V> task) {
        // 从缓存加载实体以开始跟踪变更
        SessionEntityWrapper<V> wrappedEntity = getCache(task.isOffline()).get(key);
        if (wrappedEntity == null) {
            LOG.tracef("Not present cache item for key %s", key);
            return;
        }
        // 缓存条目不含 offline 标志，需手动补全
        wrappedEntity.getEntity().setOffline(task.isOffline());

        RealmModel realm = kcSession.realms().getRealm(wrappedEntity.getEntity().getRealmId());

        SessionUpdatesList<V> myUpdates = new SessionUpdatesList<>(realm, wrappedEntity);
        getUpdates(task.isOffline()).put(key, myUpdates);

        // 立即执行以便同事务内后续读操作可见（回滚行为待验证）
        myUpdates.addAndExecute(task);
    }

    public void addTask(K key, SessionUpdateTask<V> task, V entity, UserSessionModel.SessionPersistenceState persistenceState) {
        if (entity == null) {
            throw new IllegalArgumentException("Null entity not allowed");
        }

        RealmModel realm = kcSession.realms().getRealm(entity.getRealmId());
        SessionEntityWrapper<V> wrappedEntity = new SessionEntityWrapper<>(entity);
        SessionUpdatesList<V> myUpdates = new SessionUpdatesList<>(realm, wrappedEntity, persistenceState);
        getUpdates(entity.isOffline()).put(key, myUpdates);

        if (task != null) {
            // 立即执行以便同事务内后续读操作可见
            myUpdates.addAndExecute(task);
        }
    }

    // 当前未使用，计划在下一主版本移除
    @Deprecated(forRemoval = true, since = "26.4")
    public void reloadEntityInCurrentTransaction(RealmModel realm, K key, SessionEntityWrapper<V> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Null entity not allowed");
        }
        boolean offline = entity.getEntity().isOffline();

        SessionEntityWrapper<V> latestEntity = getCache(offline).get(key);
        if (latestEntity == null) {
            return;
        }

        SessionUpdatesList<V> newUpdates = new SessionUpdatesList<>(realm, latestEntity);

        SessionUpdatesList<V> existingUpdates = getUpdates(entity.getEntity().isOffline()).get(key);
        if (existingUpdates != null) {
            newUpdates.setUpdateTasks(existingUpdates.getUpdateTasks());
        }

        getUpdates(entity.getEntity().isOffline()).put(key, newUpdates);
    }

    /**
     * Imports a session from an external source into the {@link Cache}.
     * <p>
     * If a session already exists in the cache, this method does not insert the {@code session}. The invoker should use
     * the session returned by this method invocation. When the session is successfully imported, this method returns
     * null and the {@code session} can be used by the transaction.
     * <p>
     * This transaction will keep track of further changes in the session.
     *
     * @param realmModel The {@link RealmModel} where the session belong to.
     * @param key        The cache's key.
     * @param session    The session to import.
     * @param lifespan   How long the session stays cached until it is expired and removed.
     * @param maxIdle    How long the session can be idle (without reading or writing) before being removed.
     * @param offline    {@code true} if it is an offline session.
     * @return The existing cached session. If it returns {@code null}, it means the {@code session} used in the
     * parameters was cached.
     */
    public SessionEntityWrapper<V> importSession(RealmModel realmModel, K key, SessionEntityWrapper<V> session, boolean offline, long lifespan, long maxIdle) {
        var updates = getUpdates(offline);
        var updatesList = updates.get(key);
        if (updatesList != null) {
            // 事务内已跟踪，跳过导入
            return updatesList.getEntityWrapper();
        }
        SessionEntityWrapper<V> existing = null;
        try {
            if (getCache(offline) != null) {
                existing = getCache(offline).putIfAbsent(key, session, SessionTimeouts.calculateEffectiveSessionLifespan(maxIdle, lifespan), TimeUnit.MILLISECONDS);
            }
        } catch (RuntimeException exception) {
            // 导入失败时仍可从数据库继续，不中断事务
            LOG.debugf(exception, "Failed to import session %s", session);
        }
        if (existing == null) {
            // 记录已导入会话以便后续变更跟踪
            updates.put(key, new SessionUpdatesList<>(realmModel, session));
            return null;
        }
        updates.put(key, new SessionUpdatesList<>(realmModel, existing));
        return existing;
    }

    /**
     * Imports multiple sessions from an external source into the {@link Cache}.
     * <p>
     * If one or more sessions already exist in the {@link Cache}, or is expired, it will not be imported.
     * <p>
     * This transaction will keep track of further changes in the sessions.
     *
     * @param realmModel The {@link RealmModel} where the sessions belong to.
     * @param sessions   The {@link Map} with the cache's key/session mapping to be imported.
     * @param offline    {@code true} if it is an offline session.
     */
    public void importSessionsConcurrently(RealmModel realmModel, Map<K, SessionEntityWrapper<V>> sessions, boolean offline) {
        var cache = getCache(offline);
        if (sessions.isEmpty() || cache == null) {
            // 无需导入
            return;
        }
        var stage = CompletionStages.aggregateCompletionStage();
        var allSessions = new ConcurrentHashMap<K, SessionEntityWrapper<V>>();
        var updates = getUpdates(offline);
        var lifespanFunction = getLifespanMsLoader(offline);
        var maxIdleFunction = getMaxIdleMsLoader(offline);
        sessions.forEach((key, session) -> {
            if (updates.containsKey(key)) {
                // 无需导入, already exists in transaction
                return;
            }
            var clientModel = session.getClientIfNeeded(realmModel);
            var sessionEntity = session.getEntity();
            var lifespan = lifespanFunction.apply(realmModel, clientModel, sessionEntity);
            var maxIdle = maxIdleFunction.apply(realmModel, clientModel, sessionEntity);
            if (lifespan == SessionTimeouts.ENTRY_EXPIRED_FLAG || maxIdle == SessionTimeouts.ENTRY_EXPIRED_FLAG) {
                // 已过期，跳过导入
                return;
            }
            var future = cache.putIfAbsentAsync(key, session, SessionTimeouts.calculateEffectiveSessionLifespan(maxIdle, lifespan), TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        // 导入失败时仍可从数据库继续，不中断事务
                        LOG.debugf(throwable, "Failed to import session %s", session);
                        return null;
                    });
            // 回调可能在不同线程执行，用并发 Map 汇总结果
            stage.dependsOn(future.thenAccept(existing -> allSessions.put(key, existing == null ? session : existing)));
        });

        CompletionStages.join(stage.freeze());
        allSessions.forEach((key, wrapper) -> updates.put(key, new SessionUpdatesList<>(realmModel, wrapper)));
    }
}
