/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.thread.FutureTaskExtThreadPoolExecutor;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 线程池与线程工厂创建、优雅停机的工具类（不可实例化）。
 */
public final class ThreadUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.TOOLS_LOGGER_NAME);

    public static ExecutorService newSingleThreadExecutor(String processName, boolean isDaemon) {
        return ThreadUtils.newSingleThreadExecutor(newThreadFactory(processName, isDaemon));
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return ThreadUtils.newThreadPoolExecutor(1, threadFactory);
    }

    public static ExecutorService newThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        return ThreadUtils.newThreadPoolExecutor(corePoolSize, corePoolSize,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            threadFactory);
    }

    public static ExecutorService newThreadPoolExecutor(int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit, BlockingQueue<Runnable> workQueue,
        String processName,
        boolean isDaemon) {
        return ThreadUtils.newThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, newThreadFactory(processName, isDaemon));
    }

    public static ExecutorService newThreadPoolExecutor(final int corePoolSize,
        final int maximumPoolSize,
        final long keepAliveTime,
        final TimeUnit unit,
        final BlockingQueue<Runnable> workQueue,
        final ThreadFactory threadFactory) {
        return ThreadUtils.newThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ExecutorService newThreadPoolExecutor(int corePoolSize,
        int maximumPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue,
        ThreadFactory threadFactory,
        RejectedExecutionHandler handler) {
        return new FutureTaskExtThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String processName, boolean isDaemon) {
        return ThreadUtils.newScheduledThreadPool(1, processName, isDaemon);
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return ThreadUtils.newScheduledThreadPool(1, threadFactory);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return ThreadUtils.newScheduledThreadPool(corePoolSize, Executors.defaultThreadFactory());
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String processName,
        boolean isDaemon) {
        return ThreadUtils.newScheduledThreadPool(corePoolSize, newThreadFactory(processName, isDaemon));
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return ThreadUtils.newScheduledThreadPool(corePoolSize, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize,
        ThreadFactory threadFactory,
        RejectedExecutionHandler handler) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, handler);
    }

    public static ThreadFactory newThreadFactory(String processName, boolean isDaemon) {
        return newGenericThreadFactory("ThreadUtils-" + processName, isDaemon);
    }

    public static ThreadFactory newGenericThreadFactory(String processName) {
        return newGenericThreadFactory(processName, false);
    }

    public static ThreadFactory newGenericThreadFactory(String processName, int threads) {
        return newGenericThreadFactory(processName, threads, false);
    }

    public static ThreadFactory newGenericThreadFactory(final String processName, final boolean isDaemon) {
        return new ThreadFactoryImpl(processName + "_", isDaemon);
    }

    public static ThreadFactory newGenericThreadFactory(final String processName, final int threads,
        final boolean isDaemon) {
        return new ThreadFactoryImpl(String.format("%s_%d_", processName, threads), isDaemon);
    }

    /**
     * 创建新线程并注册未捕获异常处理器。
     *
     * @param name     线程名
     * @param runnable 线程任务
     * @param daemon   是否为守护线程（守护线程不阻止 JVM 退出）
     * @return 尚未 start 的线程实例
     */
    public static Thread newThread(String name, Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemon);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.error("Uncaught exception in thread '" + t.getName() + "':", e);
            }
        });
        return thread;
    }

    /**
     * 通过 interrupt 与 join 优雅停止线程（无限等待直至结束）。
     *
     * @param t 待停止线程
     */
    public static void shutdownGracefully(final Thread t) {
        shutdownGracefully(t, 0);
    }

    /**
     * 通过 interrupt 与 join 优雅停止线程。
     *
     * @param millis 每次 join 的超时毫秒数，0 表示一直等待
     * @param t      待停止线程
     */
    public static void shutdownGracefully(final Thread t, final long millis) {
        if (t == null)
            return;
        while (t.isAlive()) {
            try {
                t.interrupt();
                t.join(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 按 {@link ExecutorService} 推荐的两阶段流程优雅关闭线程池。
     *
     * @param executor 待关闭执行器
     * @param timeout  等待超时
     * @param timeUnit 超时单位
     */
    public static void shutdownGracefully(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        // 停止接收新任务
        executor.shutdown();
        try {
            // 等待已在执行的任务结束
            if (!executor.awaitTermination(timeout, timeUnit)) {
                executor.shutdownNow();
                // 再等待一段时间以便任务响应取消
                if (!executor.awaitTermination(timeout, timeUnit)) {
                    LOGGER.warn(String.format("%s didn't terminate!", executor));
                }
            }
        } catch (InterruptedException ie) {
            // 当前线程被中断时再次尝试立即取消
            executor.shutdownNow();
            // 恢复中断状态
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 关闭指定 {@link ExecutorService}（不等待任务完成）。
     *
     * @param executorService 执行器，可为 null
     */
    public static void shutdown(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * 私有构造，禁止实例化工具类。
     */
    private ThreadUtils() {
        // 未使用

    }
}
