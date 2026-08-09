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

package io.reactivex.rxjava4.internal.util;

import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.schedulers.NonBlockingThread;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 常见阻塞操作的辅助工具方法。
 */
public final class BlockingHelper {
    /** 工具类，禁止实例化。 */
    private BlockingHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 阻塞等待 latch 完成；若已同步完成则直接返回。
     * 中断时 dispose 订阅并重新设置中断标志。
     * @param latch 完成信号 latch
     * @param subscription 中断时要 dispose 的订阅
     */
    public static void awaitForComplete(CountDownLatch latch, Disposable subscription) {
        if (latch.getCount() == 0) {
            // 同步 Observable 在 await 前已完成，跳过等待以免抛出 InterruptedException
            return;
        }
        // 阻塞直到订阅完成
        try {
            verifyNonBlocking();
            latch.await();
        } catch (InterruptedException e) {
            subscription.dispose();
            // 重新设置中断标志，便于调用方感知
            Thread.currentThread().interrupt();
            // 使用 RuntimeException 以免受检
            throw new IllegalStateException("Interrupted while waiting for subscription to complete.", e);
        }
    }

    /**
     * 检查 {@code failOnNonBlockingScheduler} 插件是否启用且当前线程属于不支持阻塞的 Scheduler。
     * @throws IllegalStateException 在不支持阻塞的 Scheduler 线程上尝试阻塞时
     */
    public static void verifyNonBlocking() {
        if (RxJavaPlugins.isFailOnNonBlockingScheduler()
                && (Thread.currentThread() instanceof NonBlockingThread
                        || RxJavaPlugins.onBeforeBlocking())) {
            throw new IllegalStateException("Attempt to block on a Scheduler " + Thread.currentThread().getName()
                    + " that doesn't support blocking operators as they may lead to deadlock");
        }
    }
}
