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

package io.reactivex.rxjava4.disposables;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.NonNull;

import java.io.Serial;

/**
 * 处理 {@link Subscription} 的 Disposable 容器。
 */
final class SubscriptionDisposable extends ReferenceDisposable<Subscription> {

    @Serial
    private static final long serialVersionUID = -707001650852963139L;

    /** @param value 要在 dispose 时 cancel 的 Subscription */
    SubscriptionDisposable(Subscription value) {
        super(value);
    }

    /** dispose 时调用 Subscription.cancel()。 */
    @Override
    protected void onDisposed(@NonNull Subscription value) {
        value.cancel();
    }
}
