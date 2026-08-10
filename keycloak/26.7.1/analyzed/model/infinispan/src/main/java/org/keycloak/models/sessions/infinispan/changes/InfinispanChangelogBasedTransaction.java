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

package org.keycloak.models.sessions.infinispan.changes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
 * 基于变更日志的 Infinispan 会话事务（纯缓存、非持久化路径）。
 * <p>
 * 事务内累积 {@link SessionUpdateTask}，提交时合并为 {@link MergedUpdate} 并异步写入集群缓存；
 * 支持实体导入、重启及 TRANSIENT 会话跳过缓存写入。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanChangelogBasedTransaction<K, V extends SessionEntity> implements SessionsChangelogBasedTransaction<K, V>, NonBlockingTransaction {

    public static final Logger logger = Logger.getLogger(InfinispanChangelogBasedTransaction.class);

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession kcSession;
    /** 键到待提交更新列表的映射。 */
    protected final Map<K, SessionUpdatesList<V>> updates = new HashMap<>();
    /** 缓存、序列化器与超时计算函数。 */
    protected final CacheHolder<K, V> cacheHolder;

    public InfinispanChangelogBasedTransaction(KeycloakSession kcSession, CacheHolder<K, V> cacheHolder) {
        this.kcSession = kcSession;
        this.cacheHolder = cacheHolder;
    }


    @Override
    public void addTask(K key, SessionUpdateTask<V> task) {
        SessionUpdatesList<V> myUpdates = updates.get(key);
        if (myUpdates != null) {
            myUpdates.addAndExecute(task);
            return;
        }
        lookupAndAndExecuteTask(key, task);
    }

    @Override
    public void restartEntity(K key, SessionUpdateTask<V> restartTask) {
        SessionUpdatesList<V> myUpdates = updates.get(key);
        if (myUpdates != null) {
            myUpdates.getUpdateTasks().clear();
            myUpdates.addAndExecute(restartTask);
            return;
        }
        lookupAndAndExecuteTask(key, restartTask);
    }


    // 创建实体及其首个版本
    public void addTask(K key, SessionUpdateTask<V> task, V entity, UserSessionModel.SessionPersistenceState persistenceState) {
        if (entity == null) {
            throw new IllegalArgumentException("Null entity not allowed");
        }

        RealmModel realm = kcSession.realms().getRealm(entity.getRealmId());
        SessionEntityWrapper<V> wrappedEntity = new SessionEntityWrapper<>(entity);
        SessionUpdatesList<V> myUpdates = new SessionUpdatesList<>(realm, wrappedEntity, persistenceState);
        updates.put(key, myUpdates);

        if (task != null) {
            // 立即执行以便同事务内读者可见
            myUpdates.addAndExecute(task);
        }
    }

    @Deprecated(since = "26.4", forRemoval = true)
    //unused method
    public void reloadEntityInCurrentTransaction(RealmModel realm, K key, SessionEntityWrapper<V> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Null entity not allowed");
        }

        SessionEntityWrapper<V> latestEntity = cacheHolder.cache().get(key);
        if (latestEntity == null) {
            return;
        }

        SessionUpdatesList<V> newUpdates = new SessionUpdatesList<>(realm, latestEntity);

        SessionUpdatesList<V> existingUpdates = updates.get(key);
        if (existingUpdates != null) {
            newUpdates.setUpdateTasks(existingUpdates.getUpdateTasks());
        }

        updates.put(key, newUpdates);
    }


    public SessionEntityWrapper<V> get(K key) {
        SessionUpdatesList<V> myUpdates = updates.get(key);
        if (myUpdates == null) {
            SessionEntityWrapper<V> wrappedEntity = cacheHolder.cache().get(key);
            if (wrappedEntity == null) {
                return null;
            }

            RealmModel realm = kcSession.realms().getRealm(wrappedEntity.getEntity().getRealmId());

            myUpdates = new SessionUpdatesList<>(realm, wrappedEntity);
            updates.put(key, myUpdates);

            return wrappedEntity;
        } else {
            // 已标记删除的实体不再对外返回
            boolean scheduledForRemove = myUpdates.getUpdateTasks().stream()
                    .map(SessionUpdateTask::getOperation)
                    .anyMatch(SessionUpdateTask.CacheOperation.REMOVE::equals);

            return scheduledForRemove ? null : myUpdates.getEntityWrapper();
        }
    }

    @Override
    public void asyncCommit(AggregateCompletionStage<Void> stage, Consumer<DatabaseUpdate> databaseUpdates) {
        for (Map.Entry<K, SessionUpdatesList<V>> entry : updates.entrySet()) {
            SessionUpdatesList<V> sessionUpdates = entry.getValue();
            SessionEntityWrapper<V> sessionWrapper = sessionUpdates.getEntityWrapper();
            List<SessionUpdateTask<V>> updateTasks = sessionUpdates.getUpdateTasks();

            if (updateTasks.isEmpty()) {
                // 无变更，跳过
                continue;
            }

            // TRANSIENT 实体仅存在于当前事务，不写 Infinispan
            if (sessionUpdates.getPersistenceState() == UserSessionModel.SessionPersistenceState.TRANSIENT) continue;

            // 同事务内 ADD_IF_ABSENT 后又 REMOVE 的条目无需写缓存
            if (updateTasks.get(0).getOperation().equals(SessionUpdateTask.CacheOperation.ADD_IF_ABSENT)
                    && updateTasks.get(updateTasks.size() - 1).getOperation().equals(SessionUpdateTask.CacheOperation.REMOVE)) {
                continue;
            }

            RealmModel realm = sessionUpdates.getRealm();

            long lifespanMs = cacheHolder.lifespanFunction().apply(realm, sessionUpdates.getClient(), sessionWrapper.getEntity());
            long maxIdleTimeMs = cacheHolder.maxIdleFunction().apply(realm, sessionUpdates.getClient(), sessionWrapper.getEntity());

            MergedUpdate<V> merged = MergedUpdate.computeUpdate(updateTasks, sessionWrapper, computeLifespan(maxIdleTimeMs, lifespanMs), computeMaxIdle(maxIdleTimeMs, lifespanMs));

            if (merged != null) {
                // 在集群上执行合并后的缓存操作
                InfinispanChangesUtils.runOperationInCluster(cacheHolder, entry.getKey(), merged, sessionWrapper, stage, logger);
            }
        }
    }

    @Override
    public void asyncRollback(AggregateCompletionStage<Void> stage) {
        updates.clear();
    }

    /**
     * @return  backing {@link Cache}。
     */
    public Cache<K, SessionEntityWrapper<V>> getCache() {
        return cacheHolder.cache();
    }

    public K generateKey() {
        assert cacheHolder.keyGenerator() != null;
        return cacheHolder.keyGenerator().get();
    }

    /**
     * 从外部源导入会话到 {@link Cache}。
     * <p>
     * 若缓存中已存在同键会话，返回已有条目且不插入参数中的 session；成功导入时返回 null，
     * 调用方可继续使用参数 session。本事务会继续跟踪后续变更。
     *
     * @param realmModel 会话所属 {@link RealmModel}
     * @param key        缓存键
     * @param session    待导入会话
     * @param lifespan   缓存存活时间（毫秒）
     * @param maxIdle    最大空闲时间（毫秒）
     * @return 已存在的缓存会话；null 表示参数 session 已写入缓存
     */
    public V importSession(RealmModel realmModel, K key, SessionEntityWrapper<V> session, long lifespan, long maxIdle) {
        SessionUpdatesList<V> updatesList = updates.get(key);
        if (updatesList != null) {
            // 事务内已存在，跳过缓存操作
            return updatesList.getEntityWrapper().getEntity();
        }
        SessionEntityWrapper<V> existing = cacheHolder.cache().putIfAbsent(key, session, computeLifespan(maxIdle, lifespan), TimeUnit.MILLISECONDS, computeMaxIdle(maxIdle, lifespan), TimeUnit.MILLISECONDS);
        if (existing == null) {
            // 记录导入会话以便后续更新
            updates.put(key, new SessionUpdatesList<>(realmModel, session));
            return null;
        }
        updates.put(key, new SessionUpdatesList<>(realmModel, existing));
        return existing.getEntity();
    }

    /**
     * 并发批量导入会话到 {@link Cache}。
     * <p>
     * 若 lifespan/maxIdle 函数返回 {@link SessionTimeouts#ENTRY_EXPIRED_FLAG} 则视为已过期不导入；
     * 缓存中已存在的键也不会覆盖。本事务会跟踪所有成功导入的会话。
     *
     * @param realmModel       会话所属 realm
     * @param sessions         键与会话包装器的映射
     * @param lifespanFunction lifespan 计算函数
     * @param maxIdleFunction  max-idle 计算函数
     */
    public void importSessionsConcurrently(RealmModel realmModel, Map<K, SessionEntityWrapper<V>> sessions, SessionFunction<V> lifespanFunction, SessionFunction<V> maxIdleFunction) {
        if (sessions.isEmpty()) {
            // 无待导入项
            return;
        }
        var stage = CompletionStages.aggregateCompletionStage();
        var allSessions = new ConcurrentHashMap<K, SessionEntityWrapper<V>>();
        sessions.forEach((key, session) -> {
            if (updates.containsKey(key)) {
                // 事务内已有，无需导入
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
            var future = cacheHolder.cache().putIfAbsentAsync(key, session, computeLifespan(maxIdle, lifespan), TimeUnit.MILLISECONDS, computeMaxIdle(maxIdle, lifespan), TimeUnit.MILLISECONDS);
            // 回调可能在不同线程执行，用并发 Map 收集结果
            stage.dependsOn(future.thenAccept(existing -> allSessions.put(key, existing == null ? session : existing)));
        });

        CompletionStages.join(stage.freeze());
        allSessions.forEach((key, wrapper) -> updates.put(key, new SessionUpdatesList<>(realmModel, wrapper)));
    }

    private void lookupAndAndExecuteTask(K key, SessionUpdateTask<V> task) {
        // 从缓存加载实体
        SessionEntityWrapper<V> wrappedEntity = cacheHolder.cache().get(key);
        if (wrappedEntity == null) {
            logger.tracef("Not present cache item for key %s", key);
            return;
        }

        RealmModel realm = kcSession.realms().getRealm(wrappedEntity.getEntity().getRealmId());

        SessionUpdatesList<V> myUpdates = new SessionUpdatesList<>(realm, wrappedEntity);
        updates.put(key, myUpdates);

        // 立即执行以便同事务读者可见（回滚行为待验证）
        myUpdates.addAndExecute(task);
    }

    protected long computeLifespan(long maxIdle, long lifespan) {
        return lifespan;
    }

    protected long computeMaxIdle(long maxIdle, long lifespan) {
        return maxIdle;
    }
}
