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

package io.reactivex.rxjava4.internal.operators.observable;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.subjects.Subject;

/**
 * 包装 Subject 以检测是否有下游订阅。
 * 若窗口未被订阅，{@link #tryAbandon} 可提前关闭空窗。
 * @param <T> 流元素类型
 * @since 3.0.0
 */
final class ObservableWindowSubscribeIntercept<T> extends Observable<T> {

    final Subject<T> window;

    final AtomicBoolean once;

    ObservableWindowSubscribeIntercept(Subject<T> source) {
        this.window = source;
        this.once = new AtomicBoolean();
    }

    /** 订阅底层 window 并标记 once。 */
    @Override
    protected void subscribeActual(Observer<? super T> s) {
        window.subscribe(s);
        once.set(true);
    }

    /** 尚无订阅者时 CAS 置位，表示可放弃该空窗口。 */
    boolean tryAbandon() {
        return !once.get() && once.compareAndSet(false, true);
    }
}
