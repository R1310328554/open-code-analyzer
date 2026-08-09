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
import io.reactivex.rxjava4.functions.Cancellable;

/**
 * 对 RxJava {@link CompletableObserver} 的抽象，允许为其关联资源。
 * <p>
 * 所有方法均可从多线程安全调用，但不保证哪个终止事件会最终送达下游。
 * <p>
 * 多次调用 {@link #onComplete()} 无效果。
 * 多次调用 {@link #onError(Throwable)}，或在 {@code onComplete} 之后调用，会将异常路由至
 * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} 全局错误处理器。
 * <p>
 * 发射器允许通过 {@link #setDisposable(Disposable)} 或 {@link #setCancellable(Cancellable)}
 * 注册单个 {@link Disposable} 或 {@link Cancellable} 资源。
 * 当下游取消流、事件生成逻辑调用 {@link #onError(Throwable)}、{@link #onComplete()}，
 * 或 {@link #tryOnError(Throwable)} 成功时，实现会 dispose/cancel 该资源。
 * <p>
 * 同一时刻只能关联一个 {@code Disposable} 或 {@code Cancellable}；
 * 再次调用任一 {@code set} 方法会 dispose/cancel 先前的对象。
 * 若需管理多个资源，可创建 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 并关联到发射器。
 * <p>
 * {@link Cancellable} 在逻辑上等价于 {@code Disposable}，但允许使用可能抛出受检异常的清理逻辑
 * （例如 Java IO 组件的 {@code close()}）。
 * 资源释放在终止事件送达或序列取消之后进行；{@code Cancellable} 内抛出的异常会路由至
 * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}。
 */
public interface CompletableEmitter {

    /**
     * 发出完成信号。
     */
    void onComplete();

    /**
     * 发出异常信号。
     * @param t 异常，不可为 null
     */
    void onError(@NonNull Throwable t);

    /**
     * 在此发射器上设置 {@link Disposable}；先前的 {@link Disposable} 或 {@link Cancellable} 会被 dispose/cancel。
     * @param d 可释放资源，允许为 null
     */
    void setDisposable(@Nullable Disposable d);

    /**
     * 在此发射器上设置 {@link Cancellable}；先前的 {@link Disposable} 或 {@link Cancellable} 会被 dispose/cancel。
     * @param c 可取消资源，允许为 null
     */
    void setCancellable(@Nullable Cancellable c);

    /**
     * 若下游已 dispose 序列，或发射器已通过 {@link #onError(Throwable)}、{@link #onComplete}
     * 或成功的 {@link #tryOnError(Throwable)} 终止，则返回 true。
     * <p>本方法线程安全。
     * @return 若下游已 dispose 或发射器已终止则为 true
     */
    boolean isDisposed();

    /**
     * 若下游尚未取消或终止序列，尝试发出指定 {@link Throwable} 错误；
     * 若因生命周期限制不允许发出，则返回 false。
     * <p>
     * 与 {@link #onError(Throwable)} 不同，若错误无法送达，不会调用
     * {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxjavaPlugins.onError}。
     * <p>历史：2.1.1 — 实验性
     * @param t 待发出的异常
     * @return 成功发出则为 true；若下游无法再接收事件则为 false
     * @since 2.2
     */
    boolean tryOnError(@NonNull Throwable t);
}
