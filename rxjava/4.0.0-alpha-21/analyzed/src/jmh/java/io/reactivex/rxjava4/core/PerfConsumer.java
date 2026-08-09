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

package io.reactivex.rxjava4.core;

import org.openjdk.jmh.infra.Blackhole;
import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 多类型同步消费者：实现 Flowable/Observer/Single/Completable/Maybe 接口，
 * 立即 request(Long.MAX_VALUE) 并消费事件到 Blackhole。
 */
@SuppressWarnings("exports")
public final class PerfConsumer implements FlowableSubscriber<Object>, Observer<Object>,
SingleObserver<Object>, CompletableObserver, MaybeObserver<Object> {

    final Blackhole bh;

    /** @param bh JMH Blackhole。 */
    public PerfConsumer(Blackhole bh) {
        this.bh = bh;
    }

    @Override
    public void onSuccess(Object value) {
        bh.consume(value);
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    /** 请求无界背压。 */
    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Object t) {
        bh.consume(t);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        bh.consume(true);
    }
}
