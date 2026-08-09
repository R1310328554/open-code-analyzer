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
 * 表示热源（<strong>Subject</strong>）的类型：同时实现基础响应式类与对应 consumer 接口，
 * 可向多个 consumer 多播事件，也可作为 consumer 订阅同类源流。
 * <p>
 * 各 Subject 与基础类、consumer 接口对应关系：
 * <br>
 * <table border="1" style="border-collapse: collapse;">
 * <caption>可用 Subject 类及其基础类与 consumer 接口。</caption>
 * <tr><td><b>Subject 类型</b></td><td><b>基础类</b></td><td><b>Consumer 接口</b></td></tr>
 * <tr>
 *     <td>{@link io.reactivex.rxjava4.subjects.Subject Subject}
 *     <br>&nbsp;&nbsp;&nbsp;{@link io.reactivex.rxjava4.subjects.AsyncSubject AsyncSubject}
 *     <br>&nbsp;&nbsp;&nbsp;{@link io.reactivex.rxjava4.subjects.BehaviorSubject BehaviorSubject}
 *     <br>&nbsp;&nbsp;&nbsp;{@link io.reactivex.rxjava4.subjects.PublishSubject PublishSubject}
 *     <br>&nbsp;&nbsp;&nbsp;{@link io.reactivex.rxjava4.subjects.ReplaySubject ReplaySubject}
 *     <br>&nbsp;&nbsp;&nbsp;{@link io.reactivex.rxjava4.subjects.UnicastSubject UnicastSubject}
 *     </td>
 *     <td>{@link io.reactivex.rxjava4.core.Observable Observable}</td>
 *     <td>{@link io.reactivex.rxjava4.core.Observer Observer}</td>
 * </tr>
 * <tr>
 *     <td>{@link io.reactivex.rxjava4.subjects.SingleSubject SingleSubject}</td>
 *     <td>{@link io.reactivex.rxjava4.core.Single Single}</td>
 *     <td>{@link io.reactivex.rxjava4.core.SingleObserver SingleObserver}</td>
 * </tr>
 * <tr>
 *     <td>{@link io.reactivex.rxjava4.subjects.MaybeSubject MaybeSubject}</td>
 *     <td>{@link io.reactivex.rxjava4.core.Maybe Maybe}</td>
 *     <td>{@link io.reactivex.rxjava4.core.MaybeObserver MaybeObserver}</td>
 * </tr>
 * <tr>
 *     <td>{@link io.reactivex.rxjava4.subjects.CompletableSubject CompletableSubject}</td>
 *     <td>{@link io.reactivex.rxjava4.core.Completable Completable}</td>
 *     <td>{@link io.reactivex.rxjava4.core.CompletableObserver CompletableObserver}</td>
 * </tr>
 * </table>
 * <p>
 * 支持背压的 {@code Subject} 变体为 {@link java.util.concurrent.Flow.Processor}，
 * 位于 {@code io.reactivex.processors} 包。
 * @see io.reactivex.rxjava4.processors
 */
package io.reactivex.rxjava4.subjects;
