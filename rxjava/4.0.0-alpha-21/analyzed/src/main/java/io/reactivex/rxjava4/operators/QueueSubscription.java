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

package io.reactivex.rxjava4.operators;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * Flowable 侧融合队列：同时是 {@link SimpleQueue} 与 {@link Subscription}。
 * 订阅时 upstream 以本接口 onSubscribe，下游须在 request 前调用 {@link #requestFusion(int)}。
 *
 * <p><b>同步融合</b>：poll 同步取数，null 表终止；不应再 request。
 * <p><b>异步融合</b>：onNext(null) 提示可 poll，仍需 request 背压。
 *
 * <p>消费规则：poll/clear 须串行 drain-loop；poll 可能抛异常；
 * isEmpty 为 false 时 poll 仍可能因融合函数返回 null。
 *
 * <p>融合实现通常仅支持 poll、isEmpty、clear，其余抛 UnsupportedOperationException。
 *
 * @param <T> 队列元素类型
 * @see QueueDisposable
 * @since 3.1.1
 */
public interface QueueSubscription<@NonNull T> extends QueueFuseable<T>, Subscription {
}
