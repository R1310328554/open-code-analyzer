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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Scheduler;

/**
 * 允许从实现类一次性获取多个 {@link Scheduler.Worker}：
 * 请求数不超过并行度时，各 Worker 绑定不同底层线程。
 * <p>History: 2.1.8 - experimental
 * @since 2.2
 */
public interface SchedulerMultiWorkerSupport {

    /**
     * 创建 number 个可能由不同线程支撑的 Worker，
     * 通过 callback 逐个回调。
     * @param number 要创建的 Worker 数量，须为正数
     * @param callback 接收 Worker 实例的回调
     */
    void createWorkers(int number, @NonNull WorkerCallback callback);

    /**
     * {@link #createWorkers(int, WorkerCallback)} 的回调接口。
     */
    interface WorkerCallback {
        /**
         * 回调 Worker 索引与实例。
         * @param index Worker 索引，从 0 开始
         * @param worker Worker 实例
         */
        void onWorker(int index, @NonNull Scheduler.Worker worker);
    }
}
