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
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Flow.*;

/**
 * 布尔取消 Subscription：request 仅做 validate，
 * cancel 置 true，可通过 isCancelled 查询。
 */
public final class BooleanSubscription extends AtomicBoolean implements Subscription {

    @Serial
    private static final long serialVersionUID = -8127758972444290902L;

    @Override
    public void request(long n) {
        SubscriptionHelper.validate(n);
    }

    /** lazySet(true) 标记已取消。 */
    @Override
    public void cancel() {
        lazySet(true);
    }

    /**
     * 是否已 cancel。
     * @return 已取消则 true
     */
    public boolean isCancelled() {
        return get();
    }

    @Override
    public String toString() {
        return "BooleanSubscription(cancelled=" + get() + ")";
    }
}
