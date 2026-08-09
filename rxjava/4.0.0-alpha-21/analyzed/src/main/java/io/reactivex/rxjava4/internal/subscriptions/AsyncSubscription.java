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

import java.io.Serial;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 异步 Subscription：仲裁单个 upstream Subscription，
 * 并可持有单个 Disposable 资源；所有方法线程安全。
 */
public final class AsyncSubscription extends AtomicLong implements Subscription, Disposable {

    @Serial
    private static final long serialVersionUID = 7028635084060361255L;

    final AtomicReference<Subscription> actual;

    final AtomicReference<Disposable> resource;

    public AsyncSubscription() {
        resource = new AtomicReference<>();
        actual = new AtomicReference<>();
    }

    /** 可选预置 Disposable 资源。 */
    public AsyncSubscription(Disposable resource) {
        this();
        this.resource.lazySet(resource);
    }

    /** deferredRequest 转发或暂存 request。 */
    @Override
    public void request(long n) {
        SubscriptionHelper.deferredRequest(actual, this, n);
    }

    @Override
    public void cancel() {
        dispose();
    }

    /** cancel actual 并 dispose resource。 */
    @Override
    public void dispose() {
        SubscriptionHelper.cancel(actual);
        DisposableHelper.dispose(resource);
    }

    @Override
    public boolean isDisposed() {
        return actual.get() == SubscriptionHelper.CANCELLED;
    }

    /**
     * 设置新 Disposable 并 dispose 旧资源。
     * @param r 新资源
     * @return 已 cancel/dispose 时 false
     * @see #replaceResource(Disposable)
     */
    public boolean setResource(Disposable r) {
        return DisposableHelper.set(resource, r);
    }

    /**
     * 替换 Disposable 资源，不 dispose 旧值。
     * @param r 新资源
     * @return 已 cancel/dispose 时 false
     */
    public boolean replaceResource(Disposable r) {
        return DisposableHelper.replace(resource, r);
    }

    /**
     * deferredSetOnce 设置唯一 upstream Subscription。
     * @param s 首次设置的 Subscription
     */
    public void setSubscription(Subscription s) {
        SubscriptionHelper.deferredSetOnce(actual, this, s);
    }
}
