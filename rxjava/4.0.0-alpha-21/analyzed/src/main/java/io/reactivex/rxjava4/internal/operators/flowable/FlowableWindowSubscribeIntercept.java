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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.processors.FlowableProcessor;

/**
 * 包装 {@link FlowableProcessor}，检测是否有订阅者接入窗口。
 * @param <T> 元素类型
 * @since 3.0.0
 */
final class FlowableWindowSubscribeIntercept<T> extends Flowable<T> {

    final FlowableProcessor<T> window;

    final AtomicBoolean once;

    /** @param source 窗口底层 Processor */
    FlowableWindowSubscribeIntercept(FlowableProcessor<T> source) {
        this.window = source;
        this.once = new AtomicBoolean();
    }

    /** 订阅 window 并将 once 置 true。 */
    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        window.subscribe(s);
        once.set(true);
    }

    /** 若无订阅者则 CAS 标记放弃并返回 true。 */
    boolean tryAbandon() {
        return !once.get() && once.compareAndSet(false, true);
    }
}
