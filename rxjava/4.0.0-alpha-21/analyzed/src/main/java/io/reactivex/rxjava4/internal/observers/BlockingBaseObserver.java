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

import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 使用 {@link CountDownLatch} 等待终止的阻塞 observer 基类。
 *
 * @param <T> 值类型
 */
public abstract class BlockingBaseObserver<T> extends CountDownLatch
implements Observer<T>, Disposable {

    T value;
    Throwable error;

    Disposable upstream;

    volatile boolean cancelled;

    public BlockingBaseObserver() {
        super(1);
    }

    @Override
    public final void onSubscribe(Disposable d) {
        this.upstream = d;
        if (cancelled) {
            d.dispose();
        }
    }

    @Override
    public final void onComplete() {
        countDown();
    }

    @Override
    public final void dispose() {
        cancelled = true;
        Disposable d = this.upstream;
        if (d != null) {
            d.dispose();
        }
    }

    @Override
    public final boolean isDisposed() {
        return cancelled;
    }

    /**
     * 阻塞直到首个值到达并返回；若源为空则返回 null，
     * 若有异常则重新抛出。
     * @return 首个值，或源为空时返回 null
     */
    public final T blockingGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        Throwable e = error;
        if (e != null) {
            throw ExceptionHelper.wrapOrThrow(e);
        }
        return value;
    }
}
