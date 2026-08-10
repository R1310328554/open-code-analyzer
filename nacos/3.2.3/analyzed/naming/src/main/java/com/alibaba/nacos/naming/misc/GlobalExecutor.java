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

package com.alibaba.nacos.naming.misc;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.core.utils.ClassUtils;
import com.alibaba.nacos.naming.NamingApp;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Naming 模块全局线程池管理器。
 *
 * <p>集中提供定时器、MySQL/TCP 健康检查、健康调度、推送重传、性能日志、过期客户端清理及推送回调等专用线程池，统一生命周期管理。</p>
 *
 * @author nacos
 */
@SuppressWarnings("checkstyle:indentation")
public class GlobalExecutor {
    
    /** 服务端状态更新周期（毫秒）。 */
    private static final long SERVER_STATUS_UPDATE_PERIOD = TimeUnit.SECONDS.toMillis(5);
    
    /** 默认线程数（CPU 核数的一半）。 */
    public static final int DEFAULT_THREAD_COUNT = EnvUtil.getAvailableProcessors(0.5);
    
    private static final ScheduledExecutorService NAMING_TIMER_EXECUTOR = ExecutorFactory.Managed
        .newScheduledExecutorService(ClassUtils.getCanonicalName(NamingApp.class),
            EnvUtil.getAvailableProcessors(2),
            new NameThreadFactory("com.alibaba.nacos.naming.timer"));
    
    private static final ExecutorService MYSQL_CHECK_EXECUTOR = ExecutorFactory.Managed
        .newFixedExecutorService(ClassUtils.getCanonicalName(NamingApp.class), DEFAULT_THREAD_COUNT,
            new NameThreadFactory("com.alibaba.nacos.naming.mysql.checker"));
    
    private static final ScheduledExecutorService TCP_SUPER_SENSE_EXECUTOR = ExecutorFactory.Managed
        .newScheduledExecutorService(ClassUtils.getCanonicalName(NamingApp.class),
            DEFAULT_THREAD_COUNT,
            new NameThreadFactory("com.alibaba.nacos.naming.supersense.checker"));
    
    private static final ExecutorService TCP_CHECK_EXECUTOR = ExecutorFactory.Managed
        .newFixedExecutorService(ClassUtils.getCanonicalName(NamingApp.class), 2,
            new NameThreadFactory("com.alibaba.nacos.naming.tcp.check.worker"));
    
    private static final ScheduledExecutorService NAMING_HEALTH_EXECUTOR = ExecutorFactory.Managed
        .newScheduledExecutorService(ClassUtils.getCanonicalName(NamingApp.class),
            Integer.max(
                Integer.getInteger("com.alibaba.nacos.naming.health.thread.num",
                    DEFAULT_THREAD_COUNT),
                1),
            new NameThreadFactory("com.alibaba.nacos.naming.health"));
    
    private static final ScheduledExecutorService RETRANSMITTER_EXECUTOR = ExecutorFactory.Managed
        .newSingleScheduledExecutorService(ClassUtils.getCanonicalName(NamingApp.class),
            new NameThreadFactory("com.alibaba.nacos.naming.push.retransmitter"));
    
    private static final ScheduledExecutorService SERVER_PERFORMANCE_EXECUTOR =
        ExecutorFactory.Managed
            .newSingleScheduledExecutorService(ClassUtils.getCanonicalName(NamingApp.class),
                new NameThreadFactory("com.alibaba.nacos.naming.nacos-server-performance"));
    
    private static final ScheduledExecutorService EXPIRED_CLIENT_CLEANER_EXECUTOR =
        ExecutorFactory.Managed
            .newSingleScheduledExecutorService(ClassUtils.getCanonicalName(NamingApp.class),
                new NameThreadFactory("com.alibaba.nacos.naming.remote-connection-manager"));
    
    private static final ExecutorService PUSH_CALLBACK_EXECUTOR = ExecutorFactory.Managed
        .newSingleExecutorService("Push",
            new NameThreadFactory("com.alibaba.nacos.naming.push.callback"));
    
    private static final ScheduledExecutorService MONITOR_HEALTH_CHECK_POOL_EXECUTOR =
        ExecutorFactory.Managed
            .newScheduledExecutorService(ClassUtils.getCanonicalName(NamingApp.class),
                1, new NameThreadFactory("com.alibaba.nacos.naming.health-check-pool"));
    
    /** 注册服务端状态周期性更新任务。 */
    public static void registerServerStatusUpdater(Runnable runnable) {
        NAMING_TIMER_EXECUTOR.scheduleAtFixedRate(runnable, 0, SERVER_STATUS_UPDATE_PERIOD,
            TimeUnit.MILLISECONDS);
    }
    
    /** 提交 MySQL 健康检查任务。 */
    public static void executeMysqlCheckTask(Runnable runnable) {
        MYSQL_CHECK_EXECUTOR.execute(runnable);
    }
    
    /** 提交 TCP 健康检查主循环任务。 */
    public static void submitTcpCheck(Runnable runnable) {
        TCP_CHECK_EXECUTOR.submit(runnable);
    }
    
    /** 批量提交 TCP SuperSense NIO 连接任务并等待全部完成。 */
    public static <T> List<Future<T>> invokeAllTcpSuperSenseTask(
        Collection<? extends Callable<T>> tasks)
        throws InterruptedException {
        return TCP_SUPER_SENSE_EXECUTOR.invokeAll(tasks);
    }
    
    /** 在 TCP SuperSense 线程池异步执行 NIO 后置处理。 */
    public static void executeTcpSuperSense(Runnable runnable) {
        TCP_SUPER_SENSE_EXECUTOR.execute(runnable);
    }
    
    /** 延迟调度 TCP 连接超时任务。 */
    public static void scheduleTcpSuperSenseTask(Runnable runnable, long delay, TimeUnit unit) {
        TCP_SUPER_SENSE_EXECUTOR.schedule(runnable, delay, unit);
    }
    
    /** 延迟调度一次健康检查任务。 */
    public static ScheduledFuture<?> scheduleNamingHealth(Runnable command, long delay,
        TimeUnit unit) {
        return NAMING_HEALTH_EXECUTOR.schedule(command, delay, unit);
    }
    
    /** 以固定延迟周期性调度健康检查任务。 */
    public static ScheduledFuture<?> scheduleNamingHealth(Runnable command, long initialDelay,
        long delay,
        TimeUnit unit) {
        return NAMING_HEALTH_EXECUTOR.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
    
    /** 延迟调度推送重传任务。 */
    public static void scheduleRetransmitter(Runnable runnable, long delay, TimeUnit unit) {
        RETRANSMITTER_EXECUTOR.schedule(runnable, delay, unit);
    }
    
    /** 周期性调度服务端性能日志任务。 */
    public static void schedulePerformanceLogger(Runnable runnable, long initialDelay, long delay,
        TimeUnit unit) {
        SERVER_PERFORMANCE_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    }
    
    /** 周期性调度过期远程客户端清理任务。 */
    public static void scheduleExpiredClientCleaner(Runnable runnable, long initialDelay,
        long delay, TimeUnit unit) {
        EXPIRED_CLIENT_CLEANER_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    }
    
    /** 获取推送回调专用单线程池。 */
    public static ExecutorService getCallbackExecutor() {
        return PUSH_CALLBACK_EXECUTOR;
    }
    
    /** 周期性监控 HTTP 健康检查连接池状态。 */
    public static ScheduledFuture<?> scheduleMonitorHealthCheckPool(Runnable runnable,
        long initialDelay, long delay,
        TimeUnit unit) {
        return MONITOR_HEALTH_CHECK_POOL_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay,
            delay, unit);
    }
}
