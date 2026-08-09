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

package io.reactivex.rxjava4.internal.subscribers;

import static io.reactivex.rxjava4.internal.util.ExceptionHelper.timeoutMessage;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.BlockingHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 同时实现 Subscriber 与 Future，期望恰好一个上游值，
 * 并通过（阻塞）Future API 提供该值。
 *
 * @param <T> 值类型
 */
public final class FutureSubscriber<T> extends CountDownLatch
implements FlowableSubscriber<T>, Future<T>, Subscription {

    T value;
    Throwable error;

    final AtomicReference<Subscription> upstream;

    public FutureSubscriber() {
        super(1);
        this.upstream = new AtomicReference<>();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        for (;;) {
            Subscription a = upstream.get();
            if (a == this || a == SubscriptionHelper.CANCELLED) {
                return false;
            }

            if (upstream.compareAndSet(a, SubscriptionHelper.CANCELLED)) {
                if (a != null) {
                    a.cancel();
                }
                countDown();
                return true;
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return upstream.get() == SubscriptionHelper.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (getCount() != 0) {
            BlockingHelper.verifyNonBlocking();
            await();
        }

        if (isCancelled()) {
            throw new CancellationException();
        }
        Throwable ex = error;
        if (ex != null) {
            throw new ExecutionException(ex);
        }
        return value;
    }

    @Override
    public T get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (getCount() != 0) {
            BlockingHelper.verifyNonBlocking();
            if (!await(timeout, unit)) {
                throw new TimeoutException(timeoutMessage(timeout, unit));
            }
        }

        if (isCancelled()) {
            throw new CancellationException();
        }

        Throwable ex = error;
        if (ex != null) {
            throw new ExecutionException(ex);
        }
        return value;
    }

    @Override
    public void onSubscribe(Subscription s) {
        SubscriptionHelper.setOnce(this.upstream, s, Long.MAX_VALUE);
    }

    /** 接收唯一元素；若收到多个元素则 cancel 上游并报告错误。 */
    @Override
    public void onNext(T t) {
        if (value != null) {
            upstream.get().cancel();
            onError(new IndexOutOfBoundsException("More than one element received"));
            return;
        }
        value = t;
    }

    @Override
    public void onError(Throwable t) {
        if (error == null) {
            Subscription a = upstream.get();
            if (a != this && a != SubscriptionHelper.CANCELLED
                    && upstream.compareAndSet(a, this)) {
                error = t;
                countDown();
                return;
            }
        }
        RxJavaPlugins.onError(t);
    }

    @Override
    public void onComplete() {
        if (value == null) {
            onError(new NoSuchElementException("The source is empty"));
            return;
        }
        Subscription a = upstream.get();
        if (a == this || a == SubscriptionHelper.CANCELLED) {
            return;
        }
        if (upstream.compareAndSet(a, this)) {
            countDown();
        }
    }

    /** 忽略：终止后 `this` 仅表示已完成的 Subscription。 */
    @Override
    public void cancel() {
        // ignoring as `this` means a finished Subscription only
    }

    /** 忽略：终止后 `this` 仅表示已完成的 Subscription。 */
    @Override
    public void request(long n) {
        // ignoring as `this` means a finished Subscription only
    }
}
