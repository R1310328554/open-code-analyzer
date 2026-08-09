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

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.functions.*;

/**
 * Reactive Streams {@link java.util.concurrent.Flow.Subscriber} 的抽象，允许关联资源
 * 并暴露下游当前请求数量。
 * <p>
 * {@link #onNext(Object)}、{@link #onError(Throwable)}、{@link #tryOnError(Throwable)}
 * 与 {@link #onComplete()} 方法应像 {@link java.util.concurrent.Flow.Subscriber Subscriber}
 * 的方法一样顺序调用。若要保证这一点，请使用 {@link #serialize()} 返回的 {@code FlowableEmitter}
 * 而非生成器例程提供的原始 {@code FlowableEmitter} 实例。其他方法为线程安全。
 * <p>
 * emitter 允许通过 {@link #setDisposable(Disposable)} 或 {@link #setCancellable(Cancellable)}
 * 分别注册单个 {@link Disposable} 或 {@link Cancellable} 资源。当下游取消流，或事件生成逻辑调用
 * {@link #onError(Throwable)}、{@link #onComplete()}，或 {@link #tryOnError(Throwable)} 成功后，
 * emitter 实现将 dispose/cancel 该实例。
 * <p>
 * 同一时刻 emitter 只能关联一个 {@code Disposable} 或 {@code Cancellable} 对象。
 * 调用任一 {@code set} 方法会 dispose/cancel 先前的对象。若需管理多个资源，
 * 可创建 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 并关联到 emitter。
 * <p>
 * {@link Cancellable} 在逻辑上等价于 {@code Disposable}，但允许使用可能抛出 checked 异常的清理逻辑
 *（例如许多 Java IO 组件的 {@code close()} 方法）。由于资源释放在终端事件交付后或序列取消后进行，
 * {@code Cancellable} 内抛出的异常将通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}
 * 路由到全局错误处理器。
 *
 * @param <T> 发射的值类型
 */
public interface FlowableEmitter<@NonNull T> extends Emitter<T> {

    /**
     * 在本 emitter 上设置 Disposable；任何先前的 {@link Disposable}
     * 或 {@link Cancellable} 将被 dispose/cancel。
     * <p>本方法为线程安全。
     * @param d disposable，允许为 {@code null}
     */
    void setDisposable(@Nullable Disposable d);

    /**
     * 在本 emitter 上设置 {@link Cancellable}；任何先前的 {@link Disposable}
     * 或 {@code Cancellable} 将被 dispose/cancel。
     * <p>本方法为线程安全。
     * @param c {@code Cancellable} 资源，允许为 {@code null}
     */
    void setCancellable(@Nullable Cancellable c);

    /**
     * 当前未完成的请求数量。
     * <p>本方法为线程安全。
     * @return 当前未完成的请求数量
     */
    long requested();

    /**
     * 若下游已取消序列，或通过 {@link #onError(Throwable)}、{@link #onComplete}
     * 或成功的 {@link #tryOnError(Throwable)} 终止 emitter，则返回 true。
     * <p>本方法为线程安全。
     * @return 若下游已取消序列或 emitter 已终止则为 true
     */
    boolean isCancelled();

    /**
     * 确保对 {@code onNext}、{@code onError} 与 {@code onComplete} 的调用被正确串行化。
     * @return 串行化后的 {@link FlowableEmitter}
     */
    @NonNull
    FlowableEmitter<T> serialize();

    /**
     * 若下游未取消序列且未以其他方式终止，尝试发射指定 {@link Throwable} 错误；
     * 若因生命周期限制不允许发射则返回 false。
     * <p>
     * 与 {@link #onError(Throwable)} 不同，若错误无法交付，不会调用
     * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}。
     * <p>History: 2.1.1 - experimental
     * @param t 若可能则发射的 throwable 错误
     * @return 成功则为 true；若下游无法接受更多事件则为 false
     * @since 2.2
     */
    boolean tryOnError(@NonNull Throwable t);
}
