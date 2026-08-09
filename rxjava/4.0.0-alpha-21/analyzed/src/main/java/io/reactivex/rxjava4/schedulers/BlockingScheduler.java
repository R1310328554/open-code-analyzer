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

package io.reactivex.rxjava4.schedulers;

import io.reactivex.rxjava4.core.Scheduler;
import io.reactivex.rxjava4.functions.Action;
import io.reactivex.rxjava4.internal.functions.Functions;
import io.reactivex.rxjava4.internal.schedulers.BlockingCurrentThreadScheduler;

/**
 * 持有阻塞调度器实例，提供 {@link #execute()} 访问，以及获取纯 {@link Scheduler} 视图供传参。
 * <p>
 * <strong>实现说明</strong><br>
 *           客户端无需实例化本 record；它同时暴露 {@code Scheduler} 接口与阻塞专用 {@link #execute()}。
 * @param backingScheduler 底层调度器实例
 * @since 4.0.0
 */
public record BlockingScheduler(BlockingCurrentThreadScheduler backingScheduler) {

    /**
     * 返回用于提交任务或作为参数的 Scheduler 视图。
     * @return 底层阻塞当前线程调度器的 Scheduler 视图
     */
    public Scheduler scheduler( ) {
        return backingScheduler;
    }

    /**
     * 无初始动作启动阻塞事件循环。
     * <p>
     * 本方法阻塞直至调用 {@link Scheduler#shutdown()}。
     * @see #execute(Action)
     */
    public void execute() {
        execute(Functions.EMPTY_ACTION);
    }

    /**
     * 以给定初始动作（通常含 main 其余逻辑）启动阻塞事件循环。
     * <p>
     * 本方法阻塞直至调用 {@link Scheduler#shutdown()}。
     * @param action 要执行的动作
     */
    public void execute(Action action) {
        backingScheduler.execute(action);
    }

    /**
     * 关闭底层阻塞当前线程调度器
     */
    public void shutdown() {
        backingScheduler.shutdown();
    }
}
