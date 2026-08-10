/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.config.server.Config;
import com.alibaba.nacos.core.utils.ClassUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置模块统一线程池门面：集中管理定时任务、异步通知、长轮询、容量校正与插件回调等 {@link java.util.concurrent.ScheduledExecutorService} 实例。
 * Config executor.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class ConfigExecutor {
    
    /** 通用配置定时任务线程池（8 线程） */
    private static final ScheduledExecutorService TIMER_EXECUTOR =
        ExecutorFactory.Managed.newScheduledExecutorService(
            ClassUtils.getCanonicalName(Config.class), 8,
            new NameThreadFactory("com.alibaba.nacos.config.server.timer"));
    
    /** 配置容量校正单线程池 */
    private static final ScheduledExecutorService CAPACITY_MANAGEMENT_EXECUTOR =
        ExecutorFactory.Managed.newSingleScheduledExecutorService(
            ClassUtils.getCanonicalName(Config.class),
            new NameThreadFactory("com.alibaba.nacos.config.CapacityManagement"));
    
    /** 异步长轮询/推送通知线程池（100 线程） */
    private static final ScheduledExecutorService ASYNC_NOTIFY_EXECUTOR =
        ExecutorFactory.Managed.newScheduledExecutorService(
            ClassUtils.getCanonicalName(Config.class), 100,
            new NameThreadFactory("com.alibaba.nacos.config.AsyncNotifyService"));
    
    /** 配置变更插件异步执行线程池 */
    private static final ScheduledExecutorService ASYNC_CONFIG_CHANGE_PLUGIN_EXECUTOR =
        ExecutorFactory.Managed.newScheduledExecutorService(
            ClassUtils.getCanonicalName(Config.class), ThreadUtils.getSuitableThreadCount(),
            new NameThreadFactory("com.alibaba.nacos.config.plugin.AsyncService"));
    
    /** 配置订阅服务线程池 */
    private static final ScheduledExecutorService CONFIG_SUB_SERVICE_EXECUTOR =
        ExecutorFactory.Managed.newScheduledExecutorService(
            ClassUtils.getCanonicalName(Config.class), ThreadUtils.getSuitableThreadCount(),
            new NameThreadFactory("com.alibaba.nacos.config.ConfigSubService"));
    
    /** 长轮询调度单线程池 */
    private static final ScheduledExecutorService LONG_POLLING_EXECUTOR =
        ExecutorFactory.Managed.newSingleScheduledExecutorService(
            ClassUtils.getCanonicalName(Config.class),
            new NameThreadFactory("com.alibaba.nacos.config.LongPolling"));
    
    /** 远程配置变更通知线程池（gRPC 等） */
    private static final ScheduledExecutorService ASYNC_CONFIG_CHANGE_NOTIFY_EXECUTOR =
        ExecutorFactory.Managed.newScheduledExecutorService(
            ClassUtils.getCanonicalName(Config.class), ThreadUtils.getSuitableThreadCount(),
            new NameThreadFactory("com.alibaba.nacos.config.server.remote.ConfigChangeNotifier"));
    
    /** 以固定延迟周期调度通用配置定时任务 */
    public static void scheduleConfigTask(Runnable command, long initialDelay, long delay,
        TimeUnit unit) {
        TIMER_EXECUTOR.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
    
    /** 一次性延迟执行配置变更相关任务 */
    public static void scheduleConfigChangeTask(Runnable command, long delay, TimeUnit unit) {
        TIMER_EXECUTOR.schedule(command, delay, unit);
    }
    
    /** 周期性调度容量使用量校正任务 */
    public static void scheduleCorrectUsageTask(Runnable runnable, long initialDelay, long delay,
        TimeUnit unit) {
        CAPACITY_MANAGEMENT_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    }
    
    /** 立即提交异步通知任务 */
    public static void executeAsyncNotify(Runnable runnable) {
        ASYNC_NOTIFY_EXECUTOR.execute(runnable);
    }
    
    /** 延迟调度异步通知任务 */
    public static void scheduleAsyncNotify(Runnable command, long delay, TimeUnit unit) {
        ASYNC_NOTIFY_EXECUTOR.schedule(command, delay, unit);
    }
    
    /** 提交配置变更插件异步任务 */
    public static void executeAsyncConfigChangePluginTask(Runnable runnable) {
        ASYNC_CONFIG_CHANGE_PLUGIN_EXECUTOR.execute(runnable);
    }
    
    /** 返回异步通知线程池当前排队任务数 */
    public static int asyncNotifyQueueSize() {
        return ((ScheduledThreadPoolExecutor) ASYNC_NOTIFY_EXECUTOR).getQueue().size();
    }
    
    /** 返回远程配置变更通知线程池排队任务数 */
    public static int asyncConfigChangeClientNotifyQueueSize() {
        return ((ScheduledThreadPoolExecutor) ASYNC_CONFIG_CHANGE_NOTIFY_EXECUTOR).getQueue()
            .size();
    }
    
    /** 获取配置订阅服务线程池 */
    public static ScheduledExecutorService getConfigSubServiceExecutor() {
        return CONFIG_SUB_SERVICE_EXECUTOR;
    }
    
    /** 获取客户端配置变更通知线程池 */
    public static ScheduledExecutorService getClientConfigNotifierServiceExecutor() {
        return ASYNC_CONFIG_CHANGE_NOTIFY_EXECUTOR;
    }
    
    /** 延迟调度客户端配置变更通知并返回 ScheduledFuture */
    public static ScheduledFuture<?> scheduleClientConfigNotifier(Runnable runnable, long delay,
        TimeUnit unit) {
        return ASYNC_CONFIG_CHANGE_NOTIFY_EXECUTOR.schedule(runnable, delay, unit);
    }
    
    /** 以固定延迟周期调度长轮询任务 */
    public static void scheduleLongPolling(Runnable runnable, long initialDelay, long delay,
        TimeUnit unit) {
        LONG_POLLING_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    }
    
    /** 一次性延迟调度长轮询任务 */
    public static ScheduledFuture<?> scheduleLongPolling(Runnable runnable, long delay,
        TimeUnit unit) {
        return LONG_POLLING_EXECUTOR.schedule(runnable, delay, unit);
    }
    
    /** 立即提交长轮询执行任务 */
    public static void executeLongPolling(Runnable runnable) {
        LONG_POLLING_EXECUTOR.execute(runnable);
    }
}
