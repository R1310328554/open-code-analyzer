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

package io.reactivex.rxjava4.operators;

import java.util.concurrent.CompletionStage;

/// 表示长度未知、可延迟就绪的可迭代源：可同步前移，
/// 并在报告元素可消费后通过简单调用获取当前项。
/// <p>
/// 避免 {@code hasNext} 与 {@code next} 的重复调用；C# IEnumerator 在此方面更优。
/// @param <T> 源元素类型
/// @see IndexableSource
/// @since 4.0.0
public interface DeferredEnumerableSource<T> extends EnumerableSource<T> {

    /**
     * 返回源是否可通过 {@link EnumerableSource#nextSync()} 与
     * {@link EnumerableSource#current()} 消费。
     * @return 完成阶段：空源为 {@code false}，非空可迭代源就绪为 {@code true}
     */
    CompletionStage<Boolean> enumerableReady();
}
