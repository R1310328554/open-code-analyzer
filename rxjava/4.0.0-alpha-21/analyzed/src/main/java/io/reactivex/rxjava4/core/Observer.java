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
 * 提供接收 push 式通知的机制。
 * <p>
 * 当 {@code Observer} 通过 {@link ObservableSource#subscribe(Observer)} 方法订阅 {@link ObservableSource} 时，
 * {@code ObservableSource} 会调用 {@link #onSubscribe(Disposable)} 并传入 {@link Disposable}，
 * 允许随时 dispose 序列；随后 {@code ObservableSource} 可任意次数调用 Observer 的 {@link #onNext} 方法
 * 以提供通知。行为良好的 {@code ObservableSource} 会恰好一次调用 {@code Observer} 的 {@link #onComplete}，
 * 或恰好一次调用 {@code Observer} 的 {@link #onError}。
 * <p>
 * 调用 {@code Observer} 的方法必须串行进行，即不可被多线程以重叠方式并发调用，
 * 且调用模式须遵循以下协议：
 * <pre><code>    onSubscribe onNext* (onError | onComplete)?</code></pre>
 * <p>
 * 不建议将同一 {@code Observer} 订阅多个 {@code ObservableSource}。若发生此类复用，
 * {@code Observer} 实现者有责任准备好接收多次方法调用，并确保其业务逻辑的并发行为正确。
 * <p>
 * 禁止以 {@code null} 参数调用 {@link #onSubscribe(Disposable)}、{@link #onNext(Object)} 或 {@link #onError(Throwable)}。
 * <p>
 * {@code onXXX} 方法的实现应避免抛出运行时异常，以下情况除外
 *（参见 Reactive Streams 规范 <a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>）：
 * <ul>
 * <li>若参数为 {@code null}，方法可抛出 {@code NullPointerException}。
 * 但 RxJava 会阻止 {@code null} 进入流，因此由标准源与中间算子组装的流通常无需检查 null。
 * </li>
 * <li>若发生致命错误（如 {@code VirtualMachineError}）。</li>
 * </ul>
 * <p>
 * 违反 Rule 2.13 会导致未定义的流行为。通常可能出现以下情况：
 * <ul>
 * <li>上游算子将其转为 {@link #onError} 调用。</li>
 * <li>若流为同步，{@link ObservableSource#subscribe(Observer)} 会抛出异常而非正常返回。</li>
 * <li>若流为异步，异常会向上传播至提供异步边界的组件（{@link Scheduler} 或 {@link java.util.concurrent.Executor}），
 * 并将异常路由到全局 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 处理器，
 * 或当前线程的 {@link java.lang.Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)} 处理器。</li>
 * </ul>
 * 从 {@code Observable} 的角度看，{@code Observer} 是终端消费者，因此处理错误并向下游传递信号是 {@code Observer} 的责任。
 * 这意味着 {@code onXXX} 方法中不可靠的代码应包在 `try-catch` 中，尤其在 {@link #onError(Throwable)} 或 {@link #onComplete()} 中处理
 *（例如记录日志或向用户展示错误对话框）。但若错误从 {@link #onNext(Object)} 抛出，
 * <a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a> 要求实现调用 {@link Disposable#dispose()}，
 * 并以适合目标上下文的方式发出异常信号，例如在同一 {@code Observer} 实例上调用 {@link #onError(Throwable)}。
 * <p>
 * 若 {@code Observer} 因某种原因无法遵循 Rule 2.13，{@link Observable#safeSubscribe(Observer)} 可为其添加必要防护，
 * 将 {@code onNext} 抛出的异常路由到 {@code onError}，并将 {@code onError} 与 {@code onComplete} 抛出的异常
 * 通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 路由到全局错误处理器。
 * @see <a href="http://reactivex.io/documentation/observable.html">ReactiveX documentation: Observable</a>
 * @param <T>
 *          Observer 期望观察的元素类型
 */
public interface Observer<@NonNull T> {

    /**
     * 为 {@link Observer} 提供以同步（在 {@link #onNext(Object)} 内）或异步方式
     * 取消（dispose）与 {@link Observable} 连接（通道）的手段。
     * @param d 可随时调用 {@link Disposable#dispose()} 以取消连接的 {@link Disposable} 实例
     * @since 2.0
     */
    void onSubscribe(@NonNull Disposable d);

    /**
     * 向 {@link Observer} 提供新的待观察元素。
     * <p>
     * {@link Observable} 可调用本方法 0 次或多次。
     * <p>
     * {@code Observable} 在调用 {@link #onComplete} 或 {@link #onError} 之后不会再调用本方法。
     *
     * @param t
     *          Observable 发射的元素
     */
    void onNext(@NonNull T t);

    /**
     * 通知 {@link Observer}：{@link Observable} 遇到错误。
     * <p>
     * 若 {@code Observable} 调用本方法，此后不会再调用 {@link #onNext} 或 {@link #onComplete}。
     *
     * @param e
     *          Observable 遇到的异常
     */
    void onError(@NonNull Throwable e);

    /**
     * 通知 {@link Observer}：{@link Observable} 已完成 push 式通知的发送。
     * <p>
     * 若 {@code Observable} 调用了 {@link #onError}，则不会调用本方法。
     */
    void onComplete();

}
