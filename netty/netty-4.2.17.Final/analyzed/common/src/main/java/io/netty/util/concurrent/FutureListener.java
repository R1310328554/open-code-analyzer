/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.util.concurrent;

/**
 * A subtype of {@link GenericFutureListener} that hides type parameter for convenience.
 * <pre>
 * Future f = new DefaultPromise(..);
 * f.addListener(new FutureListener() {
 *     public void operationComplete(Future f) { .. }
 * });
 * </pre>
 *
 * <p>{@link GenericFutureListener} 的便捷子类型，隐藏泛型参数，便于匿名实现：
 * 回调参数类型固定为 {@link Future} 而非 {@code Future&lt;V&gt;}。</p>
 *
 * <p>典型用法：{@code future.addListener(new FutureListener&lt;T&gt;() { ... })}，
 * 无需在 {@link GenericFutureListener#operationComplete} 签名中声明 {@code Future&lt;V&gt;} 泛型。</p>
 *
 * @param <V> Future 结果类型，与 {@link Future}&lt;V&gt; 一致
 */
public interface FutureListener<V> extends GenericFutureListener<Future<V>> { }
