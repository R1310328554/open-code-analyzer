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

package org.keycloak.cluster.infinispan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterListener;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ExecutionResult;
import org.keycloak.common.util.ConcurrentMultivaluedHashMap;
import org.keycloak.common.util.Retry;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.connections.infinispan.NodeInfo;
import org.keycloak.models.sessions.infinispan.CacheDecorators;

import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.jboss.logging.Logger;

/**
 * 基于嵌入式 Infinispan work 缓存的集群提供者实现。
 * <p>
 * 提供分布式锁（{@link #executeIfNotExecuted}）、集群事件通知（{@link #notify}）
 * 及跨数据中心（Cross-DC）站点过滤，通过缓存条目创建/移除事件驱动 listener 回调。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanClusterProvider implements ClusterProvider {

    protected static final Logger logger = Logger.getLogger(InfinispanClusterProvider.class);

    /** work 缓存中存储集群启动时间的键。 */
    public static final String CLUSTER_STARTUP_TIME_KEY = "cluster-start-time";
    /** 分布式任务锁条目的缓存键前缀。 */
    public static final String TASK_KEY_PREFIX = "task::";

    /** 集群启动时间（秒级 Unix 时间戳）。 */
    private final int clusterStartupTime;
    /** 本节点与站点元数据。 */
    private final NodeInfo nodeInfo;
    /** 用于锁与事件传递的 Infinispan work 缓存。 */
    private final Cache<String, Object> workCache;
    /** 按 taskKey 注册的集群事件监听器（支持多 listener）。 */
    private final ConcurrentMultivaluedHashMap<String, ClusterListener> listeners = new ConcurrentMultivaluedHashMap<>();
    /** 异步任务的完成回调映射（taskKey → TaskCallback）。 */
    private final ConcurrentMap<String, TaskCallback> taskCallbacks = new ConcurrentHashMap<>();

    /** 本地线程池，用于异步任务包装与等待。 */
    private final ExecutorService localExecutor;

    /**
     * @param clusterStartupTime 集群启动时间
     * @param nodeInfo           节点信息
     * @param workCache          work 缓存实例
     * @param localExecutor      本地执行器
     */
    public InfinispanClusterProvider(int clusterStartupTime, NodeInfo nodeInfo, Cache<String, Object> workCache, ExecutorService localExecutor) {
        this.nodeInfo = nodeInfo;
        this.clusterStartupTime = clusterStartupTime;
        this.workCache = workCache;
        this.localExecutor = localExecutor;
    }


    @Override
    public int getClusterStartupTime() {
        return clusterStartupTime;
    }


    @Override
    public void close() {
    }


    /** 集群级互斥执行：仅首个获得 work 缓存锁的节点运行 task。 */
    @Override
    public <T> ExecutionResult<T> executeIfNotExecuted(String taskKey, int taskTimeoutInSeconds, Callable<T> task) {
        String cacheKey = TASK_KEY_PREFIX + taskKey;
        boolean locked = tryLock(cacheKey, taskTimeoutInSeconds);
        if (locked) {
            try {
                try {
                    T result = task.call();
                    return ExecutionResult.executed(result);
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected exception when executed task " + taskKey, e);
                }
            } finally {
                removeFromCache(cacheKey);
            }
        } else {
            return ExecutionResult.notExecuted();
        }
    }


    /** 异步版集群互斥：若其他节点已在执行则等待其完成。 */
    @Override
    public Future<Boolean> executeIfNotExecutedAsync(String taskKey, int taskTimeoutInSeconds, Callable task) {
        TaskCallback newCallback = new TaskCallback();
        TaskCallback callback = registerTaskCallback(TASK_KEY_PREFIX + taskKey, newCallback);

        // 成功提交任务
        if (newCallback == callback) {
            Callable<Boolean> wrappedTask = () -> {
                boolean executed = executeIfNotExecuted(taskKey, taskTimeoutInSeconds, task).isExecuted();

                if (!executed) {
                    logger.infof("Task already in progress on other cluster node. Will wait until it's finished");
                }

                callback.getTaskCompletedLatch().await(taskTimeoutInSeconds, TimeUnit.SECONDS);
                return callback.isSuccess();
            };

            Future<Boolean> future = localExecutor.submit(wrappedTask);
            callback.setFuture(future);
        } else {
            logger.infof("Task already in progress on this cluster node. Will wait until it's finished");
        }

        return callback.getFuture();
    }

    /** 注册或返回已有的 TaskCallback（putIfAbsent 语义）。 */
    TaskCallback registerTaskCallback(String taskKey, TaskCallback callback) {
        TaskCallback existing = taskCallbacks.putIfAbsent(taskKey, callback);
        return existing == null ? callback : existing;
    }

    /** 通过 work 缓存 putIfAbsent 尝试获取分布式锁。 */
    private boolean tryLock(String cacheKey, int taskTimeoutInSeconds) {
        LockEntry myLock = new LockEntry(nodeInfo.nodeName());

        LockEntry existingLock = (LockEntry) workCache.putIfAbsent(cacheKey, myLock, Time.toMillis(taskTimeoutInSeconds), TimeUnit.MILLISECONDS);
        if (existingLock != null) {
            if (logger.isTraceEnabled()) {
                logger.tracef("Task %s in progress already by node %s. Ignoring task.", cacheKey, existingLock.node());
            }
            return false;
        } else {
            if (logger.isTraceEnabled()) {
                logger.tracef("Successfully acquired lock for task %s. Our node is %s", cacheKey, myLock.node());
            }
            return true;
        }
    }

    /** 带退避重试地从 work 缓存移除锁条目（应对节点故障）。 */
    private void removeFromCache(String cacheKey) {
        // 多次尝试移除（期间可能有节点故障）
        Retry.executeWithBackoff((int iteration) -> {

            CacheDecorators.ignoreReturnValues(workCache).remove(cacheKey);
            if (logger.isTraceEnabled()) {
                logger.tracef("Task %s removed from the cache", cacheKey);
            }

        }, 10, 10);
    }

    @Override
    public void registerListener(String taskKey, ClusterListener task) {
        this.listeners.add(taskKey, task);
    }

    @Override
    public void notify(String taskKey, ClusterEvent event, boolean ignoreSender, DCNotify dcNotify) {
        notify(taskKey, Collections.singleton(event), ignoreSender, dcNotify);
    }

    public void notify(String taskKey, Collection<? extends ClusterEvent> events, boolean ignoreSender) {
        notify(taskKey, events, ignoreSender, DCNotify.ALL_DCS);
    }

    /** 将集群事件包装后写入 work 缓存，触发各节点的 CacheEntryListener。 */
    @Override
    public void notify(String taskKey, Collection<? extends ClusterEvent> events, boolean ignoreSender, DCNotify dcNotify) {
        if (events == null || events.isEmpty()) {
            return;
        }
        var wrappedEvent = WrapperClusterEvent.wrap(taskKey, events, nodeInfo.nodeName(), nodeInfo.siteName(), dcNotify, ignoreSender);

        String eventKey = SecretGenerator.getInstance().generateSecureID();

        if (logger.isTraceEnabled()) {
            logger.tracef("Sending event with key %s: %s", eventKey, events);
        }

        CacheDecorators.ignoreReturnValues(workCache)
              .put(eventKey, wrappedEvent, 120, TimeUnit.SECONDS);
    }

    /**
     * work 缓存条目监听器：接收集群事件并在锁条目移除时通知 TaskCallback。
     */
    @Listener(observation = Listener.Observation.POST)
    public class CacheEntryListener {

        @CacheEntryCreated
        public void cacheEntryCreated(CacheEntryCreatedEvent<String, Object> event) {
            eventReceived(event.getKey(), event.getValue());
        }

        @CacheEntryModified
        public void cacheEntryModified(CacheEntryModifiedEvent<String, Object> event) {
            eventReceived(event.getKey(), event.getNewValue());
        }

        @CacheEntryRemoved
        public void cacheEntryRemoved(CacheEntryRemovedEvent<String, Object> event) {
            taskFinished(event.getKey());
        }
    }

    /** 处理收到的缓存事件：过滤后分发给已注册 listener。 */
    private void eventReceived(String key, Object obj) {
        if (!(obj instanceof WrapperClusterEvent event)) {
            // TASK_KEY_PREFIX 条目在锁释放后很快消失，不必记录；真实事件为 null 时仍保留告警
            if (obj == null && !key.startsWith(TASK_KEY_PREFIX)) {
                logger.warnf("Event object wasn't available in remote cache after event was received. Event key: %s", key);
            }
            return;
        }

        if (event.rejectEvent(nodeInfo.nodeName(), nodeInfo.siteName())) {
            return;
        }

        String eventKey = event.getEventKey();

        if (logger.isTraceEnabled()) {
            logger.tracef("Received event: %s", event);
        }

        List<ClusterListener> myListeners = listeners.get(eventKey);
        if (myListeners != null) {
            for (var e : event.getDelegateEvents()) {
                myListeners.forEach(e);
            }
        }
    }


    /** 任务锁条目被移除时，通知对应 TaskCallback 任务已完成。 */
    void taskFinished(String taskKey) {
        TaskCallback callback = taskCallbacks.remove(taskKey);

        if (callback != null) {
            if (logger.isDebugEnabled()) {
                logger.debugf("Finished task '%s' with '%b'", taskKey, true);
            }
            callback.setSuccess(true);
            callback.getTaskCompletedLatch().countDown();
        }
    }
}
