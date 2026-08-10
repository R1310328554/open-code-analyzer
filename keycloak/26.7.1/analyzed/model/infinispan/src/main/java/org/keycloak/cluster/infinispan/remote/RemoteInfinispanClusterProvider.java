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

package org.keycloak.cluster.infinispan.remote;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterListener;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ExecutionResult;
import org.keycloak.cluster.infinispan.LockEntry;
import org.keycloak.cluster.infinispan.TaskCallback;
import org.keycloak.common.util.Retry;

import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;

import static org.keycloak.cluster.infinispan.InfinispanClusterProvider.TASK_KEY_PREFIX;
import static org.keycloak.cluster.infinispan.remote.RemoteInfinispanClusterProviderFactory.putIfAbsentWithRetries;

/**
 * 基于远程 Infinispan（Hot Rod）的集群提供者实现。
 * <p>
 * 与嵌入式 {@link InfinispanClusterProvider} 语义相同，但通过 Hot Rod 客户端访问远端 work 缓存，
 * 事件通知委托给 {@link RemoteInfinispanNotificationManager} 处理。
 */
public class RemoteInfinispanClusterProvider implements ClusterProvider {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    /** 工厂级共享数据（缓存、通知管理器、执行器等）。 */
    private final SharedData data;

    public RemoteInfinispanClusterProvider(SharedData data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public int getClusterStartupTime() {
        return data.clusterStartupTime();
    }

    /** 集群级互斥执行（Hot Rod putIfAbsent 实现分布式锁）。 */
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

    /** 异步版集群互斥，通过 NotificationManager 协调 TaskCallback。 */
    @Override
    public Future<Boolean> executeIfNotExecutedAsync(String taskKey, int taskTimeoutInSeconds, Callable task) {
        TaskCallback newCallback = new TaskCallback();
        TaskCallback callback = data.notificationManager().registerTaskCallback(TASK_KEY_PREFIX + taskKey, newCallback);

        // 成功提交任务
        if (newCallback == callback) {
            Supplier<Boolean> wrappedTask = () -> {
                boolean executed = executeIfNotExecuted(taskKey, taskTimeoutInSeconds, task).isExecuted();

                if (!executed) {
                    logger.infof("Task already in progress on other cluster node. Will wait until it's finished");
                }

                try {
                    callback.getTaskCompletedLatch().await(taskTimeoutInSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return callback.isSuccess();
            };

            callback.setFuture(CompletableFuture.supplyAsync(wrappedTask, data.executor()));
        } else {
            logger.infof("Task already in progress on this cluster node. Will wait until it's finished");
        }

        return callback.getFuture();
    }

    @Override
    public void registerListener(String taskKey, ClusterListener task) {
        data.notificationManager().registerListener(taskKey, task);
    }

    @Override
    public void notify(String taskKey, ClusterEvent event, boolean ignoreSender, DCNotify dcNotify) {
        data.notificationManager().notify(taskKey, Collections.singleton(event), ignoreSender, dcNotify);
    }

    @Override
    public void notify(String taskKey, Collection<? extends ClusterEvent> events, boolean ignoreSender) {
        data.notificationManager().notify(taskKey, events, ignoreSender, DCNotify.ALL_DCS);
    }

    /** 集群事件通知委托给 NotificationManager。 */
    @Override
    public void notify(String taskKey, Collection<? extends ClusterEvent> events, boolean ignoreSender, DCNotify dcNotify) {
        data.notificationManager().notify(taskKey, events, ignoreSender, dcNotify);
    }

    @Override
    public void close() {

    }

    /** 通过 Hot Rod putIfAbsent 尝试获取分布式锁。 */
    private boolean tryLock(String cacheKey, int taskTimeoutInSeconds) {
        LockEntry myLock = createLockEntry();

        LockEntry existingLock = putIfAbsentWithRetries(data.cache(), cacheKey, myLock, taskTimeoutInSeconds);
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

    /** 创建包含本节点名称的 LockEntry。 */
    private LockEntry createLockEntry() {
        return new LockEntry(data.notificationManager().getMyNodeName());
    }

    /** 带退避重试地从远端缓存移除锁条目。 */
    private void removeFromCache(String cacheKey) {
        // 多次尝试移除（期间可能有节点故障）
        Retry.executeWithBackoff((int iteration) -> {
            data.cache().remove(cacheKey);
            if (logger.isTraceEnabled()) {
                logger.tracef("Task %s removed from the cache", cacheKey);
            }
        }, 10, 10);
    }

    /** 工厂与 Provider 共享的运行时数据接口。 */
    public interface SharedData {
        /** 集群启动时间（秒）。 */
        int clusterStartupTime();
        /** 用于分布式锁的 Hot Rod 缓存。 */
        RemoteCache<String, LockEntry> cache();
        /** 集群事件通知管理器。 */
        RemoteInfinispanNotificationManager notificationManager();
        /** 异步任务执行器。 */
        Executor executor();
    }
}
