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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 表明实现类包装了可通过 {@link #getWrappedRunnable()} 访问的 {@code Runnable}。
 * <p>
 * 可检查提交到 {@link io.reactivex.rxjava4.core.Scheduler Scheduler}
 * （或其 {@link io.reactivex.rxjava4.core.Scheduler.Worker Scheduler.Worker}）的 {@link Runnable}
 * 是否实现本接口以解包原始任务，避免在自定义
 * {@link RxJavaPlugins#onSchedule(Runnable)} 钩子中因内部委托而重复包装同一底层任务。
 * <p>History: 2.1.7 - experimental
 * @since 2.2
 */
public interface SchedulerRunnableIntrospection {

    /**
     * 返回被包装的动作。
     *
     * @return 被包装的动作，不可为 null
     */
    @NonNull
    Runnable getWrappedRunnable();
}
