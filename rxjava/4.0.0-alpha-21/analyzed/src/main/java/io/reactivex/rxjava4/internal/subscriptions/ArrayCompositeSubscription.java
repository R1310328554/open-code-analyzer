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
import java.util.concurrent.atomic.AtomicReferenceArray;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 固定槽位的 Subscription 复合容器（继承 AtomicReferenceArray）。
 * <p>
 * 对外仅应调用 setResource/replaceResource/dispose；
 * 直接调用数组其它方法可能导致未定义行为。
 */
public final class ArrayCompositeSubscription extends AtomicReferenceArray<Subscription> implements Disposable {

    @Serial
    private static final long serialVersionUID = 2746389416410565408L;

    /** @param capacity 槽位数量 */
    public ArrayCompositeSubscription(int capacity) {
        super(capacity);
    }

    /**
     * 在 index 设置 Subscription 并 cancel 旧值。
     * @param index 槽位索引
     * @param resource 新 Subscription
     * @return 成功 true；已 dispose（CANCELLED）则 false
     */
    public boolean setResource(int index, Subscription resource) {
        for (;;) {
            Subscription o = get(index);
            if (o == SubscriptionHelper.CANCELLED) {
                if (resource != null) {
                    resource.cancel();
                }
                return false;
            }
            if (compareAndSet(index, o, resource)) {
                if (o != null) {
                    o.cancel();
                }
                return true;
            }
        }
    }

    /**
     * 替换 index 处 Subscription 并返回旧值（不 cancel 旧值）。
     * @param index 槽位索引
     * @param resource 新 Subscription
     * @return 旧 Subscription，可为 null
     */
    public Subscription replaceResource(int index, Subscription resource) {
        for (;;) {
            Subscription o = get(index);
            if (o == SubscriptionHelper.CANCELLED) {
                if (resource != null) {
                    resource.cancel();
                }
                return null;
            }
            if (compareAndSet(index, o, resource)) {
                return o;
            }
        }
    }

    /** 将所有槽位置 CANCELLED 并 cancel 各 Subscription。 */
    @Override
    public void dispose() {
        if (get(0) != SubscriptionHelper.CANCELLED) {
            int s = length();
            for (int i = 0; i < s; i++) {
                Subscription o = get(i);
                if (o != SubscriptionHelper.CANCELLED) {
                    o = getAndSet(i, SubscriptionHelper.CANCELLED);
                    if (o != SubscriptionHelper.CANCELLED && o != null) {
                        o.cancel();
                    }
                }
            }
        }
    }

    /** 槽位 0 是否为 CANCELLED。 */
    @Override
    public boolean isDisposed() {
        return get(0) == SubscriptionHelper.CANCELLED;
    }
}
