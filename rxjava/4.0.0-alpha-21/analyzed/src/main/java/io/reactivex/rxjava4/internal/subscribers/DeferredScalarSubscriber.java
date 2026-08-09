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

package io.reactivex.rxjava4.internal.subscribers;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.internal.subscriptions.*;

import java.io.Serial;

/**
 * 继承 {@link DeferredScalarSubscription} 的 subscriber：
 * 无界请求上游，可生成 0 或 1 个结果值。
 * @param <T> 输入值类型
 * @param <R> 输出值类型
 */
public abstract class DeferredScalarSubscriber<T, R> extends DeferredScalarSubscription<R>
implements FlowableSubscriber<T> {

    @Serial
    private static final long serialVersionUID = 2984505488220891551L;

    /** 上游 subscription。 */
    protected Subscription upstream;

    /** 是否至少收到一次 onNext。 */
    protected boolean hasValue;

    /**
     * 创建 DeferredScalarSubscriber 实例并包装下游 Subscriber。
     * @param downstream 下游 subscriber，非 null（未校验）
     */
    public DeferredScalarSubscriber(Subscriber<? super R> downstream) {
        super(downstream);
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {
            this.upstream = s;

            downstream.onSubscribe(this);

            s.request(Long.MAX_VALUE);
        }
    }

    @Override
    public void onError(Throwable t) {
        value = null;
        downstream.onError(t);
    }

    @Override
    public void onComplete() {
        if (hasValue) {
            complete(value);
        } else {
            downstream.onComplete();
        }
    }

    /** 取消自身并 cancel 上游 subscription。 */
    @Override
    public void cancel() {
        super.cancel();
        upstream.cancel();
    }
}
