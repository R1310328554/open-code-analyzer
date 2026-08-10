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

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.ThreadFactoryBuilder;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Core 模块全局线程池入口，统一管理通用调度、Distro 协议与 gRPC 执行器。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public class GlobalExecutor {
    
    /** 通用定时/调度线程池（4 线程）。 */
    private static final ScheduledExecutorService COMMON_EXECUTOR = ExecutorFactory.Managed
        .newScheduledExecutorService(ClassUtils.getCanonicalName(GlobalExecutor.class), 4,
            new NameThreadFactory("com.alibaba.nacos.core.common"));
    
    /** Distro 协议数据同步专用调度线程池。 */
    private static final ScheduledExecutorService DISTRO_EXECUTOR = ExecutorFactory.Managed
        .newScheduledExecutorService(ClassUtils.getCanonicalName(GlobalExecutor.class),
            EnvUtil.getAvailableProcessors(2),
            new NameThreadFactory("com.alibaba.nacos.core.protocal.distro"));
    
    /** SDK gRPC 请求处理线程池。 */
    public static final ThreadPoolExecutor sdkRpcExecutor = new ThreadPoolExecutor(
        EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()),
        EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()), 60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(RemoteUtils.getRemoteExecutorQueueSize()),
        new ThreadFactoryBuilder().daemon(true).nameFormat("nacos-grpc-executor-%d").build());
    
    /** 集群 gRPC 请求处理线程池。 */
    public static final ThreadPoolExecutor clusterRpcExecutor = new ThreadPoolExecutor(
        EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()),
        EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()), 60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(RemoteUtils.getRemoteExecutorQueueSize()),
        new ThreadFactoryBuilder().daemon(true).nameFormat("nacos-cluster-grpc-executor-%d")
            .build());
    
    /** 在当前线程同步执行 Runnable（不提交线程池）。 */
    public static void runWithoutThread(Runnable runnable) {
        runnable.run();
    }
    
    /** 提交任务到通用线程池；已关闭则忽略。 */
    public static void executeByCommon(Runnable runnable) {
        if (COMMON_EXECUTOR.isShutdown()) {
            return;
        }
        COMMON_EXECUTOR.execute(runnable);
    }
    
    /** 延迟 {@code delayMs} 毫秒后在通用线程池执行。 */
    public static void scheduleByCommon(Runnable runnable, long delayMs) {
        if (COMMON_EXECUTOR.isShutdown()) {
            return;
        }
        COMMON_EXECUTOR.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /** 以固定间隔 {@code delayMs} 在通用线程池周期执行。 */
    public static void scheduleWithFixDelayByCommon(Runnable runnable, long delayMs) {
        if (COMMON_EXECUTOR.isShutdown()) {
            return;
        }
        COMMON_EXECUTOR.scheduleWithFixedDelay(runnable, delayMs, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /** 立即提交 Distro 数据加载任务。 */
    public static void submitLoadDataTask(Runnable runnable) {
        DISTRO_EXECUTOR.submit(runnable);
    }
    
    /** 延迟 {@code delay} 毫秒后提交 Distro 数据加载任务。 */
    public static void submitLoadDataTask(Runnable runnable, long delay) {
        DISTRO_EXECUTOR.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }
    
    /** 以固定间隔调度 Distro 分区数据定时同步任务。 */
    public static void schedulePartitionDataTimedSync(Runnable runnable, long interval) {
        DISTRO_EXECUTOR.scheduleWithFixedDelay(runnable, interval, interval, TimeUnit.MILLISECONDS);
    }
    
}
