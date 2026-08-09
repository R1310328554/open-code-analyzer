/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.core.config;

import java.util.concurrent.ThreadFactory;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.internal.functions.ObjectHelper;
import io.reactivex.rxjava4.schedulers.Schedulers;

/**
 * {@link Schedulers#createParallel(ParallelSchedulerConfig)} 的配置 record。
 * @param parallelism 并发线程数，默认为 CPU 数量。
 * @param tracking 若为 true，提交的任务会被跟踪并可批量 dispose
 * @param priority 所创建平台线程的线程优先级。参见 {@link Thread#NORM_PRIORITY}。
 * @param threadNamePrefix 调度器线程的命名前缀
 * @param factory 底层 Executor 的可定制工厂；若非 null，则 priority 与 threadNamePrefix 被忽略
 */
public record ParallelSchedulerConfig(
        int parallelism,
        boolean tracking,
        int priority,
        @NonNull String threadNamePrefix,
        @Nullable ThreadFactory factory) {

    /**
     * 默认配置：可用 CPU 并行度、启用 tracking、普通线程优先级、
     * {@code RxParallelScheduler} 命名、无自定义 {@link ThreadFactory}。
     */
    public static final ParallelSchedulerConfig DEFAULT = new ParallelSchedulerConfig(
            Runtime.getRuntime().availableProcessors(), true, Thread.NORM_PRIORITY,
            "RxParallelScheduler", null);

    /**
     * 使用给定并行度创建默认配置：普通优先级、启用 tracking、RxParallelScheduler 线程名前缀。
     * @param parallelism 调度器中使用的线程数
     */
    public ParallelSchedulerConfig(int parallelism) {
        this(parallelism, true, Thread.NORM_PRIORITY, "RxParallelScheduler", null);
    }

    /**
     * Creates a default config with the given parallelism,
     * normal priority, optionally tracking and RxParallelScheduler thread name prefix.
     * @param parallelism the number of threads to work with in the scheduler
     * @param tracking 若为 true，提交的任务会被跟踪并可批量 dispose
     */
    public ParallelSchedulerConfig(int parallelism, boolean tracking) {
        this(parallelism, tracking, Thread.NORM_PRIORITY, "RxParallelScheduler", null);
    }

    /**
     * Creates a default config with the given parallelism,
     * normal priority, optionally tracking and RxParallelScheduler thread name prefix.
     * @param parallelism the number of threads to work with in the scheduler
     * @param threadNamePrefix 调度器线程的命名前缀
     */
    public ParallelSchedulerConfig(int parallelism, @NonNull String threadNamePrefix) {
        this(parallelism, true, Thread.NORM_PRIORITY, threadNamePrefix, null);
    }

    /**
     * Creates a default config with the given parallelism,
     * normal priority, optionally tracking and RxParallelScheduler thread name prefix.
     * @param parallelism the number of threads to work with in the scheduler
     * @param tracking 若为 true，提交的任务会被跟踪并可批量 dispose
     * @param threadNamePrefix 调度器线程的命名前缀
     */
    public ParallelSchedulerConfig(int parallelism, boolean tracking, @NonNull String threadNamePrefix) {
        this(parallelism, tracking, Thread.NORM_PRIORITY, threadNamePrefix, null);
    }

    /**
     * Creates a default config with the given parallelism,
     * normal priority, optionally tracking and RxParallelScheduler thread name prefix.
     * @param parallelism the number of threads to work with in the scheduler
     * @param tracking 若为 true，提交的任务会被跟踪并可批量 dispose
     * @param factory 底层 Executor 的可定制工厂
     */
    public ParallelSchedulerConfig(int parallelism, boolean tracking, @NonNull ThreadFactory factory) {
        this(parallelism, tracking, Thread.NORM_PRIORITY, "", factory);
    }

    /**
     * Creates a fully configurable ParallelSchedulerConfig object.
     * @param parallelism the number of threads to work with in the scheduler
     * @param tracking 若为 true，提交的任务会被跟踪并可批量 dispose
     * @param priority 所创建平台线程的线程优先级。参见 {@link Thread#NORM_PRIORITY}。
     * @param threadNamePrefix 调度器线程的命名前缀
     * @param factory 底层 Executor 的可定制工厂, if non-null, the priority
     *                and threadNamePrefix are ignored
     */
    public ParallelSchedulerConfig {
        ObjectHelper.verifyPositive(parallelism, "parallelism");
    }
}
