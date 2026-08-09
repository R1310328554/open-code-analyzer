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

package io.reactivex.rxjava4.internal.operators.streamable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.fuseable.HasUpstreamStreamableSource;

/**
 * 在上游正常结束后，按 {@code whenFunction} 决定是否重新订阅源 {@link Streamable}。
 * 每完成一轮消费调用 {@code whenFunction(completionCount)}，返回 true 则再次订阅。
 * @param <T> 元素类型
 */
public record StreamableRepeat<T>(
        Streamable<T> source,
        Function<? super Long, ? extends CompletionStage<Boolean>> whenFunction
)
implements Streamable<T>, HasUpstreamStreamableSource<T> {

    @Override
    public @NonNull Streamer<@NonNull T> stream(@NonNull StreamerCancellation cancellation) {
        var streamer = new RepeatStreamer<>(source, cancellation, whenFunction);
        streamer.retrySource();
        return streamer;
    }

    /** 三阶段状态机：拉取上游 → finish → 调用 whenFunction 决定是否 repeat。 */
    static final class RepeatStreamer<T>
    implements Streamer<T>, BiConsumer<Object, Throwable> {

        final Streamable<T> source;

        final StreamerCancellation downstreamCancellation;

        final Function<? super Long, ? extends CompletionStage<Boolean>> whenFunction;

        final AtomicInteger wipSource;

        Streamer<T> currentStreamer;

        CompletableFuture<Boolean> nextWaiter;

        volatile int stage;

        long completionCount;

        Disposable whenFunctionCancel;

        RepeatStreamer(Streamable<T> source, StreamerCancellation downstreamCancellation,
                Function<? super Long, ? extends CompletionStage<Boolean>> whenFunction) {
            this.source = source;
            this.downstreamCancellation = downstreamCancellation;
            this.whenFunction = whenFunction;
            this.wipSource = new AtomicInteger();
            this.stage = -1;
        }

        /** wip 串行保护下重新订阅 source 并启动 next 回调链。 */
        void retrySource() {
            if (wipSource.getAndIncrement() != 0) {
                return;
            }
            do {
                // FIXME：部分算子未清理 StreamerCancellation，暂为每次重订阅派生新的 cancellation
                var innerCanceller = downstreamCancellation.derive();
                currentStreamer = source.stream(innerCanceller);
                if (stage == 0) {
                    stage = 1;
                    currentStreamer.next().whenComplete(this);
                }
            } while (wipSource.decrementAndGet() != 0);
        }

        @Override
        public @NonNull CompletionStage<Boolean> next() {
            nextWaiter = new CompletableFuture<>();
            stage = 1;
            currentStreamer.next().whenComplete(this);
            return nextWaiter;
        }

        @Override
        public void accept(Object t, Throwable u) {
            if (stage == 1) {
                if (u != null) {
                    nextWaiter.completeExceptionally(u);
                } else
                if ((Boolean)t) {
                    nextWaiter.complete(true);
                } else {
                    var streamer = currentStreamer;
                    currentStreamer = null;
                    stage = 2;
                    streamer.finish().whenComplete(this);

                }
            } else
            if (stage == 2) {
                if (u != null) {
                    nextWaiter.completeExceptionally(u);
                } else {
                    try {
                        var cs = whenFunction.apply(completionCount++);
                        whenFunctionCancel = Disposable.fromAction(() -> cs.toCompletableFuture().cancel(true));
                        downstreamCancellation.add(whenFunctionCancel);
                        stage = 3;
                        cs.whenComplete(this);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        nextWaiter.completeExceptionally(ex);
                    }
                }
            } else { // 阶段 3：whenFunction 完成，true 则重试，false 则结束
                downstreamCancellation.delete(whenFunctionCancel);
                whenFunctionCancel = null;
                var cf = nextWaiter;
                if (u != null) {
                    cf.completeExceptionally(u);
                } else
                if ((Boolean)t){
                    stage = 0;
                    retrySource();
                } else {
                    cf.complete(false);
                }
            }
        }

        @Override
        public @NonNull T current() {
            return currentStreamer.current();
        }

        @Override
        public @NonNull CompletionStage<Void> finish() {
            if (currentStreamer != null) {
                return currentStreamer.finish();
            }
            return FINISHED;
        }
    }
}
