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

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Processor;

import io.reactivex.rxjava4.annotations.*;

/**
 * 类 {@link Processor} 接口，结合 {@code Streamable} 与 {@link StreamSink} 接口，
 * 基于 {@link CompletionStage} 的异步处理与值/错误分发建立 push-pull 桥接。
 * @param <In> 输入侧元素类型
 * @param <Out> 输出侧元素类型
 * @since 4.0.0
 */
public interface StreamProcessor<@NonNull In, @NonNull Out> extends Streamable<Out>, StreamSink<In> {

    /**
     * 若本 {@link StreamProcessor} 拥有 {@link Streamer} 则返回 {@code true}。
     * @return 若本 {@link StreamProcessor} 拥有 {@link Streamer} 则为 {@code true}
     */
    boolean hasStreamers();

    /**
     * 返回当前订阅本 {@link StreamProcessor} 的 {@link Streamer} 数量。
     * @return 当前订阅本 {@link StreamProcessor} 的 {@link Streamer} 数量
     */
    int streamerCount();

    /**
     * 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 正常完成则返回 {@code true}。
     * @return 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 正常完成则为 {@code true}
     */
    boolean hasComplete();

    /**
     * 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 以 {@link Throwable} 完成则返回 {@code true}。
     * @return 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 以 {@link Throwable} 完成则为 {@code true}
     */
    boolean hasThrowable();

    /**
     * 若本 {@code StreamProcessor} 通过 {@link #finish(Throwable)} 以 {@code Throwable} 完成，
     * 则返回终端 {@link Throwable}。
     * @return 若存在则为 {@link Throwable}
     */
    @Nullable
    Throwable getThrowable();
}
