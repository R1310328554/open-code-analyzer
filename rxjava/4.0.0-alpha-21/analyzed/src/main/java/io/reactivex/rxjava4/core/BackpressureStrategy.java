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

package io.reactivex.rxjava4.core;

/**
 * 对源流施加背压时的策略选项。
 */
public enum BackpressureStrategy {
    /**
     * {@code onNext} 事件不经缓冲或丢弃直接写入；下游须自行处理溢出。
     * <p>适用于已配合自定义参数的 onBackpressureXXX 算子使用。
     */
    MISSING,
    /**
     * 若下游跟不上，则发出 {@link io.reactivex.rxjava4.exceptions.MissingBackpressureException MissingBackpressureException}。
     */
    ERROR,
    /**
     * 缓冲<em>全部</em> {@code onNext} 值，直至下游消费完毕。
     */
    BUFFER,
    /**
     * 若下游跟不上，丢弃最新的 {@code onNext} 值。
     */
    DROP,
    /**
     * 仅保留最新 {@code onNext} 值；若下游跟不上，新值覆盖旧值。
     */
    LATEST
}
