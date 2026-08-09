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

package io.reactivex.rxjava4.subjects;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.*;

/**
 * 同时表示 {@link Observer} 与 {@link Observable}，可将单一源的事件多播给多个子 Observer。
 * <p>
 * 除 {@link #onSubscribe(io.reactivex.rxjava4.disposables.Disposable)}、{@link #onNext(Object)}、
 * {@link #onError(Throwable)}、{@link #onComplete()} 外，其余方法均为线程安全；
 * 可调用 {@link #toSerialized()} 使这些方法也线程安全。
 *
 * @param <T> 元素值类型
 */
public abstract class Subject<T> extends Observable<T> implements Observer<T> {
    /**
     * 若 subject 有任意 Observer 则返回 true。
     * <p>本方法线程安全。
     * @return 有 Observer 时为 true
     */
    @CheckReturnValue
    public abstract boolean hasObservers();

    /**
     * 若 subject 已通过 error 事件进入终止状态则返回 true。
     * <p>本方法线程安全。
     * @return 因 error 终止时为 true
     * @see #getThrowable()
     * @see #hasComplete()
     */
    @CheckReturnValue
    public abstract boolean hasThrowable();

    /**
     * 若 subject 已通过 complete 事件进入终止状态则返回 true。
     * <p>本方法线程安全。
     * @return 因 complete 终止时为 true
     * @see #hasThrowable()
     */
    @CheckReturnValue
    public abstract boolean hasComplete();

    /**
     * 返回导致 Subject 终止的错误；尚未终止时返回 null。
     * <p>本方法线程安全。
     * @return 终止错误，或尚未终止时为 null
     */
    @Nullable
    @CheckReturnValue
    public abstract Throwable getThrowable();

    /**
     * 包装本 Subject，串行化 onSubscribe、onNext、onError、onComplete 调用，使其线程安全。
     * <p>本方法线程安全。
     * @return 包装后的串行化 subject
     */
    @NonNull
    @CheckReturnValue
    public final Subject<T> toSerialized() {
        if (this instanceof SerializedSubject) {
            return this;
        }
        return new SerializedSubject<>(this);
    }
}
