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

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 使用 {@link CountDownLatch} 等待终止的阻塞 subscriber 基类。
 *
 * @param <T> 值类型
 */
public abstract class BlockingBaseSubscriber<T> extends CountDownLatch
implements FlowableSubscriber<T> {

    T value;
    Throwable error;

    Subscription upstream;

    volatile boolean cancelled;

    public BlockingBaseSubscriber() {
        super(1);
    }

    /** 验证 subscription 并请求 {@code Long.MAX_VALUE}；取消时同步 cancel 上游。 */
    @Override
    public final void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {
            this.upstream = s;
            if (!cancelled) {
                s.request(Long.MAX_VALUE);
                if (cancelled) {
                    this.upstream = SubscriptionHelper.CANCELLED;
                    s.cancel();
                }
            }
        }
    }

    @Override
    public final void onComplete() {
        countDown();
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
                Subscription s = this.upstream;
                this.upstream = SubscriptionHelper.CANCELLED;
                if (s != null) {
                    s.cancel();
                }
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
