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

/**
 * 基础响应式类： {@link io.reactivex.rxjava4.core.Flowable},  {@link io.reactivex.rxjava4.core.Observable},
 * {@link io.reactivex.rxjava4.core.Single},  {@link io.reactivex.rxjava4.core.Maybe} and
 *  {@link io.reactivex.rxjava4.core.Completable}; 基础响应式消费者；
 * 以及其他常用基础接口。
 *
 * <p>本库支持订阅并组合异步事件与回调。</p>
 * <p>Flowable/Subscriber、Observable/Observer、Single/SingleObserver 与
 * Completable/CompletableObserver 接口及其关联算子（位于
 * {@code io.reactivex.internal.operators} 包）受 Microsoft .NET 中 Reactive Rx 库启发，
 * 但基于更先进的 Reactive-Streams（ http://www.reactivestreams.org ）原则设计与实现。</p>
 * <p>
 * 更多信息见 <a
 * href="http://msdn.microsoft.com/en-us/data/gg577609">http://msdn.microsoft.com/en-us/data/gg577609</a>.
 * </p>
 *
 *
 * <p>与 Microsoft 实现的对应关系：
 * <ul>
 * <li>Observable == IObservable （基础类型）</li>
 * <li>Observer == IObserver （事件消费者）</li>
 * <li>Disposable == IDisposable （资源/取消管理）</li>
 * <li>Observable == Observable （工厂方法）</li>
 * <li>Flowable == IAsyncEnumerable （背压）</li>
 * <li>Subscriber == IAsyncEnumerator</li>
 * </ul>
 * Single 与 Completable 响应式基础类型在 Rx.NET 3.x 中尚无对应物。
 *
 * <p>希望异步暴露数据并允许响应式处理与组合的服务，
 * 可实现
 * {@link io.reactivex.rxjava4.core.Flowable}, {@link io.reactivex.rxjava4.core.Observable}, {@link io.reactivex.rxjava4.core.Single},
 * {@link io.reactivex.rxjava4.core.Maybe} or {@link io.reactivex.rxjava4.core.Completable} 类，供消费者订阅并接收事件。</p>
 * <p>用法示例见 {@link io.reactivex.rxjava4.core.Flowable}/{@link io.reactivex.rxjava4.core.Observable}
 * and {@link java.util.concurrent.Flow.Subscriber} classes.</p>
 * <p>
 * 支持 Flowable 基础响应式类的类型：
 * {@link io.reactivex.rxjava4.core.ConnectableFlowable} and
 * {@link io.reactivex.rxjava4.core.GroupedFlowable}.
 * <p>
 * 支持 Observable 基础响应式类的类型：
 * {@link io.reactivex.rxjava4.core.ConnectableObservable} and
 * {@link io.reactivex.rxjava4.core.GroupedObservable}.
 */
package io.reactivex.rxjava4.core;

