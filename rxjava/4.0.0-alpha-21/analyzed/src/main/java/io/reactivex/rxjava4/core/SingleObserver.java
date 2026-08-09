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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 提供接收单个值或错误的 push 式通知机制。
 * <p>
 * 当 {@code SingleObserver} 通过 {@link SingleSource#subscribe(SingleObserver)} 订阅 {@link SingleSource} 时，
 * {@code SingleSource} 会调用 {@link #onSubscribe(Disposable)} 并传入 {@link Disposable}，允许随时 dispose 序列。
 * 行为良好的 {@code SingleSource} 会恰好一次调用 {@code SingleObserver} 的 {@link #onSuccess(Object)}，
 * 或恰好一次调用 {@code SingleObserver} 的 {@link #onError}，二者被视为互斥的 <strong>终端信号</strong>。
 * <p>
 * 调用 {@code SingleObserver} 的方法必须串行进行，即不可被多线程以重叠方式并发调用，
 * 且调用模式须遵循以下协议：
 * <pre><code>    onSubscribe (onSuccess | onError)?</code></pre>
 * <p>
 * 不建议将同一 {@code SingleObserver} 订阅多个 {@code SingleSource}。若发生此类复用，
 * {@code SingleObserver} 实现者有责任准备好接收多次方法调用，并确保其业务逻辑的并发行为正确。
 * <p>
 * 禁止以 {@code null} 参数调用 {@link #onSubscribe(Disposable)}、{@link #onSuccess(Object)} 或 {@link #onError(Throwable)}。
 * <p>
 * {@code onXXX} 方法的实现应避免抛出运行时异常，以下情况除外：
 * <ul>
 * <li>若参数为 {@code null}，方法可抛出 {@code NullPointerException}。
 * 但 RxJava 会阻止 {@code null} 进入流，因此由标准源与中间算子组装的流通常无需检查 null。
 * </li>
 * <li>若发生致命错误（如 {@code VirtualMachineError}）。</li>
 * </ul>
 * @see <a href="http://reactivex.io/documentation/observable.html">ReactiveX documentation: Observable</a>
 * @param <T>
 *          SingleObserver 期望观察的元素类型
 * @since 2.0
 */
public interface SingleObserver<@NonNull T> {

    /**
     * 为 {@link SingleObserver} 提供以同步（在 {@code onSubscribe(Disposable)} 内）或异步方式
     * 取消（dispose）与 Single 连接（通道）的手段。
     * @param d 可随时调用 {@link Disposable#dispose()} 以取消连接的 Disposable 实例
     * @since 2.0
     */
    void onSubscribe(@NonNull Disposable d);

    /**
     * 向 {@link SingleObserver} 通知单个元素，并表明 {@link Single} 已完成 push 式通知的发送。
     * <p>
     * 若 {@code Single} 调用了 {@link #onError}，则不会调用本方法。
     *
     * @param t
     *          {@code Single} 发射的元素
     */
    void onSuccess(@NonNull T t);

    /**
     * 通知 {@link SingleObserver}：{@link Single} 遇到错误。
     * <p>
     * 若 {@code Single} 调用本方法，此后不会再调用 {@link #onSuccess}。
     *
     * @param e
     *          {@code Single} 遇到的异常
     */
    void onError(@NonNull Throwable e);
}
