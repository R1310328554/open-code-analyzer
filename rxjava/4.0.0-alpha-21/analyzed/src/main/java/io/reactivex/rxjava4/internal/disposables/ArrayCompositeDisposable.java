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

package io.reactivex.rxjava4.internal.disposables;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReferenceArray;

import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 具有固定槽位数量的复合 disposable。
 *
 * <p>注意：由于实现暴露了 {@link AtomicReferenceArray} 的方法，应仅调用 setResource、replaceResource 与 dispose；
 * 调用其它方法可能导致未定义行为，仅供内部使用。
 */
public final class ArrayCompositeDisposable extends AtomicReferenceArray<Disposable> implements Disposable {

    @Serial
    private static final long serialVersionUID = 2746389416410565408L;

    /** @param capacity 槽位数量 */
    public ArrayCompositeDisposable(int capacity) {
        super(capacity);
    }

    /**
     * 在指定索引设置资源并 dispose 旧资源。
     * @param index 要设置资源的索引
     * @param resource 新资源
     * @return 若资源已设置则为 true；若复合体已被 dispose 则为 false
     */
    public boolean setResource(int index, Disposable resource) {
        for (;;) {
            Disposable o = get(index);
            if (o == DisposableHelper.DISPOSED) {
                resource.dispose();
                return false;
            }
            if (compareAndSet(index, o, resource)) {
                if (o != null) {
                    o.dispose();
                }
                return true;
            }
        }
    }

    /**
     * 替换指定索引的资源并返回旧资源。
     * @param index 要替换资源的索引
     * @param resource 新资源
     * @return 旧资源，可为 null
     */
    public Disposable replaceResource(int index, Disposable resource) {
        for (;;) {
            Disposable o = get(index);
            if (o == DisposableHelper.DISPOSED) {
                resource.dispose();
                return null;
            }
            if (compareAndSet(index, o, resource)) {
                return o;
            }
        }
    }

    /** dispose 所有槽位中的资源。 */
    @Override
    public void dispose() {
        if (get(0) != DisposableHelper.DISPOSED) {
            int s = length();
            for (int i = 0; i < s; i++) {
                Disposable o = get(i);
                if (o != DisposableHelper.DISPOSED) {
                    o = getAndSet(i, DisposableHelper.DISPOSED);
                    if (o != DisposableHelper.DISPOSED && o != null) {
                        o.dispose();
                    }
                }
            }
        }
    }

    /** 若首个槽位标记为已 dispose 则返回 true。 */
    @Override
    public boolean isDisposed() {
        return get(0) == DisposableHelper.DISPOSED;
    }
}
