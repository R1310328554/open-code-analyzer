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
 * 提供接收无值完成或错误推送通知的机制。
 * <p>
 * 当 {@code CompletableObserver} 通过 {@link CompletableSource#subscribe(CompletableObserver)} 订阅
 * {@link CompletableSource} 时，{@code CompletableSource} 会调用 {@link #onSubscribe(Disposable)}，
 * 传入可在任意时刻取消序列的 {@link Disposable}。
 * 行为良好的 {@code CompletableSource} 会恰好调用一次 {@link #onError(Throwable)} 或
 * {@link #onComplete()}，二者互斥，视为<strong>终止信号</strong>。
 * <p>
 * 对 {@code CompletableObserver} 方法的调用须串行化：不得多线程重叠调用，且须遵循下列协议：
 * <pre><code>    onSubscribe (onError | onComplete)?</code></pre>
 * <p>
 * 不建议同一 {@code CompletableObserver} 订阅多个 {@code CompletableSource}。
 * 若确需复用，实现须能处理多次回调并保证业务逻辑的并发正确性。
 * <p>
 * 以 {@code null} 参数调用 {@link #onSubscribe(Disposable)} 或 {@link #onError(Throwable)} 是禁止的。
 * <p>
 * {@code onXXX} 方法实现应避免抛出运行时异常，下列情况除外：
 * <ul>
 * <li>参数为 {@code null} 时可抛出 {@code NullPointerException}。
 * 注意 RxJava 会阻止 {@code null} 进入流，因此由标准源与中间算子组装的流通常无需空值检查。
 * </li>
 * <li>发生致命错误（如 {@code VirtualMachineError}）时。</li>
 * </ul>
 * @since 2.0
 */
public interface CompletableObserver {
    /**
     * 由 {@link Completable} 调用一次，在此实例上设置 {@link Disposable}，以便随时取消订阅。
     * @param d 用于取消时调用 dispose 的 {@code Disposable} 实例，不可为 null
     */
    void onSubscribe(@NonNull Disposable d);

    /**
     * 延迟计算正常完成时调用一次。
     */
    void onComplete();

    /**
     * 延迟计算“抛出”异常时调用一次。
     * @param e 异常，不可为 {@code null}
     */
    void onError(@NonNull Throwable e);
}
