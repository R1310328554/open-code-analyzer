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

package io.reactivex.rxjava4.internal.schedulers;

import java.util.concurrent.ThreadFactory;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Scheduler;

/**
 * 每次 createWorker 创建独立 NewThreadWorker（新线程池）的 Scheduler。
 */
public final class NewThreadScheduler extends Scheduler {

    final ThreadFactory threadFactory;

    private static final String THREAD_NAME_PREFIX = "RxNewThreadScheduler";
    private static final RxThreadFactory THREAD_FACTORY;

    /** 设置本 Scheduler 线程优先级的系统属性键。 */
    private static final String KEY_NEWTHREAD_PRIORITY = "rxjava4.newthread-priority";

    static {
        int priority = Math.clamp(
                Integer.getInteger(KEY_NEWTHREAD_PRIORITY, Thread.NORM_PRIORITY), Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);

        THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, priority);
    }

    public NewThreadScheduler() {
        this(THREAD_FACTORY);
    }

    /** @param threadFactory 创建 NewThreadWorker 底层线程的工厂 */
    public NewThreadScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    /** 返回基于 threadFactory 的新 NewThreadWorker。 */
    @NonNull
    @Override
    public Worker createWorker() {
        return new NewThreadWorker(threadFactory);
    }
}
