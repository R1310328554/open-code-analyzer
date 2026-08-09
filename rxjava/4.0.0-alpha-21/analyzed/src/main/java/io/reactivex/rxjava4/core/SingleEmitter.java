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
 * RxJava {@link SingleObserver} 的抽象，允许关联资源。
 * <p>
 * 所有方法可从多线程安全调用，但无法保证哪个终端事件会胜出并交付给下游。
 * <p>
 * 多次调用 {@link #onSuccess(Object)} 无效果。
 * 多次调用 {@link #onError(Throwable)} 或在 {@code onSuccess} 之后调用，
 * 会将异常通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 路由到全局错误处理器。
 * <p>
 * emitter 允许通过 {@link #setDisposable(Disposable)} 或 {@link #setCancellable(Cancellable)}
 * 分别注册单个 {@link Disposable} 或 {@link Cancellable} 资源。当下游取消流，或事件生成逻辑调用
 * {@link #onSuccess(Object)}、{@link #onError(Throwable)}，或 {@link #tryOnError(Throwable)} 成功后，
 * emitter 实现将 dispose/cancel 该实例。
 * <p>
 * 同一时刻 emitter 只能关联一个 {@code Disposable} 或 {@code Cancellable} 对象。
 * 调用任一 {@code set} 方法会 dispose/cancel 先前的对象。若需管理多个资源，
 * 可创建 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 并关联到 emitter。
 * <p>
 * {@link Cancellable} 在逻辑上等价于 {@code Disposable}，但允许使用可能抛出 checked 异常的清理逻辑
 *（例如许多 Java IO 组件的 {@code close()} 方法）。资源释放在终端事件交付后或序列取消后进行，
 * {@code Cancellable} 内抛出的异常将通过 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}
 * 路由到全局错误处理器。
 *
 * @param <T> 发射的值类型
 */
public interface SingleEmitter<@NonNull T> {

    /**
     * 发射成功值。
     * @param t 值，不可为 null
     */
    void onSuccess(@NonNull T t);

    /**
     * 发射异常。
     * @param t 异常，不可为 {@code null}
     */
    void onError(@NonNull Throwable t);

    /**
     * 在本 emitter 上设置 {@link Disposable}；任何先前的 {@code Disposable}
     * 或 {@link Cancellable} 将被 dispose/cancel。
     * <p>本方法为线程安全。
     * @param d {@code Disposable}，允许为 {@code null}
     */
    void setDisposable(@Nullable Disposable d);

    /**
     * 在本 emitter 上设置 Cancellable；任何先前的 {@link Disposable}
     * 或 {@link Cancellable} 将被 dispose/cancel。
     * <p>本方法为线程安全。
     * @param c {@code Cancellable} 资源，允许为 {@code null}
     */
    void setCancellable(@Nullable Cancellable c);

    /**
     * 若下游已 dispose 序列，或通过 {@link #onSuccess(Object)}、{@link #onError(Throwable)}
     * 或成功的 {@link #tryOnError(Throwable)} 终止 emitter，则返回 true。
     * <p>本方法为线程安全。
     * @return 若下游已 dispose 序列或 emitter 已终止则为 true
     */
    boolean isDisposed();

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
