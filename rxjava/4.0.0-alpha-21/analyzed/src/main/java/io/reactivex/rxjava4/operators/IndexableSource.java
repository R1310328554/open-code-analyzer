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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Streamer;

/// 表示可通过从零开始的索引同步访问的源，
/// 无需像 [Streamer#next()] 那样逐步获取下一项。
/// @param <T> 源元素类型
/// @since 4.0.0
public interface IndexableSource<T> {

    /**
     * 按给定索引获取元素；请仅读取不超过 {@link #limit()} 的范围。
     * @param index 索引
     * @return 指定索引处的元素
     * @throws Throwable 若索引访问涉及可能抛出的计算
     */
    @NonNull
    T elementAt(long index) throws Throwable;

    /**
     * 返回可通过 {@link #elementAt(long)} 获取的元素数量上限。
     * @return 索引上限（不含）
     */
    long limit();
}
