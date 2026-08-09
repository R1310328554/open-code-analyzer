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

package io.reactivex.rxjava4.internal.subscriptions;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.operators.QueueSubscription;

/**
 * 空 Subscription 单例：request 仅 validate，cancel 无操作；
 * 用于 error/complete 辅助方法向下游传递占位 Subscription。
 */
public enum EmptySubscription implements QueueSubscription<Object> {
    /** 无状态单例 INSTANCE。 */
    INSTANCE;

    @Override
    public void request(long n) {
        SubscriptionHelper.validate(n);
    }

    @Override
    public void cancel() {
        // no-op
    }

    @Override
    public String toString() {
        return "EmptySubscription";
    }

    /**
     * onSubscribe(INSTANCE) 后 onError(e)。
     * <p>仅应在下游尚未收到 Subscription 时调用。
     * @param e 错误
     * @param s 目标 Subscriber
     */
    public static void error(Throwable e, Subscriber<?> s) {
        s.onSubscribe(INSTANCE);
        s.onError(e);
    }

    /**
     * onSubscribe(INSTANCE) 后 onComplete。
     * <p>仅应在下游尚未收到 Subscription 时调用。
     * @param s 目标 Subscriber
     */
    public static void complete(Subscriber<?> s) {
        s.onSubscribe(INSTANCE);
        s.onComplete();
    }

    @Nullable
    @Override
    public Object poll() {
        return null; // always empty
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void clear() {
        // nothing to do
    }

    /** 接受 ASYNC fusion（后续仍会 onComplete/onError）。 */
    @Override
    public int requestFusion(int mode) {
        return mode & ASYNC; // accept async mode: an onComplete or onError will be signaled after anyway
    }

    @Override
    public boolean offer(Object value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override
    public boolean offer(Object v1, Object v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
