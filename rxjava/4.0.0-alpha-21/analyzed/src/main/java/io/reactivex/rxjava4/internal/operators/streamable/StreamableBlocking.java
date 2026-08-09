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

import java.util.NoSuchElementException;
import java.util.concurrent.CompletionException;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.Streamable;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/** Streamable 阻塞消费工具：blockingFirst/blockingLast 同步取首/末元素。 */
public record StreamableBlocking() {

    /**
     * 阻塞消费首个元素并 awaitFinish；空序列抛 NoSuchElementException。
     * @param <T> 元素类型
     * @param source 源 {@code Streamable}
     * @return 首个元素
     * @throws RuntimeException 上游抛出 unchecked 异常时
     * @throws CompletionException 上游抛出 checked 异常时
     */
    @CheckReturnValue
    @NonNull
    public static <T> T blockingFirst(Streamable<T> source) {
        return blockingFirst(source, new CompositeDisposable());
    }

    /**
     * 带外部 cancellation 的 blockingFirst：awaitNext 取首元素后 awaitFinish。
     * @param <T> 元素类型
     * @param source 源 {@code Streamable}
     * @param cancellation 外部取消管理器
     * @return 首个元素
     * @throws RuntimeException 上游抛出 unchecked 异常时
     * @throws CompletionException 上游抛出 checked 异常时
     */
    @CheckReturnValue
    @NonNull
    /** awaitNext 取 current()，合并 next/finish 异常后返回或抛 NoSuchElementException。 */
    public static <T> T blockingFirst(Streamable<T> source, StreamerCancellation cancellation) {
        var streamer = source.stream(cancellation);
        Throwable nextException = null;
        Throwable finishException = null;
        T result = null;
        try {
            if (streamer.awaitNext()) {
                result = streamer.current();
            }
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            nextException = ex;
        }
        try {
            streamer.awaitFinish();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            finishException = ex;
        }

        if (nextException != null || finishException != null) {
            throw ExceptionHelper.wrapOrThrow(ExceptionHelper.unwrapAndCombine(nextException, finishException));
        }
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    /**
     * 阻塞消费全部元素并返回最后一个；空序列抛 NoSuchElementException。
     * @param <T> 元素类型
     * @param source 源序列
     * @return 最后一个元素
     * @throws RuntimeException 上游抛出 unchecked 异常时
     * @throws CompletionException 上游抛出 checked 异常时
     */
    public static <T> T blockingLast(Streamable<T> source) {
        return blockingLast(source, new CompositeDisposable());
    }

    /**
     * 带 cancellation 的 blockingLast：while(awaitNext) 更新 result 后 awaitFinish。
     * @param <T> 元素类型
     * @param source 源序列
     * @param cancellation 外部取消管理器
     * @return 最后一个元素
     * @throws RuntimeException 上游抛出 unchecked 异常时
     * @throws CompletionException 上游抛出 checked 异常时
     */
    /** while(awaitNext) 循环更新 result，awaitFinish 后返回末元素。 */
    public static <T> T blockingLast(Streamable<T> source, StreamerCancellation cancellation) {
        var streamer = source.stream(cancellation);
        Throwable nextException = null;
        Throwable finishException = null;
        T result = null;
        try {
            while (streamer.awaitNext()) {
                result = streamer.current();
            }
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            nextException = ex;
        }
        try {
            streamer.awaitFinish();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            finishException = ex;
        }

        if (nextException != null || finishException != null) {
            throw ExceptionHelper.wrapOrThrow(ExceptionHelper.unwrapAndCombine(nextException, finishException));
        }
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }
}
