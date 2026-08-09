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

package io.reactivex.rxjava4.internal.fuseable;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.Streamable;

/**
 * 表示实现者可通过 {@link #source()} 方法提供上游 {@link Streamable} 类源。
 *
 * @param <T> 序列的值类型
 * @since 4.0.0
 */
public interface HasUpstreamStreamableSource<@NonNull T> {
    /**
     * 返回本 {@link Streamable} 的上游源。
     * <p>用于发现 streamable 链。
     * @return 源 {@code Streamable}
     */
    @NonNull
    Streamable<T> source();
}
