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

package org.keycloak.cluster;


import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.keycloak.provider.Provider;

/**
 * 集群工具提供者：协调多节点并发任务、监听器注册与跨数据中心事件通知。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClusterProvider extends Provider {

    /**
     * 返回所有集群节点一致的启动时间戳；非集群环境下为本机启动时间。
     *
     * Same value for all cluster nodes. It will use startup time of this server in non-cluster environment.
     */
    int getClusterStartupTime();


    /**
     * 仅当指定任务尚未在本节点或其他节点执行时运行；通过 {@link ExecutionResult} 返回是否实际执行。
     *
     * Execute given task just if it's not already in progress (either on this or any other cluster node).
     *
     * @param taskKey
     * @param taskTimeoutInSeconds timeout for given task. If there is existing task in progress for longer time, it's considered outdated so we will start our task.
     * @param task
     * @param <T>
     * @return result with "executed" flag specifying if execution was executed or ignored.
     */
    <T> ExecutionResult<T> executeIfNotExecuted(String taskKey, int taskTimeoutInSeconds, Callable<T> task);


    /**
     * 与 {@link #executeIfNotExecuted} 类似，但返回 {@link Future}；任务成功完成时为 {@code true}，节点故障等导致失败时为 {@code false}。
     *
     * Execute given task just if it's not already in progress (either on this or any other cluster node). It will return corresponding future to every caller and this future is fulfilled if:
     * - The task is successfully finished. In that case Future will be true
     * - The task wasn't successfully finished. For example because cluster node failover. In that case Future will be false
     *
     * @param taskKey
     * @param taskTimeoutInSeconds timeout for given task. If there is existing task in progress for longer time, it's considered outdated so we will start our task.
     * @param task
     * @return Future, which will be completed once the running task is finished. Returns true if task was successfully finished. Otherwise (for example if cluster node when task was running leaved cluster) returns false
     */
    Future<Boolean> executeIfNotExecutedAsync(String taskKey, int taskTimeoutInSeconds, Callable task);


    /**
     * 在指定 taskKey 下注册 {@link ClusterListener}；任意节点向缓存写入该键时触发监听器。
     *
     * Register task (listener) under given key. When this key will be put to the cache on any cluster node, the task will be executed.
     *
     * @param taskKey
     * @param task
     */
    void registerListener(String taskKey, ClusterListener task);


    /**
     * 向所有数据中心的所有集群节点广播事件，通知注册在 taskKey 下的监听器。
     *
     * Notify registered listeners on all cluster nodes in all datacenters. It will notify listeners registered under given taskKey
     *
     * @param taskKey
     * @param event
     * @param ignoreSender if true, then sender node itself won't receive the notification
     * @param dcNotify Specify which DCs to notify. See {@link DCNotify} enum values for more info
     * @deprecated use {@link ClusterProvider#notify(String, ClusterEvent, boolean)} instead. {@link DCNotify} is deprecated. For removal in Keycloak 27.
     */
    @Deprecated(since = "26.3", forRemoval = true)
    void notify(String taskKey, ClusterEvent event, boolean ignoreSender, DCNotify dcNotify);

    default void notify(String taskKey, ClusterEvent event, boolean ignoreSender) {
        notify(taskKey, event, ignoreSender, DCNotify.ALL_DCS);
    }

    /**
     * 批量通知的替代方案：在一次网络调用中发送多个 {@link ClusterEvent}。
     *
     * An alternative to {@link #notify(String, ClusterEvent, boolean, DCNotify)} that sends multiple events in a single
     * network call.
     * <p>
     * Notifies registered listeners on all cluster nodes in all datacenters. It will notify listeners registered under
     * given {@code taskKey}
     *
     * @see #notify(String, ClusterEvent, boolean, DCNotify)
     * @deprecated use {@link ClusterProvider#notify(String, Collection, boolean)} instead. {@link DCNotify} is deprecated. For removal in Keycloak 27.
     */
    @Deprecated(since = "26.3", forRemoval = true)
    default void notify(String taskKey, Collection<? extends ClusterEvent> events, boolean ignoreSender, DCNotify dcNotify) {
        events.forEach(event -> notify(taskKey, event, ignoreSender, dcNotify));
    }

    default void notify(String taskKey, Collection<? extends ClusterEvent> events, boolean ignoreSender) {
        events.forEach(event -> notify(taskKey, event, ignoreSender, DCNotify.ALL_DCS));
    }

    /**
     * 仅用于已弃用方法；使用本枚举的方法 JavaDoc 中均给出了替代方案。
     *
     * This is now used only in deprecated methods.
     * All methods that are using this enum have a Javadoc suggesting alternative.
     * @deprecated For removal in Keycloak 27.
     */
    @Deprecated(since = "26.3", forRemoval = true)
    enum DCNotify {
        /** 向所有数据中心的所有集群节点发送消息。 **/
        ALL_DCS,

        /** 仅向本数据中心的所有集群节点发送消息。 **/
        LOCAL_DC_ONLY,

        /** 向除本数据中心外的所有数据中心广播；{@link #notify} 的 ignoreSender 将被忽略。 **/
         ALL_BUT_LOCAL_DC
    }

    /**
     * 判断当前节点是否为主集群协调者。
     *
     * Checks if the current cluster is the primary cluster.
     *
     * @return {@code true} if this node is the coordinator.
     */
    default boolean isPrimaryCluster() {
        return false;
    }

    /**
     * 判断是否支持主集群检测（并非所有配置都使用 JDBC_PING）。
     *
     * Checks if the primary cluster check is supported.
     * <p>
     * Not all configurations use JDBC_PING and can determine this information.
     *
     * @return {@code true} if the coordinator check is supported.
     */
    default boolean isPrimaryClusterSupported() {
        return false;
    }

}
