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
 * 表示所谓热、背压感知源（<strong>processor</strong>）的类：实现
 * {@link io.reactivex.rxjava4.processors.FlowableProcessor FlowableProcessor}，
 * 即 Reactive Streams {@link java.util.concurrent.Flow.Processor Processor} 接口，
 * 支持向一个或多个订阅者多播，也可消费另一 Reactive Streams {@link java.util.concurrent.Flow.Publisher Publisher}。
 * <p>
 * 可用 processor 实现：
 * <br>
 * <ul>
 *     <li>{@link io.reactivex.rxjava4.processors.AsyncProcessor AsyncProcessor} — 重放最后一项</li>
 *     <li>{@link io.reactivex.rxjava4.processors.BehaviorProcessor BehaviorProcessor} — 记住最新一项</li>
 *     <li>{@link io.reactivex.rxjava4.processors.MulticastProcessor MulticastProcessor} — 协调源与消费者</li>
 *     <li>{@link io.reactivex.rxjava4.processors.PublishProcessor PublishProcessor} — 向当前消费者分发</li>
 *     <li>{@link io.reactivex.rxjava4.processors.ReplayProcessor ReplayProcessor} — 记住部分或全部项并重放</li>
 *     <li>{@link io.reactivex.rxjava4.processors.UnicastProcessor UnicastProcessor} — 向单一消费者记住或转发</li>
 * </ul>
 * <p>
 * {@code FlowableProcessor} 的无背压变体称为 {@link io.reactivex.rxjava4.subjects.Subject}，
 * 位于 {@code io.reactivex.subjects} 包。
 * @see io.reactivex.rxjava4.subjects
 */
package io.reactivex.rxjava4.processors;
