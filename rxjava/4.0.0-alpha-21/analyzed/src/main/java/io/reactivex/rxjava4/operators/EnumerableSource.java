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

/// 表示长度未知的可迭代源：可同步前移并通过简单调用获取当前项。
/// <p>
/// 避免 {@code hasNext} 与 {@code next} 的重复调用；C# IEnumerator 在此方面更优。
/// @param <T> 源元素类型
/// @see IndexableSource
/// @since 4.0.0
public interface EnumerableSource<T> {

    /**
     * 同步获取下一项；若无更多项则返回 {@code false}。
     * @return 若有可用项（可通过 {@link #current()} 获取）则为 {@code true}，
     *         否则为 {@code false}
     * @throws Throwable 同步前进时发生（处理）错误
     */
    boolean nextSync() throws Throwable;

    /**
     * 若上次 {@link #nextSync()} 返回 {@code true}，返回当前项。
     * <p>
     * 在首次调用前或源耗尽后调用行为未定义。
     * @return 当前项
     */
    T current(); // FIXME not sure about the name clash with Streamable.current
}
