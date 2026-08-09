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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.functions.*;
import io.reactivex.rxjava4.internal.operators.observable.*;
import io.reactivex.rxjava4.internal.util.ConnectConsumer;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;
import io.reactivex.rxjava4.schedulers.Schedulers;

/**
 * {@code ConnectableObservable} 类似普通 {@link Observable}，但不会在订阅时立即开始发射元素，
 * 仅在其 {@link #connect} 方法被调用后才开始。这样你可以等待所有预期的 {@link Observer}
 * 对 {@code Observable} 执行 {@link Observable#subscribe} 之后，再让 {@code Observable} 开始发射。
 * <p>
 * <img width="640" height="510" src="https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/publishConnect.v3.png" alt="">
 * <p>
 * 当上游终止时，{@code ConnectableObservable} 保持该终止状态，
 * 并根据底层实现将缓存的事件转发给迟到的 {@code Observer}。
 * 若要复用并重启此 {@code ConnectableObservable}，必须调用 {@link #reset()} 方法。
 * 调用后，此 {@code ConnectableObservable} 对新 {@code Observer} 将呈现为全新的未连接源。
 * 释放连接会将 {@code ConnectableObservable} 重置为初始状态，此情况下无需再调用 {@link #reset()}。
 * <p>
 * 尽管 {@link #connect()} 与 {@link #reset()} 可从多线程安全调用，仍建议由专用线程或业务逻辑
 * 管理 {@code ConnectableObservable} 的连接或重置，以避免在 {@code Observer} 仍在订阅、
 * 尚未从一开始就接收信号时，过早调用 {@code connect()} 或 {@code reset()} 造成意外信号丢失。
 *
 * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Connectable-Observable-Operators">RxJava Wiki: Connectable Observable Operators</a>
 * @param <T>
 *          {@code ConnectableObservable} 发射元素的类型
 */
public abstract class ConnectableObservable<T> extends Observable<T> {

    /**
     * 指示 {@code ConnectableObservable} 开始将其底层 {@link Observable} 的元素发射给 {@link Observer}。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>行为由本抽象类的实现者决定。</dd>
     * </dl>
     *
     * @param connection
     *          在订阅源之前接收连接订阅的操作，
     *          允许调用方同步断开同步源
     * @throws NullPointerException 若 {@code connection} 为 {@code null}
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">ReactiveX documentation: Connect</a>
     */
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract void connect(@NonNull Consumer<? super Disposable> connection);

    /**
     * 若本 {@code ConnectableObservable} 已终止或已被释放，则将其重置为初始状态。
     * <p>
     * 对全新或活跃的 {@code ConnectableObservable} 调用此方法无效果。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>行为由本抽象类的实现者决定。</dd>
     * </dl>
     * @since 3.0.0
     */
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract void reset();

    /**
     * 指示 {@code ConnectableObservable} 开始将其底层 {@link Observable} 的元素发射给 {@link Observer}。
     * <p>
     * 若要断开同步源，请使用 {@link #connect(Consumer)} 方法。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>行为由本抽象类的实现者决定。</dd>
     * </dl>
     *
     * @return 表示连接的 {@link Disposable}
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">ReactiveX documentation: Connect</a>
     */
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public final Disposable connect() {
        ConnectConsumer cc = new ConnectConsumer();
        connect(cc);
        return cc.disposable;
    }

    /**
     * 返回一个 {@link Observable}，只要本 {@code ConnectableObservable} 至少存在一个订阅，
     * 它就保持与本 {@code ConnectableObservable} 的连接。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>此 {@code refCount} 重载不在任何特定 {@link Scheduler} 上运行。</dd>
     * </dl>
     * @return 新的 {@code Observable} 实例
     * @see <a href="http://reactivex.io/documentation/operators/refcount.html">ReactiveX documentation: RefCount</a>
     * @see #refCount(int)
     * @see #refCount(long, TimeUnit)
     * @see #refCount(int, long, TimeUnit)
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> refCount() {
        return RxJavaPlugins.onAssembly(new ObservableRefCount<>(this));
    }

    /**
     * 当已订阅的 observer 数量达到指定值时连接上游 {@code ConnectableObservable}，
     * 若所有 {@link Observer} 均已取消订阅则断开连接。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>此 {@code refCount} 重载不在任何特定 {@link Scheduler} 上运行。</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param observerCount 连接上游所需的 {@code Observer} 数量
     * @return 新的 {@link Observable} 实例
     * @throws IllegalArgumentException 若 {@code observerCount} 非正数
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    @NonNull
    public final Observable<T> refCount(int observerCount) {
        return refCount(observerCount, 0, TimeUnit.NANOSECONDS, Schedulers.trampoline());
    }

    /**
     * 当已订阅的 observer 数量达到 1 时连接上游 {@code ConnectableObservable}，
     * 若所有 {@link Observer} 均已取消订阅，则在指定超时后断开连接。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>此 {@code refCount} 重载在 {@code computation} {@link Scheduler} 上运行。</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param timeout 所有 {@code Observer} 取消订阅后断开连接前的等待时间
     * @param unit 超时时间单位
     * @return 新的 {@link Observable} 实例
     * @throws NullPointerException 若 {@code unit} 为 {@code null}
     * @see #refCount(long, TimeUnit, Scheduler)
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @NonNull
    public final Observable<T> refCount(long timeout, @NonNull TimeUnit unit) {
        return refCount(1, timeout, unit, Schedulers.computation());
    }

    /**
     * 当已订阅的 observer 数量达到 1 时连接上游 {@code ConnectableObservable}，
     * 若所有 {@link Observer} 均已取消订阅，则在指定超时后断开连接。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>此 {@code refCount} 重载在指定 {@link Scheduler} 上运行。</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param timeout 所有 {@code Observer} 取消订阅后断开连接前的等待时间
     * @param unit 超时时间单位
     * @param scheduler 断开连接前等待的目标调度器
     * @return 新的 {@link Observable} 实例
     * @throws NullPointerException 若 {@code unit} 或 {@code scheduler} 为 {@code null}
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @NonNull
    public final Observable<T> refCount(long timeout, @NonNull TimeUnit unit, @NonNull Scheduler scheduler) {
        return refCount(1, timeout, unit, scheduler);
    }

    /**
     * 当已订阅的 observer 数量达到指定值时连接上游 {@code ConnectableObservable}，
     * 若所有 {@link Observer} 均已取消订阅，则在指定超时后断开连接。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>此 {@code refCount} 重载在 {@code computation} {@link Scheduler} 上运行。</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param observerCount 连接上游所需的 {@code Observer} 数量
     * @param timeout 所有 {@code Observer} 取消订阅后断开连接前的等待时间
     * @param unit 超时时间单位
     * @return 新的 {@link Observable} 实例
     * @throws NullPointerException 若 {@code unit} 为 {@code null}
     * @throws IllegalArgumentException 若 {@code observerCount} 非正数
     * @see #refCount(int, long, TimeUnit, Scheduler)
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @NonNull
    public final Observable<T> refCount(int observerCount, long timeout, @NonNull TimeUnit unit) {
        return refCount(observerCount, timeout, unit, Schedulers.computation());
    }

    /**
     * 当已订阅的 observer 数量达到指定值时连接上游 {@code ConnectableObservable}，
     * 若所有 {@link Observer} 均已取消订阅，则在指定超时后断开连接。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>此 {@code refCount} 重载在指定 {@link Scheduler} 上运行。</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param observerCount 连接上游所需的 {@code Observer} 数量
     * @param timeout 所有 {@code Observer} 取消订阅后断开连接前的等待时间
     * @param unit 超时时间单位
     * @param scheduler 断开连接前等待的目标调度器
     * @return 新的 {@link Observable} 实例
     * @throws NullPointerException 若 {@code unit} 或 {@code scheduler} 为 {@code null}
     * @throws IllegalArgumentException 若 {@code observerCount} 非正数
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @NonNull
    public final Observable<T> refCount(int observerCount, long timeout, @NonNull TimeUnit unit, @NonNull Scheduler scheduler) {
        ObjectHelper.verifyPositive(observerCount, "observerCount");
        Objects.requireNonNull(unit, "unit is null");
        Objects.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new ObservableRefCount<>(this, observerCount, timeout, unit, scheduler));
    }

    /**
     * 返回一个 {@link Observable}，在首个 {@link Observer} 订阅时自动（至多一次）连接本 {@code ConnectableObservable}。
     * <p>
     * <img width="640" height="348" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.o.png" alt="">
     * <p>
     * 连接在首次订阅后发生，且在返回的 {@code Observable} 生命周期内至多发生一次。
     * 若本 {@code ConnectableObservable} 终止，无论 {@code Observer} 如何增减，连接都不会恢复。
     * 使用 {@link #refCount()} 可在所有 {@code Observer} 释放 {@link Disposable} 时
     * 恢复连接或释放活跃连接。
     * <p>
     * 此重载无法断开通过 {@link #connect(Consumer)} 建立的连接。
     * 使用 {@link #autoConnect(int, Consumer)} 重载可获取表示唯一连接的 {@code Disposable}。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>{@code autoConnect} 重载不在任何特定 {@link Scheduler} 上运行。</dd>
     * </dl>
     *
     * @return 在首个 {@code Observer} 订阅时自动连接本 {@code ConnectableObservable} 的新 {@code Observable} 实例
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> autoConnect() {
        return autoConnect(1);
    }

    /**
     * 返回一个 {@link Observable}，在指定数量的 {@link Observer} 订阅时自动（至多一次）连接本 {@code ConnectableObservable}。
     * <p>
     * <img width="640" height="348" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.o.png" alt="">
     * <p>
     * 连接在给定数量的订阅后发生，且在返回的 {@code Observable} 生命周期内至多发生一次。
     * 若本 {@code ConnectableObservable} 终止，无论 {@code Observer} 如何增减，连接都不会恢复。
     * 使用 {@link #refCount()} 可在所有 {@code Observer} 释放 {@link Disposable} 时
     * 恢复连接或释放活跃连接。
     * <p>
     * 此重载无法断开通过 {@link #connect(Consumer)} 建立的连接。
     * 使用 {@link #autoConnect(int, Consumer)} 重载可获取表示唯一连接的 {@code Disposable}。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>{@code autoConnect} 重载不在任何特定 {@link Scheduler} 上运行。</dd>
     * </dl>
     *
     * @param numberOfObservers 在 {@code ConnectableObservable} 上调用 connect 前等待的订阅者数量。
     *                            非正数表示立即连接。
     * @return 在指定数量的 {@code Observer} 订阅时自动连接本 {@code ConnectableObservable} 的新 {@code Observable} 实例
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> autoConnect(int numberOfObservers) {
        return autoConnect(numberOfObservers, Functions.emptyConsumer());
    }

    /**
     * 返回一个 {@link Observable}，在指定数量的 {@link Observer} 订阅时自动（至多一次）连接本 {@code ConnectableObservable}，
     * 并以已建立连接关联的 {@link Disposable} 调用指定回调。
     * <p>
     * <img width="640" height="348" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.o.png" alt="">
     * <p>
     * 连接在给定数量的订阅后发生，且在返回的 {@code Observable} 生命周期内至多发生一次。
     * 若本 {@code ConnectableObservable} 终止，无论 {@code Observer} 如何增减，连接都不会恢复。
     * 使用 {@link #refCount()} 可在所有 {@code Observer} 释放 {@code Disposable} 时
     * 恢复连接或释放活跃连接。
     * <dl>
     *  <dt><b>调度器：</b></dt>
     *  <dd>{@code autoConnect} 重载不在任何特定 {@link Scheduler} 上运行。</dd>
     * </dl>
     *
     * @param numberOfObservers 在 {@code ConnectableObservable} 上调用 connect 前等待的订阅者数量。
     *                            非正数表示立即连接。
     * @param connection 将接收表示已建立连接的 {@code Disposable} 的回调 {@link Consumer}
     * @return 在指定数量的 {@code Observer} 订阅时自动连接本 {@code ConnectableObservable} 并以
     *         已建立连接关联的 {@code Disposable} 调用指定回调的新 {@code Observable} 实例
     * @throws NullPointerException 若 {@code connection} 为 {@code null}
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> autoConnect(int numberOfObservers, @NonNull Consumer<? super Disposable> connection) {
        Objects.requireNonNull(connection, "connection is null");
        if (numberOfObservers <= 0) {
            this.connect(connection);
            return RxJavaPlugins.onAssembly(this);
        }
        return RxJavaPlugins.onAssembly(new ObservableAutoConnect<>(this, numberOfObservers, connection));
    }
}
