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

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.Flow.Subscriber;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.operators.streamable.*;

/**
 * 向消费者提交元素与终端事件的接口；消费者会指示该元素或终端事件的处理何时完成，
 * 类似 {@link Subscriber} 接收事件的方式。
 * <p>
 * 一般约定：以非重叠方式调用 {@link #next(Object)} 零次或多次，
 * 然后至多调用一次 {@link #finish(Throwable)}，且仅在返回的 {@link CompletionStage} 以某种方式完成之后。
 * @param <T> 待提交的元素类型
 * @since 4.0.0
 */
public interface StreamSink<@NonNull T> {

    /**
     * 提交下一个元素。
     * @param item 待提交的元素
     * @return 若值被成功消费则完成为 {@code true} 的 {@link CompletionStage}；
     *         若值被拒绝或在错误时以异常完成则为 {@code false}
     */
    @NonNull
    CompletionStage<Boolean> next(T item);

    /**
     * 提交最终终端事件。
     * @param throwable 可选的用于发出错误信号的 throwable，{@code null} 表示正常完成
     * @return 若调用成功则完成为 {@code null} 的 {@link CompletionStage}，或在错误时以异常完成
     */
    @NonNull
    CompletionStage<Void> finish(@Nullable Throwable throwable);

    /**
     * 提交给定元素并以阻塞方式等待其被消费。
     * @param item 待提交的元素
     * @return 若元素被接受则为 true，否则为 false
     * @throws CancellationException 若发生取消
     * @throws CompletionException 若上游失败
     */
    default boolean awaitNext(T item) {
        return next(item).toCompletableFuture().join();
    }

    /**
     * 提交最终终端事件并以阻塞方式等待其被消费。
     * @param throwable 可选的用于发出错误信号的 throwable，{@code null} 表示正常完成
     * @throws CancellationException 若发生取消
     * @throws CompletionException 若上游失败
     */
    default void awaitFinish(Throwable throwable) {
        finish(throwable).toCompletableFuture().join();
    }

    /**
     * 返回用于检测消费者是否已表示不再接受更多元素的 {@link DisposableContainer}。
     * <p>
     * 默认实现返回新的 {@link CompositeDisposable}。
     * @return {@code DisposableContainer}
     */
    @NonNull
    default DisposableStreamerCancellation cancellation() {
        return new CompositeDisposable();
    }

    /**
     * 返回新的 {@link StreamSink}，其 {@link #cancellation()} 返回给定 {@link DisposableStreamerCancellation}，
     * 允许覆盖本 {@code StreamSink} 的取消管理。
     * @param cancellation 用作取消管理的 {@link DisposableStreamerCancellation}
     * @return 新的 {@code StreamSink} 实例
     * @throws NullPointerException 若 {@code cancellation} 为 {@code null}
     */
    @NonNull
    default StreamSink<T> withCancellation(DisposableStreamerCancellation cancellation) {
        Objects.requireNonNull(cancellation, "cancellation is null");
        return new StreamSinkWithCancellation<>(this, cancellation);
    }

    /**
     * 通过 {@link #next(Object)} 与 {@link #finish(Throwable)} 的 lambda 回调创建 {@link StreamSink}。
     * <p>
     * 回调抛出的非致命异常会转为失败的 {@link CompletableFuture#failedFuture(Throwable)}。
     * @param <T> 流的元素类型
     * @param onNext {@code next} 方法的回调
     * @param onFinish {@code finish} 方法的回调
     * @return 新的 {@link StreamSink} 实例
     */
    @NonNull
    static <T> StreamSink<T> create(
            @NonNull Function<? super T, ? extends CompletionStage<Boolean>> onNext,
            @NonNull Function<? super Throwable, ? extends CompletionStage<Void>> onFinish
    ) {
        Objects.requireNonNull(onNext, "onNext is null");
        Objects.requireNonNull(onFinish, "onFinish is null");
        return new StreamSinkLambda<>(onNext, onFinish);
    }
}
