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

import java.util.NoSuchElementException;
import java.util.concurrent.*;

import io.reactivex.rxjava4.annotations.NonNull;

/// 已实现的可分步异步消费的流。
/// 可将其视为移植到 Java 的 C# `IAsyncEnumerator`。
///
/// `Streamer` 方法必须像
/// <a href='https://github.com/reactive-streams/reactive-streams-jvm#1.3'>Reactive Streams 规则 §1.3</a> 一样顺序、非重叠地调用。
///
/// 为优化同步操作，请考虑使用 {@link #NEXT_TRUE}、{@link #NEXT_FALSE}
/// 与 {@link #FINISHED} 常量 CompletionStage。
/// @param <T> 元素类型
/// @since 4.0.0
public interface Streamer<@NonNull T> {

    // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
    // API
    // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

    /**
     * 判断源是否还有更多可用元素。
     * @return 具有 3 种结果的 `CompletionStage`
     * <ul>
     * <li>`true` 表示可通过 {@link #current()} 消费一个元素
     * <li>`false` 表示没有更多可用元素
     * <li>`Throwable` 表示上游发生错误
     * </ul>
     */
    @NonNull
    CompletionStage<Boolean> next();

    /**
     * 若前一次 [#next()] 调用返回 `true`，则同步返回当前可用元素。
     * 在 [#next()] 或 [#finish()] 调用进行中，或在 `Streamer` 生命周期之外调用本方法属于未定义行为，可能返回 `null` 或抛出异常。
     * @return 当前元素
     * @throws NoSuchElementException 若无元素可返回
     */
    @NonNull
    T current();

    /**
     * 在通过耗尽或取消完成全部处理后结束序列。
     * <p>
     * 通常涉及资源清理，因此必须始终调用本方法。
     * <p>
     * 若清理崩溃且 [#next()] 也崩溃，清理产生的 `Throwable` 将作为 suppressed 异常
     * 附加到 `next` 产生的主崩溃 `Throwable` 上。
     *
     * @return 在资源清理正常或异常完成时完成的 `CompletionStage`
     */
    @NonNull
    CompletionStage<Void> finish();

    // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
    // HELPERS
    // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

    /**
     * 便捷方法：阻塞等待 {@link #next()} 返回的 CompletionStage。
     * @return 若还有更多元素则为 true，若无更多元素则为 false，或在出错时崩溃
     */
    default boolean awaitNext() {
        return awaitBoolean(next());
    }

    /**
     * 便捷方法：阻塞等待 {@link #finish()} 返回的 CompletionStage。
     */
    default void awaitFinish() {
        awaitVoid(finish());
    }

    /**
     * 便捷方法：等待 boolean stage 完成，针对 {@link #NEXT_TRUE} 与 {@link #NEXT_FALSE} 做了直接处理优化。
     * @param stage 待等待的 stage
     * @return stage 的结果
     */
    static boolean awaitBoolean(CompletionStage<Boolean> stage) {
        if (stage == NEXT_TRUE) {
            return true;
        } else
        if (stage == NEXT_FALSE) {
            return false;
        }
        return stage.toCompletableFuture().join();
    }

    /**
     * 便捷方法：等待 stage 完成，针对 {@link #FINISHED} 做了直接处理优化。
     * @param stage 待等待的 stage
     */
    static void awaitVoid(CompletionStage<Void> stage) {
        if (stage == FINISHED) {
            return;
        }
        stage.toCompletableFuture().join();
    }

    /**
     * 在 {@link #next()} 中使用本常量，表示下一个值同步可用。
     */
    CompletableFuture<Boolean> NEXT_TRUE = CompletableFuture.completedFuture(true);

    /**
     * 在 {@link #next()} 中使用本常量，表示同步地没有更多可用值。
     */
    CompletableFuture<Boolean> NEXT_FALSE = CompletableFuture.completedFuture(false);

    /**
     * 在 {@link #finish()} 中使用本常量，表示清理已同步完成。
     */
    CompletableFuture<Void> FINISHED = CompletableFuture.completedFuture(null);
}
