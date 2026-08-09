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
 * 提供接收单个值、错误或无值完成推送通知的机制。
 * <p>
 * 当 {@code MaybeObserver} 通过 {@link MaybeSource#subscribe(MaybeObserver)} 订阅
 * {@link MaybeSource} 时，{@code MaybeSource} 会调用 {@link #onSubscribe(Disposable)}，
 * 传入可在任意时刻取消序列的 {@link Disposable}。
 * 行为良好的 {@code MaybeSource} 会恰好调用一次 {@link #onSuccess(Object)}、
 * {@link #onError(Throwable)} 或 {@link #onComplete()}，三者互斥，视为<strong>终止信号</strong>。
 * <p>
 * 对 {@code MaybeObserver} 方法的调用须串行化：不得多线程重叠调用，且须遵循下列协议：
 * <pre><code>    onSubscribe (onSuccess | onError | onComplete)?</code></pre>
 * <p>
 * 注意：与 {@code Observable} 协议不同，通过 {@link #onSuccess(Object)} 发出成功项后
 * 不会调用 {@link #onComplete()}。
 * <p>
 * 不建议同一 {@code MaybeObserver} 订阅多个 {@code MaybeSource}。
 * 若确需复用，实现须能处理多次回调并保证业务逻辑的并发正确性。
 * <p>
 * 以 {@code null} 参数调用 {@link #onSubscribe(Disposable)}、{@link #onSuccess(Object)}
 * 或 {@link #onError(Throwable)} 是禁止的。
 * <p>
 * {@code onXXX} 方法实现应避免抛出运行时异常，下列情况除外：
 * <ul>
 * <li>参数为 {@code null} 时可抛出 {@code NullPointerException}。
 * 注意 RxJava 会阻止 {@code null} 进入流，因此由标准源与中间算子组装的流通常无需空值检查。
 * </li>
 * <li>发生致命错误（如 {@code VirtualMachineError}）时。</li>
 * </ul>
 * @see <a href="http://reactivex.io/documentation/observable.html">ReactiveX documentation: Observable</a>
 * @param <T>
 *          {@code MaybeObserver} 期望观察的元素类型
 * @since 2.0
 */
public interface MaybeObserver<@NonNull T> {

    /**
     * 为 {@link MaybeObserver} 提供同步（在 {@code onSubscribe(Disposable)} 内）
     * 与异步取消（dispose）与 {@link Maybe} 连接（通道）的手段。
     * @param d 可随时调用 {@link Disposable#dispose()} 以取消连接的 {@link Disposable} 实例
     */
    void onSubscribe(@NonNull Disposable d);

    /**
     * 向 {@link MaybeObserver} 通知一个元素，并表明 {@link Maybe} 已完成推送通知。
     * <p>
     * 若 {@link Maybe} 调用了 {@link #onError}，则不会调用本方法。
     *
     * @param t
     *          {@code Maybe} 发射的元素
     */
    void onSuccess(@NonNull T t);

    /**
     * 通知 {@link MaybeObserver} {@link Maybe} 遇到错误。
     * <p>
     * 若 {@link Maybe} 调用本方法，则此后不会调用 {@link #onSuccess}。
     *
     * @param e
     *          {@code Maybe} 遇到的异常
     */
    void onError(@NonNull Throwable e);

    /**
     * 延迟计算正常完成时调用一次。
     */
    void onComplete();
}
