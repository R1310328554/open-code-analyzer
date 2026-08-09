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

package io.reactivex.rxjava4.internal.observers;

import java.util.concurrent.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 通过 CountDownLatch 等待成功或错误信号的合并 Observer。
 * @param <T> 值类型
 */
public final class BlockingMultiObserver<T>
extends CountDownLatch
implements SingleObserver<T>, CompletableObserver, MaybeObserver<T> {

    T value;
    Throwable error;

    Disposable upstream;

    volatile boolean cancelled;

    public BlockingMultiObserver() {
        super(1);
    }

    void dispose() {
        cancelled = true;
        Disposable d = this.upstream;
        if (d != null) {
            d.dispose();
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        this.upstream = d;
        if (cancelled) {
            d.dispose();
        }
    }

    @Override
    public void onSuccess(T value) {
        this.value = value;
        countDown();
    }

    @Override
    public void onError(Throwable e) {
        error = e;
        countDown();
    }

    @Override
    public void onComplete() {
        countDown();
    }

    /**
     * 阻塞直到 latch 计数归零，然后重新抛出收到的异常（checked 异常会包装），
     * 或返回收到的值（无值时为 null）。
     * @return 收到的值，无值时返回 null
     */
    public T blockingGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        Throwable ex = error;
        if (ex != null) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
        return value;
    }

    /**
     * 阻塞直到 latch 计数归零，然后重新抛出收到的异常（checked 异常会包装），
     * 或返回收到的值（无值时返回 defaultValue）。
     * @param defaultValue 未收到值时的默认返回值
     * @return 收到的值，无值时返回 defaultValue
     */
    public T blockingGet(T defaultValue) {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        Throwable ex = error;
        if (ex != null) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
        T v = value;
        return v != null ? v : defaultValue;
    }

    /**
     * 阻塞直到 observer 终止并返回 true；若等待超时则返回 false。
     * @param timeout 超时值
     * @param unit 时间单位
     * @return observer 及时终止则为 true，否则为 false
     */
    public boolean blockingAwait(long timeout, TimeUnit unit) {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                if (!await(timeout, unit)) {
                    dispose();
                    return false;
                }
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        Throwable ex = error;
        if (ex != null) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
        return true;
    }

    /**
     * 阻塞直到源完成并调用相应回调。
     * @param onSuccess 源成功时的回调
     * @param onError 源失败时的回调
     * @param onComplete 源为空时的回调
     */
    public void blockingConsume(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError, Action onComplete) {
        try {
            if (getCount() != 0) {
                try {
                    BlockingHelper.verifyNonBlocking();
                    await();
                } catch (InterruptedException ex) {
                    dispose();
                    onError.accept(ex);
                    return;
                }
            }
            Throwable ex = error;
            if (ex != null) {
                onError.accept(ex);
                return;
            }
            T v = value;
            if (v != null) {
                onSuccess.accept(v);
            } else {
                onComplete.run();
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            RxJavaPlugins.onError(t);
        }
    }
}
