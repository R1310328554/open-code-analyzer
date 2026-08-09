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

package io.reactivex.rxjava4.internal.operators.streamable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.operators.*;

/// 标记接口：表示 [Streamable] 源将产生同时支持 [IndexableSource]
/// 与 [EnumerableSource] 的 [Streamer]（同步可枚举/可索引）。
/// @param <T> `Streamable` 的元素类型
/// @since 4.0.0
public interface IsSynchronousStreamable<T> extends IsIndexableStreamable<T>, IsEnumerableStreamable<T> {

}
