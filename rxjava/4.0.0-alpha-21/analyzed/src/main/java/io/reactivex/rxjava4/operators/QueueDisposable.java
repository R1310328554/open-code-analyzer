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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 扩展 {@link SimpleQueue} 与 {@link Disposable}，用于协商
 * {@link io.reactivex.rxjava4.core.Observable Observable} 链上相邻算子之间的融合模式。
 * <p>
 * 协商发生在订阅时：上游以本接口实例调用 {@code onSubscribe}，
 * 下游须在 {@code request()} 前调用 {@link #requestFusion(int)} 指定模式。
 * <p>
 * <b>同步融合</b>：上游值已就绪或在 {@link #poll()} 同步调用时生成；
 * {@link #poll()} 返回 {@code null} 表示流已终止，上游不再调用 onXXX。
 * <p>
 * <b>异步融合</b>：值可能稍后通过 {@link #poll()} 可用；
 * onError/onComplete 照常，但 onNext 以 {@code null} 代替实际值，
 * 下游应将其视为可调用 {@link #poll()} 的信号。
 * <p>
 * 消费 {@link SimpleQueue} 的一般规则：
 * <ul>
 * <li>{@link #poll()} 与 {@link #clear()} 须在串行 drain-loop 中顺序调用。</li>
 * <li>{@link #poll()} 调用方应准备捕获异常。</li>
 * <li>因计算附着于 {@link #poll()}，即使 {@link #isEmpty()} 曾为 false，{@link #poll()} 仍可能返回 {@code null}。</li>
 * </ul>
 * <p>
 * 实现应仅允许调用 {@link #poll()}、{@link #isEmpty()}、{@link #clear()}，
 * 其余 {@link SimpleQueue} 方法应抛出 {@link UnsupportedOperationException}。
 * @param <T> 经队列传递的值类型
 * @see QueueSubscription
 * @since 3.1.1
 */
public interface QueueDisposable<@NonNull T> extends QueueFuseable<T>, Disposable {
}
